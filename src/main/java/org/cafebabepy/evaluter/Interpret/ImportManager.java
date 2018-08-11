package org.cafebabepy.evaluter.Interpret;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.RaiseException;
import org.cafebabepy.runtime.object.PyModuleObject;
import org.cafebabepy.util.ReflectionUtils;
import org.cafebabepy.util.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;

/**
 * Created by yotchang4s on 2018/04/28.
 */
class ImportManager {
    private Python runtime;

    ImportManager(Python runtime) {
        this.runtime = runtime;
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
                    PyObject target = this.runtime.getattrOptional(loadModule, nameJava).orElseGet(() -> {
                        PyObject sysModules = this.runtime.getattr(this.runtime.moduleOrThrow("sys"), "modules");

                        return this.runtime.getitemOptional(sysModules, name).orElseThrow(() ->
                                this.runtime.newRaiseException("builtins.ImportError", "cannot import name '" + nameJava + "'")
                        );
                    });

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

    private PyObject loadModule(String moduleName) {
        try {
            Optional<PyObject> moduleOpt;

            moduleOpt = loadModuleFromClassPath(moduleName);
            if (moduleOpt.isPresent()) {
                return moduleOpt.get();
            }

            moduleOpt = loadModuleFromFile(moduleName);
            if (moduleOpt.isPresent()) {
                return moduleOpt.get();
            }

            throw this.runtime.newRaiseException("builtins.ImportError", "No module named '" + moduleName + "'");

        } catch (IOException e) {
            e.printStackTrace();
            throw this.runtime.newRaiseException("builtins.ImportError", "No module named '" + moduleName + "'");
        }
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
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        String path = moduleName.replace('.', '/');

        InputStream in;

        String file = path + "/__init.__.py";

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
