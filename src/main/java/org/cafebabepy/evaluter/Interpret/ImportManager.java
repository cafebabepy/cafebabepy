package org.cafebabepy.evaluter.Interpret;

import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.RaiseException;
import org.cafebabepy.runtime.module.DefinePyModule;
import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.runtime.object.PyModuleObject;
import org.cafebabepy.util.ReflectionUtils;
import org.cafebabepy.util.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Created by yotchang4s on 2018/04/28.
 */
class ImportManager {
    private Python runtime;

    ImportManager(Python runtime) {
        this.runtime = runtime;
    }

    void importSimple(PyObject context, PyObject name) {
        importAsName(context, name, this.runtime.None());
    }

    void importAsName(PyObject context, PyObject name, PyObject asName) {
        PyObject importName = asName.isNone() ? name : asName;

        String[] moduleNames = StringUtils.splitDot(name.toJava(String.class));

        StringBuilder moduleNameBuilder = new StringBuilder();
        for (String moduleName : moduleNames) {
            moduleNameBuilder.append(moduleName);

            String currentModuleName = moduleNameBuilder.toString();
            PyObject module = this.runtime.module(currentModuleName).orElseGet(() -> loadModule(currentModuleName));

            context.getScope().put(importName, module);

            moduleNameBuilder.append(".");
        }
    }

    void importFrom(PyObject context, PyObject moduleName, PyObject names, PyObject level) {
        String moduleNameJava = moduleName.toJava(String.class);
        int levelJava = level.toJava(int.class);

        String fromModuleName = getImportModuleName(context.getName(), moduleNameJava, levelJava);

        String[] moduleSplitNames = StringUtils.splitDot(fromModuleName);

        StringBuilder moduleNameBuilder = new StringBuilder();
        for (String moduleSplitName : moduleSplitNames) {
            moduleNameBuilder.append(moduleSplitName);

            String currentModuleName = moduleNameBuilder.toString();
            PyObject loadModule = this.runtime.module(currentModuleName).orElseGet(() -> loadModule(currentModuleName));

            this.runtime.iter(names, n -> {
                PyObject name = this.runtime.getattr(n, "name");
                PyObject asName = this.runtime.getattr(n, "asname");
                PyObject importName = asName.isNone() ? name : asName;

                String nameJava = name.toJava(String.class);
                if ("*".equals(nameJava)) {
                    context.getScope().put(loadModule.getScope());

                } else {
                    PyObject target = this.runtime.getattrOptional(loadModule, nameJava).orElseThrow(() ->
                            this.runtime.newRaiseException("builtins.ImportError", "cannot import name '" + nameJava + "'")
                    );

                    context.getScope().put(importName, target);
                }
            });

            moduleNameBuilder.append(".");
        }
    }

    private String getImportModuleName(String baseName, String moduleName, int level) {
        String[] dotSplit = StringUtils.splitDot(baseName);
        if (dotSplit.length < level) {
            throw this.runtime.newRaiseException("builtins.ValueError", "attempted relative import beyond top-level package");
        }

        if (level > 0) {
            StringBuilder builder = new StringBuilder();

            for (int i = level; i >= level; i--) {
                builder.append(dotSplit[level - i]).append('.');
            }

            builder.append(moduleName);

            return builder.toString();
        }

        return moduleName;
    }

    @SuppressWarnings("unchecked")
    PyObject loadModule(String moduleName) {
        try {
            Optional<PyObject> moduleOpt;

            moduleOpt = loadModuleFromClassPath(moduleName);
            if (!moduleOpt.isPresent()) {
                moduleOpt = loadModuleFromFile(moduleName);
            }

            if (moduleOpt.isPresent()) {
                PyObject module = moduleOpt.get();

                Set<Class<?>> classes = ReflectionUtils.getClasses("org.cafebabepy.runtime.module." + moduleName);
                Set<String> checkDuplicateTypes = new HashSet<>();
                for (Class<?> c : classes) {
                    DefinePyType definePyType = c.getAnnotation(DefinePyType.class);
                    if (definePyType == null || !PyObject.class.isAssignableFrom(c)) {
                        continue;
                    }

                    String javaObjectModuleName;
                    String[] splitDot = StringUtils.splitDot(definePyType.name());
                    if (splitDot.length == 1) {
                        javaObjectModuleName = definePyType.name();

                    } else {
                        String[] moduleSplitDotArray = new String[splitDot.length - 1];
                        System.arraycopy(splitDot, 0, moduleSplitDotArray, 0, splitDot.length - 1);
                        javaObjectModuleName = String.join(".", moduleSplitDotArray);
                    }

                    if (!moduleName.equals(javaObjectModuleName)) {
                        throw new CafeBabePyException("Invalid module '" + javaObjectModuleName + "'");
                    }

                    if (checkDuplicateTypes.contains(definePyType.name())) {
                        throw new CafeBabePyException("Duplicate type '" + definePyType.name() + "'");
                    }

                    PyObject type = this.runtime.createJavaPyObject((Class<PyObject>) c);
                    module.getScope().put(this.runtime.str(type.getName()), type);

                    checkDuplicateTypes.add(definePyType.name());
                }

                return module;
            }

        } catch (IOException e) {
            throw new CafeBabePyException("Internal Error", e);
        }

        throw this.runtime.newRaiseException("builtins.ImportError", "No module named '" + moduleName + "'");
    }

    private Optional<PyObject> loadModuleFromFile(String moduleName) throws IOException {
        File baseFile = new File(".").getAbsoluteFile().getParentFile();

        String path = moduleName.replace('.', '/');

        // __init__
        String initPath = path + "/__init__.py";
        File initFile = new File(baseFile, initPath);

        try (InputStream in = new FileInputStream(initFile)) {
            return loadModuleFromInputStream(moduleName, initFile.getAbsolutePath(), in);

        } catch (FileNotFoundException e) {
            // Nothing
        }

        // module.py
        String pyPath = path + ".py";
        File file = new File(baseFile, pyPath);

        try (InputStream in = new FileInputStream(file)) {
            return loadModuleFromInputStream(moduleName, file.getAbsolutePath(), in);

        } catch (FileNotFoundException e) {
            // Nothing
        }

        // empty module
        File dir = new File(baseFile, path);
        if (dir.isDirectory()) {
            return Optional.of(createModule(moduleName));
        }

        return Optional.empty();
    }

    private Optional<PyObject> loadModuleFromClassPath(String moduleName) throws IOException {
        Class<PyObject> moduleClass = null;

        Set<Class<?>> classes = ReflectionUtils.getClasses("org.cafebabepy.runtime.module." + moduleName);
        for (Class<?> clazz : classes) {
            DefinePyModule ma = clazz.getAnnotation(DefinePyModule.class);
            if (ma != null) {
                if (!PyObject.class.isAssignableFrom(clazz)) {
                    throw new CafeBabePyException("not module" + moduleName);
                }

                if (moduleClass != null) {
                    throw new CafeBabePyException("module duplicate" + ma.name());
                }

                moduleClass = (Class<PyObject>) clazz;
            }
        }

        if (moduleClass != null) {
            this.runtime.initializeModuleAndTypes((Class<PyObject>) moduleClass);

            return this.runtime.module(moduleName);
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        String path = moduleName.replace('.', '/');

        InputStream in;

        String file = path + "/__init__.py";

        // __init__.py
        in = classLoader.getResourceAsStream(file);
        if (in != null) {
            try (InputStream tmpIn = in) {
                return loadModuleFromInputStream(file, moduleName, tmpIn);
            }
        }

        file = path + ".py";

        // module.py
        in = classLoader.getResourceAsStream(file);
        if (in != null) {
            try (InputStream tmpIn = in) {
                return loadModuleFromInputStream(file, moduleName, tmpIn);
            }
        }

        if (ReflectionUtils.isDirectory(moduleName)) {
            return Optional.of(createModule(moduleName));
        }

        return Optional.empty();
    }

    private Optional<PyObject> loadModuleFromInputStream(String file, String moduleName, InputStream in) throws IOException {
        StringBuilder builder = new StringBuilder();

        // TODO UTF-8固定はまずいか？
        Charset charset = Charset.forName("UTF-8");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset))) {
            char[] buffer = new char[5120];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                builder.append(buffer, 0, read);
            }
        }

        PyObject module = createModule(moduleName);

        PyObject sys = this.runtime.moduleOrThrow("sys");
        PyObject modules = this.runtime.getattr(sys, "modules");

        this.runtime.setitem(modules, this.runtime.str(moduleName), module);

        try {
            this.runtime.eval(module, file, builder.toString());

        } catch (RaiseException e) {
            this.runtime.del(modules, this.runtime.str(moduleName));
            throw e;
        }

        return Optional.of(module);
    }

    private PyObject createModule(String name) {
        PyObject module = new PyModuleObject(this.runtime, name);
        module.initialize();

        PyObject builtinsModule = this.runtime.moduleOrThrow("builtins");
        Map<PyObject, PyObject> objectMap = builtinsModule.getScope().gets();
        for (Map.Entry<PyObject, PyObject> e : objectMap.entrySet()) {
            module.getScope().put(e.getKey(), e.getValue());
        }

        return module;
    }
}
