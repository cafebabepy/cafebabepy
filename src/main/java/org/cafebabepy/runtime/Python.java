package org.cafebabepy.runtime;

import org.cafebabepy.annotation.DefinePyModule;
import org.cafebabepy.annotation.DefinePyType;
import org.cafebabepy.runtime.module.PyMainModule;
import org.cafebabepy.runtime.module._ast.PyAstModule;
import org.cafebabepy.runtime.module.builtins.PyBuiltinsModule;
import org.cafebabepy.runtime.module.builtins.PyObjectType;
import org.cafebabepy.runtime.module.builtins.PyTypeType;
import org.cafebabepy.runtime.module.types.PyMethodTypeType;
import org.cafebabepy.runtime.module.types.PyTypesModule;
import org.cafebabepy.runtime.object.iterator.PyGeneratorObject;
import org.cafebabepy.runtime.object.java.*;
import org.cafebabepy.runtime.object.literal.PyEllipsisObject;
import org.cafebabepy.runtime.object.literal.PyNoneObject;
import org.cafebabepy.runtime.object.literal.PyNotImplementedObject;
import org.cafebabepy.util.ReflectionUtils;
import org.cafebabepy.util.StringUtils;

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

    public static final String VERSION = "3.6.2.0";

    public static final String APPLICATION_NAME = "cafebabepy";

    // FIXME sys.modulesに持って行きたい
    private Map<String, PyObject> moduleMap;

    private PyNoneObject noneObject;

    private PyBoolObject trueObject;

    private PyBoolObject falseObject;

    private PyEllipsisObject ellipsisObject;

    private PyNotImplementedObject notImplementedObject;

    private Python() {
        this.moduleMap = new ConcurrentHashMap<>();
    }

    public static Python createRuntime() {
        Python runtime = new Python();
        runtime.initialize();

        return runtime;
    }

    private void initialize() {
        initializeBootstrap();

        initializeModuleAndTypes(PyTypesModule.class);
        initializeModuleAndTypes(PyMainModule.class);
        initializeModuleAndTypes(PyAstModule.class);

        initializeObjects();

        // TODO __main__でいいの？
        PyObject builtinsModule = moduleOrThrow("builtins");
        PyObject mainModule = moduleOrThrow("__main__");

        Map<String, PyObject> objectMap = builtinsModule.getScope().gets();
        for (Map.Entry<String, PyObject> e : objectMap.entrySet()) {
            mainModule.getScope().put(e.getKey(), e.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private void initializeBootstrap() {
        Class<PyBuiltinsModule> builtinsModuleClass = PyBuiltinsModule.class;
        DefinePyModule defineBuiltinsModule = builtinsModuleClass.getAnnotation(DefinePyModule.class);
        if (defineBuiltinsModule == null) {
            throw new CafeBabePyException("'" + builtinsModuleClass.getName() + "' module is not found");
        }
        PyObject builtinsModule = createType(builtinsModuleClass, defineBuiltinsModule.name());

        Class<PyObjectType> objectClass = PyObjectType.class;
        DefinePyType defineObjectType = objectClass.getAnnotation(DefinePyType.class);
        if (defineObjectType == null) {
            throw new CafeBabePyException("'" + objectClass.getName() + "' type is not found");
        }
        PyObject objectType = createType(objectClass, defineObjectType.name());

        Class<PyTypeType> typeClass = PyTypeType.class;
        DefinePyType defineTypeType = typeClass.getAnnotation(DefinePyType.class);
        if (defineTypeType == null) {
            throw new CafeBabePyException("'" + typeClass.getName() + "' type is not found");
        }
        PyObject typeType = createType(typeClass, defineTypeType.name());

        Class<PyTypesModule> typesModuleClass = PyTypesModule.class;
        DefinePyModule defineTypesModule = typesModuleClass.getAnnotation(DefinePyModule.class);
        if (defineTypesModule == null) {
            throw new CafeBabePyException("'" + typesModuleClass.getName() + "' module is not found");
        }
        PyObject typesModule = createType(typesModuleClass, defineTypesModule.name());


        Class<PyMethodTypeType> methodTypeClass = PyMethodTypeType.class;
        DefinePyType defineMethodTypeType = methodTypeClass.getAnnotation(DefinePyType.class);
        if (defineMethodTypeType == null) {
            throw new CafeBabePyException("'" + methodTypeClass.getName() + "' type is not found");
        }
        PyObject methodTypeType = createType(methodTypeClass, defineMethodTypeType.name());

        builtinsModule.preInitialize();
        objectType.preInitialize();
        typeType.preInitialize();
        typesModule.preInitialize();
        methodTypeType.preInitialize();

        typeType.postInitialize();
        objectType.postInitialize();
        builtinsModule.postInitialize();
        typesModule.postInitialize();
        methodTypeType.postInitialize();

        initializeTypes(builtinsModuleClass, PyTypeType.class);
        initializeTypes(typesModuleClass, PyMethodTypeType.class);
    }

    @SuppressWarnings("unchecked")
    private void initializeModuleAndTypes(Class<? extends PyObject> moduleClass) {
        DefinePyModule definePyModule = moduleClass.getAnnotation(DefinePyModule.class);
        if (definePyModule == null) {
            throw new CafeBabePyException("'" + moduleClass.getName() + "' module is not found");
        }

        PyObject module = createType(moduleClass, definePyModule.name());
        module.preInitialize();
        initializeTypes(moduleClass);
        module.postInitialize();
    }

    @SuppressWarnings("unchecked")
    private void initializeTypes(Class<? extends PyObject> module, Class<? extends PyObject>... ignores) {
        Set<Class<?>> builtinsClasses;
        try {
            builtinsClasses = ReflectionUtils.getClasses(module.getPackage().getName());

        } catch (IOException e) {
            throw new CafeBabePyException("Fail initialize package '" + module.getPackage().getName() + "'");
        }

        Set<PyObject> types = new HashSet<>();
        Set<String> checkDuplicateTypes = new HashSet<>();

        BuiltinsClass:
        for (Class<?> c : builtinsClasses) {
            DefinePyType definePyType = c.getAnnotation(DefinePyType.class);
            if (definePyType == null || !PyObject.class.isAssignableFrom(c)) {
                continue;
            }
            for (Class<? extends PyObject> ignore : ignores) {
                if (c.equals(ignore)) {
                    continue BuiltinsClass;
                }
            }

            Class<PyObject> clazz = (Class<PyObject>) c;

            PyObject type = createType(clazz, definePyType.name());
            type.preInitialize();

            if (checkDuplicateTypes.contains(definePyType.name())) {
                throw new CafeBabePyException("Duplicate type '" + definePyType.name() + "'");
            }

            types.add(type);

            checkDuplicateTypes.add(definePyType.name());
        }

        for (PyObject type : types) {
            type.postInitialize();
        }
    }

    private PyObject createType(Class<? extends PyObject> clazz, String typeFullName) {
        try {
            Constructor<? extends PyObject> constructor = clazz.getConstructor(Python.class);
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
        this.noneObject = new PyNoneObject(this);
        this.trueObject = new PyTrueObject(this);
        this.falseObject = new PyFalseObject(this);
        this.notImplementedObject = new PyNotImplementedObject(this);
        this.ellipsisObject = new PyEllipsisObject(this);
    }

    public PyObject str(PyObject value) {
        if (value instanceof PyStrObject) {
            return value;
        }

        if (value.isType()) {
            PyObject strFunc = typeOrThrow("builtins.object").getScope().getOrThrow(__str__);
            return strFunc.call(value);

        } else {
            PyObject strMethod = value.getScope().getOrThrow(__str__);
            return strMethod.call();
        }
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

    public PyObject None() {
        return this.noneObject;
    }

    public PyObject Ellipsis() {
        return this.ellipsisObject;
    }

    public PyObject True() {
        return this.trueObject;
    }

    public PyObject False() {
        return this.falseObject;
    }

    public PyObject NotImplemented() {
        return this.notImplementedObject;
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
        String[] splitLastDot = StringUtils.splitLastDot(name);

        if (StringUtils.isEmpty(splitLastDot[0])) {
            throw new CafeBabePyException("'" + name + "' is not type");
        }

        return moduleOrThrow(splitLastDot[0])
                .getScope()
                .getThisOnlyOrThrow(splitLastDot[1], appear);
    }

    public Optional<PyObject> type(String name) {
        return type(name, true);
    }

    public Optional<PyObject> type(String name, boolean appear) {
        String[] splitDot = StringUtils.splitLastDot(name);

        if (StringUtils.isEmpty(splitDot[0])) {
            throw new CafeBabePyException("'" + name + "' is not type");

        }

        return module(splitDot[0])
                .map(PyObject::getScope)
                .flatMap(scope -> scope.getThisOnly(splitDot[1], appear));
    }

    public PyObject newPyObject(String typeName, PyObject... args) {
        return newPyObject(typeName, true, args);
    }

    public PyObject newPyObject(String typeName, boolean appear, PyObject... args) {
        return typeOrThrow(typeName, appear).call(args);
    }

    public PyObject callFunction(String name, PyObject... args) {
        String[] splitLastDot = StringUtils.splitLastDot(name);
        if (StringUtils.isEmpty(splitLastDot[0])) {
            throw newRaiseException("builtins.NameError",
                    "name '" + splitLastDot[1] + "'is not defined");
        }

        PyObject function = moduleOrThrow(splitLastDot[0])
                .getScope()
                .getOrThrow(splitLastDot[1]);

        return function.call(args);
    }

    public void iterIndex(PyObject object, BiConsumer<PyObject, Integer> action) {

        if (object instanceof PyListObject) {
            iterIndex((PyListObject) object, action);
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
        for (int i = 0; i < list.size(); i++) {
            action.accept(list.get(i), i);
        }
    }

    public void iter(PyObject object, Consumer<PyObject> action) {

        if (object instanceof PyListObject) {
            iter((PyListObject) object, action);
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
        for (int i = 0; i < list.size(); i++) {
            action.accept(list.get(i));
        }
    }

    public PyObject getattr(PyObject object, String name) {
        try {
            return object.getScope().getOrThrow(name);

        } catch (RaiseException e) {
            Optional<PyObject> getattrOpt = object.getType().getScope().get(__getattr__);
            if (getattrOpt.isPresent()) {
                PyObject getattr = getattrOpt.get();
                return getattr.call(object, str(name));

            } else {
                throw e;
            }
        }
    }

    public boolean hasattr(PyObject object, String name) {
        try {
            getattr(object, name);
            return true;

        } catch (RaiseException e) {
            if (isInstance(e.getException(), "builtins.AttributeError")) {
                return false;
            }

            throw e;
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
        PyObject operator = x.getScope().get(opFunctionName).orElseThrow(() ->
                newRaiseTypeError("unsupported operand type(s) for " + op + ": '"
                        + x.getType().getFullName()
                        + "' and '"
                        + y.getType().getFullName()
                        + "'")
        );
        PyObject result = operator.call(y);

        if (result.isNotImplemented()) {
            PyObject rop = y.getScope().getOrThrow(ropFunctionName);
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
        return object.getType().getScope().get(__iter__).orElseThrow(() ->
                newRaiseException("builtins.TypeError",
                        "'" + object.getName() + "' object is not iterable"));
    }

    private PyObject getIterNext(PyObject iter) {
        return iter.getType().getScope().get(__next__).orElseThrow(() ->
                newRaiseException("builtins.TypeError",
                        "iter() returned non-iterator of type '" + iter.getType().getName() + "'"));
    }

    private Optional<PyObject> getNext(PyObject object) {
        return object.getType().getScope().get(__next__);
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
        return object.getScope().get(__iter__).map(x -> true).orElse(false);
    }

    public RaiseException newRaiseTypeError(String message) {
        return newRaiseException("builtins.TypeError", message);
    }

    public RaiseException newRaiseException(String exceptionType) {
        PyObject e = newPyObject(exceptionType);

        return new RaiseException(e);
    }

    public RaiseException newRaiseException(String exceptionType, String message) {
        PyObject e = newPyObject(exceptionType, str(message));

        return new RaiseException(e, message);
    }

    public void defineModule(PyObject module) {
        if (!module.isModule()) {
            throw new CafeBabePyException("No module named '" + module.getFullName() + "'");
        }

        String[] moduleNames = StringUtils.splitDot(module.getName());

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
