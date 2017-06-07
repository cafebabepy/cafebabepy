package org.cafebabepy.runtime;

import org.cafebabepy.annotation.DefineCafeBabePyModule;
import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.module.builtins.PyIntType;
import org.cafebabepy.runtime.module.builtins.PyStrType;
import org.cafebabepy.runtime.module.builtins.PyTupleType;
import org.cafebabepy.util.ModuleOrClassSplitter;
import org.cafebabepy.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yotchang4s on 2017/05/12.
 */
public final class Python {

    public static final String MAIN_MODULE_NAME = "__main__";

    public static final String BUILTINS_MODULE_NAME = "builtins";

    public static final String TYPES_MODULE_NAME = "types";

    // FIXME sys.modulesに持って行きたい
    private Map<String, PyObject> moduleMap;

    private PyObject none;

    private PyObject notImplementedType;

    private Python() {
        this.moduleMap = new ConcurrentHashMap<>();
    }

    public static Python createRuntime() {
        Python runtime = new Python();
        runtime.initialize();

        return runtime;
    }

    private void initialize() {
        initializeBuiltins("org.cafebabepy.runtime.module");
        initializeBuiltins("org.cafebabepy.runtime.module.types");
        initializeBuiltins("org.cafebabepy.runtime.module.builtins");
        initializeBuiltins("org.cafebabepy.runtime.module._ast");
        initializeObjects();
    }

    private void initializeBuiltins(String packageName) {
        Set<Class<?>> builtinsClasses;

        // FIXME 本当は form builtins import * の形にしたい
        try {
            builtinsClasses = ReflectionUtils.getClasses(packageName);

            PyObject module = null;
            for (Class<?> clazz : builtinsClasses) {
                DefineCafeBabePyModule defineCafeBabePyModule = clazz.getAnnotation(DefineCafeBabePyModule.class);
                if (defineCafeBabePyModule == null) {
                    continue;
                }

                // Check duplicate module
                if (module != null) {
                    throw new CafeBabePyException(
                            "Duplicate module '"
                                    + clazz.getName()
                                    + "' and '"
                                    + module.getClass().getName()
                                    + "'");

                }

                Constructor c = clazz.getConstructor(Python.class);
                module = (PyObject) c.newInstance(this);
            }

            if (module == null) {
                throw new CafeBabePyException("'" + packageName + "' module not found");
            }

            for (Class<?> clazz : builtinsClasses) {
                DefineCafeBabePyType defineCafeBabePyType = clazz.getAnnotation(DefineCafeBabePyType.class);
                if (defineCafeBabePyType == null) {
                    continue;
                }

                Constructor c = clazz.getConstructor(Python.class);
                c.newInstance(this);
            }

        } catch (
                IOException |
                        NoSuchMethodException |
                        InstantiationException |
                        IllegalAccessException |
                        InvocationTargetException |
                        ClassCastException e) {

            throw new CafeBabePyException("Fail initialize", e);
        }
    }

    private void initializeObjects() {
        this.none = type("types.NoneType", false)
                .map(o -> o.call())
                .orElseThrow(() -> new CafeBabePyException("'NoneType' is not found"));

        this.notImplementedType = type("builtins.NotImplementedType", false)
                .map(o -> o.call())
                .orElseThrow(() -> new CafeBabePyException("'NotImplementedType' is not found"));
    }

    public PyObject str(String str) {
        return PyStrType.newStr(this, str);
    }

    public PyObject number(int value) {
        return PyIntType.newInt(this, value);
    }

    public PyObject tuple(PyObject... args) {
        return PyTupleType.newTuple(this, args);
    }

    public PyObject none() {
        return this.none;
    }

    public PyObject notImplementedType() {
        return this.notImplementedType;
    }

    public PyObject moduleOrThrow(String name) {
        return module(name).orElseThrow(() ->
                newRaiseException("builtins.NameError", "name '" + name + "' is not defined"));
    }

    public Optional<PyObject> module(String name) {
        return Optional.ofNullable(this.moduleMap.get(name));
    }

    public PyObject typeOrThrow(String name) {
        return typeOrThrow(name, true);
    }

    public PyObject typeOrThrow(String name, boolean appear) {
        ModuleOrClassSplitter splitter = new ModuleOrClassSplitter(name);
        if (!splitter.getModuleName().isPresent()) {
            return moduleOrThrow(splitter.getSimpleName());

        } else {
            return moduleOrThrow(splitter.getModuleName().orElseThrow(
                    () -> newRaiseException("builtins.NameError", "name '" + name + "' is not defined")))
                    .getObjectOrThrow(splitter.getSimpleName(), appear);
        }
    }

    public Optional<PyObject> type(String name) {
        return type(name, true);
    }

    public Optional<PyObject> type(String name, boolean appear) {
        ModuleOrClassSplitter splitter = new ModuleOrClassSplitter(name);
        if (!splitter.getModuleName().isPresent()) {
            return module(splitter.getSimpleName());

        } else {
            return module(splitter.getModuleName().get())
                    .flatMap(n -> n.getScope().get(splitter.getSimpleName(), appear));
        }
    }

    public RaiseException newRaiseException(String exceptionType, String message) {
        ModuleOrClassSplitter splitter = new ModuleOrClassSplitter(exceptionType);

        PyObject exception = moduleOrThrow(splitter.getModuleName().orElseThrow(() ->
                newRaiseException("builtins.NameError",
                        "'" + splitter.getName() + "' module is not found")

        )).getObjectOrThrow(splitter.getSimpleName()).call(str(message));

        return new RaiseException(exception, message);
    }

    public void defineModule(PyObject module) {
        if (!module.isModule()) {
            throw newRaiseException("builtins.ModuleNotFoundError",
                    "No module named '" + module.getFullName() + "'");
        }
        String[] moduleNames = module.getName().split("\\.");
        if (moduleNames.length == 1) {
            this.moduleMap.put(moduleNames[0], module);
            return;
        }

        StringBuilder moduleBuilder = new StringBuilder();
        moduleBuilder.append(moduleNames[0]);

        for (int i = 0; i < moduleNames.length; i++) {
            moduleBuilder.append('.').append(moduleNames[i]);

            String moduleName = moduleBuilder.toString();
            if (!this.moduleMap.containsKey(moduleName)) {
                throw new CafeBabePyException("module '" + moduleName + "' is not found");
            }

            if (i == moduleNames.length - 1) {
                this.moduleMap.put(moduleName, module);
            }
        }
    }
}
