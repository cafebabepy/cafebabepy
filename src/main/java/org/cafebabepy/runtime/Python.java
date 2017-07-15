package org.cafebabepy.runtime;

import org.cafebabepy.annotation.DefineCafeBabePyModule;
import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.object.*;
import org.cafebabepy.util.ModuleOrClassSplitter;
import org.cafebabepy.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.cafebabepy.util.ProtocolNames.*;

/**
 * Created by yotchang4s on 2017/05/12.
 */
public final class Python {

    public static final String MAIN_MODULE_NAME = "__main__";

    public static final String BUILTINS_MODULE_NAME = "builtins";

    // FIXME sys.modulesに持って行きたい
    private Map<String, PyObject> moduleMap;

    private PyObject objectObject;

    private PyNoneObject noneObject;

    private PyBoolObject trueObject;

    private PyBoolObject falseObject;

    private PyNotImplementedObject notImplementedTypeObject;

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
        initializeBuiltins("org.cafebabepy.runtime.module");

        initializeObjects();

        // TODO __main__でいいの？
        PyObject builtinsModule = moduleOrThrow("builtins");
        PyObject mainModule = moduleOrThrow("__main__");

        Map<String, PyObject> objectMap = builtinsModule.getObjects();
        for (Map.Entry<String, PyObject> e : objectMap.entrySet()) {
            mainModule.getScope().put(e.getKey(), e.getValue());
        }
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
        Set<String> checkDuplicateTypes = new HashSet<>();

        for (Class<?> c : builtinsClasses) {
            DefineCafeBabePyType defineCafeBabePyType = c.getAnnotation(DefineCafeBabePyType.class);
            if (defineCafeBabePyType == null || !PyObject.class.isAssignableFrom(c)) {
                continue;
            }

            Class<PyObject> clazz = (Class<PyObject>) c;

            PyObject type = createType(clazz, defineCafeBabePyType.name());
            type.preInitialize();

            if (checkDuplicateTypes.contains(defineCafeBabePyType.name())) {
                throw new CafeBabePyException("Duplicate type '" + defineCafeBabePyType.name() + "'");
            }
            types.add(type);
            checkDuplicateTypes.add(defineCafeBabePyType.name());
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
                .map(c -> c.call())
                .orElseThrow(() -> new CafeBabePyException("'object' is not found"));

        this.noneObject = new PyNoneObject(this);
        this.trueObject = new PyTrueObject(this);
        this.falseObject = new PyFalseObject(this);
        this.notImplementedTypeObject = new PyNotImplementedObject(this);
    }

    public PyStrObject str(String value) {
        PyStrObject object = new PyStrObject(this, value);
        object.preInitialize();
        object.postInitialize();

        return object;
    }

    public PyIntObject number(int value) {
        PyIntObject object = new PyIntObject(this, value);
        object.preInitialize();
        object.postInitialize();

        return object;
    }

    public PyObject tuple(Collection<PyObject> value) {
        PyObject[] array = new PyObject[value.size()];
        value.toArray(array);

        return tuple(array);
    }

    public PyObject tuple(PyObject... value) {
        PyTupleObject object = new PyTupleObject(this, value);
        object.preInitialize();
        object.postInitialize();

        return object;
    }

    public PyObject list(Collection<PyObject> value) {
        PyObject[] array = new PyObject[value.size()];
        value.toArray(array);

        return list(array);
    }

    public PyObject list(PyObject... value) {
        PyListObject object = new PyListObject(this, value);
        object.preInitialize();
        object.postInitialize();

        return object;
    }

    public PyBoolObject bool(boolean bool) {
        return bool ? this.trueObject : this.falseObject;
    }

    public PyGeneratorObject generator(Function<PyGeneratorObject.YieldStopper, PyObject> iter) {
        PyGeneratorObject object = new PyGeneratorObject(this, iter);
        object.preInitialize();
        object.postInitialize();

        return object;
    }

    public PyObject Object() {
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
        return newPyObject(typeName, true, args);
    }

    public PyObject newPyObject(String typeName, boolean appear, PyObject... args) {
        return typeOrThrow(typeName, appear).call(args);
    }

    public PyObject callFunction(String name, PyObject... args) {
        ModuleOrClassSplitter splitter = new ModuleOrClassSplitter(name);
        Optional<String> moduleNameOpt = splitter.getModuleName();
        if (!splitter.getModuleName().isPresent()) {
            throw newRaiseException("builtins.NameError",
                    "name '" + splitter.getSimpleName() + "'is not defined");
        }
        PyObject module = moduleOrThrow(moduleNameOpt.get());
        PyObject object = module.getObjectOrThrow(splitter.getSimpleName());

        return object.call(args);
    }

    public void iterIndex(PyObject object, BiConsumer<PyObject, Integer> action) {

        if(object instanceof PyListObject) {
            iterIndex((PyListObject)object, action);
            return;
        }

        PyObject next;
        PyObject obj;

        Optional<PyObject> nextOpt = getNext(object);
        if (nextOpt.isPresent()) {
            next = nextOpt.get();
            obj = object;

        } else {
            PyObject iterType = getIterType(object);
            PyObject iter = iterType.call(object);
            next = getIterNext(iter);
            obj = iter;
        }


        int i = 0;
        do {
            PyObject value;
            try {
                value = next.call(obj);

            } catch (RaiseException e) {
                PyObject exception = e.getException();
                if (!isInstance(exception, "builtins.StopIteration")) {
                    throw e;
                }
                break;
            }

            action.accept(value, i);
            i++;

        } while (true);
    }

    private void iterIndex(PyListObject listObject, BiConsumer<PyObject, Integer> action) {
        List<PyObject> list = listObject.getRawList();
        for(int i = 0; i < list.size(); i++) {
            action.accept(list.get(i), i);
        }
    }

    public void iter(PyObject object, Consumer<PyObject> action) {

        if(object instanceof PyListObject) {
            iter((PyListObject)object, action);
            return;
        }

        PyObject next;
        PyObject obj;

        Optional<PyObject> nextOpt = getNext(object);
        if (nextOpt.isPresent()) {
            next = nextOpt.get();
            obj = object;

        } else {
            PyObject iterType = getIterType(object);
            PyObject iter = iterType.call(object);
            next = getIterNext(iter);
            obj = iter;
        }


        do {
            PyObject value;

            try {
                value = next.call(obj);

            } catch (RaiseException e) {
                PyObject exception = e.getException();
                if (!isInstance(exception, "builtins.StopIteration")) {
                    throw e;
                }
                break;
            }

            action.accept(value);

        } while (true);
    }

    private void iter(PyListObject listObject, Consumer<PyObject> action) {
        List<PyObject> list = listObject.getRawList();
        for(int i = 0; i < list.size(); i++) {
            action.accept(list.get(i));
        }
    }

    public PyObject eq(PyObject x, PyObject y) {
        PyObject xType = x.getType();
        PyObject yType = y.getType();

        PyObject result;
        if (xType == yType || !isSubClass(xType, yType)) {
            result = refSimpleOp(x, y, "==", __eq__, __eq__);

        } else {
            result = refSimpleOp(y, x, "==", __eq__, __eq__);
        }
        if (result.isNotImplemented()) {
            result = bool(x == y);
        }

        return result;
    }

    public PyObject ne(PyObject x, PyObject y) {
        PyObject xType = x.getType();
        PyObject yType = y.getType();

        PyObject result;
        if (xType == yType || !isSubClass(xType, yType)) {
            result = refSimpleOp(x, y, "!=", __ne__, __ne__);

        } else {
            result = refSimpleOp(y, x, "!=", __ne__, __ne__);
        }

        if (result.isNotImplemented()) {
            if (eq(x, y).isTrue()) {
                return False();

            } else {
                return True();
            }
        }

        return result;
    }

    // <
    public PyObject lt(PyObject x, PyObject y) {
        return refOp(x, y, "<", __lt__, __gt__);
    }

    // <=
    public PyObject le(PyObject x, PyObject y) {
        return refOp(x, y, "<=", __le__, __rle__);
    }

    // >
    public PyObject gt(PyObject x, PyObject y) {
        return refOp(x, y, ">", __gt__, __lt__);
    }

    // >=
    public PyObject ge(PyObject x, PyObject y) {
        return refOp(x, y, ">=", __ge__, __rge__);
    }

    // +
    public PyObject add(PyObject x, PyObject y) {
        return refOp(x, y, "+", __add__, __radd__);
    }

    // -
    public PyObject sub(PyObject x, PyObject y) {
        return refOp(x, y, "-", __sub__, __rsub__);
    }

    // %
    public PyObject mod(PyObject x, PyObject y) {
        return refOp(x, y, "%", __mod__, __rmod__);
    }

    // *
    public PyObject mul(PyObject x, PyObject y) {
        return refOp(x, y, "*", __mul__, __mul__);
    }

    private PyObject refOp(PyObject x, PyObject y, String op, String opFunctionName, String ropFunctionName) {
        PyObject xType = x.getType();
        PyObject yType = y.getType();

        PyObject result;
        if (xType == yType || !isSubClass(xType, yType)) {
            result = refSimpleOp(x, y, op, opFunctionName, ropFunctionName);

        } else {
            result = refSimpleOp(y, x, op, ropFunctionName, opFunctionName);
        }

        if (result.isNotImplemented()) {
            throw newRaiseTypeError(
                    "'" + op + "' not supported between instances of '"
                            + x.getType().getFullName()
                            + "' and '"
                            + y.getType().getFullName()
                            + "'");
        }

        return result;
    }

    private PyObject refSimpleOp(PyObject x, PyObject y, String op, String opFunctionName, String ropFunctionName) {
        PyObject operator = x.getObject(opFunctionName).orElseThrow(() ->
                newRaiseTypeError("unsupported operand type(s) for " + op + ": '"
                        + x.getType().getFullName()
                        + "' and '"
                        + y.getType().getFullName()
                        + "'")
        );
        PyObject result = operator.call(x, y);

        if (result.isNotImplemented()) {
            PyObject rop = y.getObjectOrThrow(ropFunctionName);
            result = rop.call(y, x);
        }

        return result;
    }

    public List<PyObject> toList(PyObject object) {
        List<PyObject> list = new ArrayList<>();
        iter(object, list::add);

        return list;
    }

    private PyObject getIterType(PyObject object) {
        return object.getType().getObject(__iter__).orElseThrow(() ->
                newRaiseException("builtins.TypeError",
                        "'" + object.getName() + "' object is not iterable"));
    }

    private PyObject getIterNext(PyObject iter) {
        return iter.getType().getObject(__next__).orElseThrow(() ->
                newRaiseException("builtins.TypeError",
                        "iter() returned non-iterator of type '" + iter.getType().getName() + "'"));
    }

    private Optional<PyObject> getNext(PyObject object) {
        return object.getType().getObject(__next__);
    }

    public boolean isInstance(PyObject instance, PyObject type) {
        return callFunction("builtins.isinstance", instance, type).isTrue();
    }

    public boolean isSubClass(PyObject clazz, PyObject classInfo) {
        return callFunction("builtins.issubclass", clazz, classInfo).isTrue();
    }

    public boolean isInstance(PyObject instance, String typeName) {
        return isInstance(instance, typeOrThrow(typeName));
    }

    public boolean isIterable(PyObject object) {
        return object.getObject(__iter__).map(x -> true).orElse(false);
    }

    public RaiseException newRaiseException(String exceptionType) {
        ModuleOrClassSplitter splitter = new ModuleOrClassSplitter(exceptionType);

        PyObject eType = moduleOrThrow(splitter.getModuleName().orElseThrow(() ->
                newRaiseException("builtins.NameError",
                        "'" + splitter.getName() + "' module is not found")

        )).getObjectOrThrow(splitter.getSimpleName());

        PyObject e = eType.call(eType, eType);

        return new RaiseException(e);
    }

    public RaiseException newRaiseTypeError(String message) {
        PyObject typeErrorType = typeOrThrow("builtins.TypeError");
        return new RaiseException(typeErrorType, message);
    }

    public RaiseException newRaiseException(String exceptionType, String message) {
        ModuleOrClassSplitter splitter = new ModuleOrClassSplitter(exceptionType);

        PyObject type = moduleOrThrow(splitter.getModuleName().orElseThrow(() ->
                newRaiseException("builtins.NameError",
                        "'" + splitter.getName() + "' module is not found")

        )).getObjectOrThrow(splitter.getSimpleName());

        PyObject e = type.call(str(message));

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
