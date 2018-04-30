package org.cafebabepy.evaluter.Interpret;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

            context.getScope().put(importName.toJava(String.class), module);

            moduleNameBuilder.append(".");
        }
    }

    private PyObject loadModule(String moduleName) {
        try {
            Optional<PyObject> moduleOpt = loadModuleFromClassPath(moduleName);
            if (moduleOpt.isPresent()) {
                return moduleOpt.get();

            } else {
                throw this.runtime.newRaiseException("builtins.ImportError", "No module named '" + moduleName + "'");
            }

        } catch (IOException e) {
            throw this.runtime.newRaiseException("builtins.ImportError", "No module named '" + moduleName + "'");
        }
    }

    private Optional<PyObject> loadModuleFromClassPath(String moduleName) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        // File
        InputStream in = classLoader.getResourceAsStream(moduleName.replace('.', '/') + ".py");
        if (in == null) {
            return Optional.empty();
        }

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
            this.runtime.eval(module, builder.toString());

        } catch (Exception e) {
            this.runtime.del(modules, this.runtime.str(moduleName));
            throw e;
        }

        return Optional.of(module);
    }

    private PyObject createModule(String name) {
        PyObject module = this.runtime.newPyObject("types.ModuleType", false, this.runtime.str(name));

        PyObject builtinsModule = this.runtime.moduleOrThrow("builtins");
        Map<String, PyObject> objectMap = builtinsModule.getScope().gets();
        for (Map.Entry<String, PyObject> e : objectMap.entrySet()) {
            module.getScope().put(e.getKey(), e.getValue());
        }

        return module;
    }
}
