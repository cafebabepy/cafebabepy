package org.cafebabepy.runtime;

import org.cafebabepy.annotation.DefineCafeBabePyModule;
import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.module.builtins.*;
import org.cafebabepy.runtime.object.PyNoneObject;
import org.cafebabepy.util.BinaryConsumer;
import org.cafebabepy.util.ModuleOrClassSplitter;
import org.cafebabepy.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static org.cafebabepy.util.ProtocolNames.__iter__;
import static org.cafebabepy.util.ProtocolNames.__next__;

/**
 * Created by yotchang4s on 2017/05/12.
 */
public final class Python {

    public static final String MAIN_MODULE_NAME = "__main__";

    public static final String BUILTINS_MODULE_NAME = "builtins";

    public static final String TYPES_MODULE_NAME = "types";

    // FIXME sys.modulesに持って行きたい
    private Map<String, PyObject> moduleMap;

    private PyObject objectObject;

    private PyObject noneObject;

    private PyObject trueObject;

    private PyObject falseObject;

    private PyObject notImplementedTypeObject;

    private Python() {
        this.moduleMap = new ConcurrentHashMap<>();
    }

    public static Python createRuntime() {
        Python runtime = new Python();
        runtime.initialize();

        return runtime;
    }

    private void initialize() {
        initializeBuiltins("org.cafebabepy.runtime.module.builtins");
        initializeBuiltins("org.cafebabepy.runtime.module.types");

        initializeBuiltins("org.cafebabepy.runtime.module");

        initializeObjects();
    }

    @SuppressWarnings("unchecked")
    public void initializeBuiltins(String packageName) {
        Set<Class<?>> builtinsClasses;

        // FIXME 本当は form builtins import * の形にしたい
        try {
            builtinsClasses = ReflectionUtils.getClasses(packageName);

        } catch (IOException e) {
            throw new CafeBabePyException("Fail initialize package '" + packageName + "'");
        }

        PyObject module = null;
        for (Class<?> c : builtinsClasses) {
            DefineCafeBabePyModule defineCafeBabePyModule = c.getAnnotation(DefineCafeBabePyModule.class);
            if (defineCafeBabePyModule == null || !PyObject.class.isAssignableFrom(c)) {
                continue;
            }

            Class<PyObject> clazz = (Class<PyObject>) c;

            // Check duplicate module
            if (module != null) {
                throw new CafeBabePyException(
                        "Duplicate module '"
                                + clazz.getName()
                                + "' and '"
                                + module.getClass().getName()
                                + "'");

            }

            module = createType(clazz, defineCafeBabePyModule.name());
            module.preInitialize();
        }

        if (module == null) {
            throw new CafeBabePyException("'" + packageName + "' module not found");
        }

        Set<PyObject> types = new HashSet<>();

        for (Class<?> c : builtinsClasses) {
            DefineCafeBabePyType defineCafeBabePyType = c.getAnnotation(DefineCafeBabePyType.class);
            if (defineCafeBabePyType == null || !PyObject.class.isAssignableFrom(c)) {
                continue;
            }

            Class<PyObject> clazz = (Class<PyObject>) c;

            PyObject type = createType(clazz, defineCafeBabePyType.name());
            type.preInitialize();

            types.add(type);
        }

        module.postInitialize();

        for (PyObject type : types) {
            type.postInitialize();
        }
    }

    private PyObject createType(Class<PyObject> clazz, String typeFullName) {
        try {
            Constructor<PyObject> constructor = clazz.getConstructor(Python.class);
            constructor.setAccessible(true);

            return constructor.newInstance(this);

        } catch (InstantiationException |
                InvocationTargetException |
                NoSuchMethodException |
                IllegalAccessException e) {
            throw new CafeBabePyException(
                    "Fail '" + typeFullName + "' initialize '" + clazz.getName() + "'", e);
        }
    }

    private void initializeObjects() {
        this.objectObject = type("builtins.object")
                .map(PyObject::callStatic)
                .orElseThrow(() -> new CafeBabePyException("'object' is not found"));

        this.noneObject = new PyNoneObject(this);

        this.trueObject = bool(true);
        this.falseObject = bool(false);

        this.notImplementedTypeObject = type("builtins.NotImplementedType", false)
                .map(PyObject::callStatic)
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

    public PyObject tuple(Collection<PyObject> args) {
        return PyTupleType.newTuple(this, args);
    }

    public PyObject list(PyObject... args) {
        return PyListType.newList(this, args);
    }

    public PyObject list(Collection<PyObject> args) {
        return PyListType.newList(this, args);
    }

    public PyObject object() {
        return this.objectObject;
    }

    public PyObject None() {
        return this.noneObject;
    }

    public PyObject True() {
        return this.trueObject;
    }

    public PyObject False() {
        return this.falseObject;
    }

    public PyObject bool(boolean bool) {
        return PyBoolType.newBool(this, bool);
    }

    public PyObject NotImplementedType() {
        return this.notImplementedTypeObject;
    }

    public PyObject getBuiltinsModule() {
        return moduleOrThrow(Python.BUILTINS_MODULE_NAME);
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

    public PyObject newPyObject(String typeName, PyObject... args) {
        PyObject type = typeOrThrow(typeName);

        PyObject[] selfArgs = new PyObject[args.length + 1];
        selfArgs[0] = type;
        System.arraycopy(args, 0, selfArgs, 1, args.length);

        return type.call(selfArgs);
    }

    public void iterIndex(PyObject object, BinaryConsumer<PyObject, Integer> action) {

        PyObject next = getNext(object);

        try {
            int i = 0;
            while (true) {
                PyObject value = next.call(next);
                action.accept(value, i);
                i++;
            }

        } catch (RaiseException e) {
            if (e.getException().getType() != typeOrThrow("builtins.StopIteration")) {
                throw e;
            }
        }
    }

    public void iter(PyObject object, Consumer<PyObject> action) {

        PyObject next = getNext(object);

        try {
            while (true) {
                PyObject value = next.call(next);
                action.accept(value);
            }

        } catch (RaiseException e) {
            if (e.getException().getType() != typeOrThrow("builtins.StopIteration")) {
                throw e;
            }
        }
    }

    private PyObject getNext(PyObject object) {
        PyObject obj;
        if (object.isType()) {
            obj = object;
        } else {
            obj = object.getType();
        }
        Optional<PyObject> next = obj.getObject(__next__);
        if (next.isPresent()) {
            return next.get();
        }

        PyObject iter = obj.getObject(__iter__).orElseThrow(() ->
                newRaiseException("builtins.TypeError",
                        "'" + object.getName() + "' object is not iterable"));

        return iter.getType().getObject(__next__).orElseThrow(() ->
                newRaiseException("builtins.TypeError",
                        "iter() returned non-iterator of type '" + iter.getType().getName() + "'"));
    }

    public RaiseException newRaiseException(String exceptionType) {
        ModuleOrClassSplitter splitter = new ModuleOrClassSplitter(exceptionType);

        PyObject eType = moduleOrThrow(splitter.getModuleName().orElseThrow(() ->
                newRaiseException("builtins.NameError",
                        "'" + splitter.getName() + "' module is not found")

        )).getObjectOrThrow(splitter.getSimpleName());

        PyObject e = PyObject.callStatic(eType);

        return new RaiseException(e);
    }

    public RaiseException newRaiseTypeError(String message) {
        PyObject typeErrorType = typeOrThrow("builtins.TypeError");
        return new RaiseException(typeErrorType, message);
    }

    public RaiseException newRaiseException(String exceptionType, String message) {
        ModuleOrClassSplitter splitter = new ModuleOrClassSplitter(exceptionType);

        PyObject eType = moduleOrThrow(splitter.getModuleName().orElseThrow(() ->
                newRaiseException("builtins.NameError",
                        "'" + splitter.getName() + "' module is not found")

        )).getObjectOrThrow(splitter.getSimpleName());

        PyObject e = PyObject.callStatic(eType);

        return new RaiseException(e, message);
    }

    public void defineModule(PyObject module) {
        if (!module.isModule()) {
            throw new CafeBabePyException("No module named '" + module.getFullName() + "'");
        }
        String[] moduleNames = module.getModuleName().orElseThrow(() ->
                new CafeBabePyException("moduleName is not found")
        ).split("\\.");

        if (moduleNames.length == 1) {
            this.moduleMap.put(moduleNames[0], module);
            return;
        }

        StringBuilder moduleBuilder = new StringBuilder();
        moduleBuilder.append(moduleNames[0]);

        for (int i = 1; i < moduleNames.length; i++) {
            String moduleName = moduleBuilder.toString();
            if (!this.moduleMap.containsKey(moduleName)) {
                throw new CafeBabePyException("module '" + moduleName + "' is not found");
            }

            moduleBuilder.append('.').append(moduleNames[i]);

            if (i == moduleNames.length - 1) {
                this.moduleMap.put(moduleBuilder.toString(), module);
            }
        }
    }
}
