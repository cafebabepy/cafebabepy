package org.cafebabepy.runtime;

import org.cafebabepy.evaluter.Interpret.InterpretEvaluator;
import org.cafebabepy.parser.NormalParser;
import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.runtime.module.PyMainModule;
import org.cafebabepy.runtime.module._ast.PyAstModule;
import org.cafebabepy.runtime.module.builtins.PyBuiltinsModule;
import org.cafebabepy.runtime.module.sys.PySysModule;
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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.cafebabepy.util.ProtocolNames.*;

/**
 * Created by yotchang4s on 2017/05/12.
 */
public final class Python {

    public static final String APPLICATION_NAME = "cafebabepy";

    public static final int MAJOR = 3;
    public static final int MINOR = 6;
    public static final int MICRO = 5;
    public static final String RELEASE_LEVEL = "alpha";
    public static final int SERIAL = 0;

    public static final String VERSION = MAJOR + "." + MINOR + "." + MICRO;

    private volatile boolean initialize = false;

    private NormalParser parser;

    private InterpretEvaluator evaluator;

    private LinkedHashMap<PyObject, PyObject> sysModules;

    private PyNoneObject noneObject;

    private PyBoolObject trueObject;

    private PyBoolObject falseObject;

    private PyEllipsisObject ellipsisObject;

    private PyNotImplementedObject notImplementedObject;

    private Python() {
    }

    public static PyObject eval(String input) {
        Python runtime = Python.createRuntime();
        runtime.initialize();

        PyObject mainModule = runtime.createMainModule();

        runtime.defineModule(mainModule);

        return runtime.evalWithInitialize(mainModule, "<string>", input);
    }

    public static Python createRuntime() {
        return new Python();
    }

    private static PyObject lookupType(PyObject object, String name) {
        for (PyObject type : object.getTypes()) {
            PyObject typeObject = type.getFrame().lookup(name);
            if (typeObject != null) {
                return typeObject;
            }
        }

        return null;
    }

    public PyObject evalWithInitialize(PyObject context, String file, String input) {
        // PyObject traceback = this.evaluator.loadModule("traceback");
        // this.sysModules.put(str("traceback"), traceback);

        return eval(context, file, input);
    }

    public PyObject eval(PyObject context, String file, String input) {
        PyObject ast = this.parser.parse(file, input);
        return this.evaluator.eval(context, ast);
    }

    private PyObject lookup(PyObject object, String name) {
        PyObject attr = object.getFrame().lookup(name);
        if (attr != null) {
            return attr;
        }

        return lookupType(object, name);
    }

    public InterpretEvaluator getEvaluator() {
        return this.evaluator;
    }

    public PyObject evalModule(String file) {
        return getEvaluator().loadModule(file);
    }

    public PyObject createMainModule() {
        PyObject[] moduleRef = new PyObject[1];
        initializeModuleAndTypes(m -> moduleRef[0] = m, PyMainModule.class);

        PyObject module = moduleRef[0];

        module.getFrame().getLocals().put(__builtins__, moduleOrThrow("builtins"));

        return module;
    }

    private void initialize() {
        if (this.initialize) {
            return;
        }
        this.parser = new NormalParser(this);
        this.evaluator = new InterpretEvaluator(this);
        this.sysModules = new LinkedHashMap<>();

        initializeObjects();

        initializeModuleAndTypes(PyBuiltinsModule.class);
        initializeModuleAndTypes(PySysModule.class);
        initializeModuleAndTypes(PyAstModule.class);

        this.initialize = true;
    }

    public void initializeModuleAndTypes(Class<? extends PyObject>... moduleClasses) {
        initializeModuleAndTypes(this::defineModule, moduleClasses);
    }

    @SuppressWarnings("unchecked")
    public void initializeModuleAndTypes(Consumer<PyObject> moduleConsumer, Class<? extends PyObject>... moduleClasses) {
        Map<Class<? extends PyObject>, PyObject> moduleMap = new LinkedHashMap<>();
        for (Class<? extends PyObject> moduleClass : moduleClasses) {
            PyObject module = createJavaPyObject(moduleClass);
            moduleConsumer.accept(module);

            moduleMap.put(moduleClass, module);
        }

        List<PyObject> allTypes = new ArrayList<>();
        for (Class<? extends PyObject> moduleClass : moduleClasses) {
            PyObject module = moduleMap.get(moduleClass);

            List<PyObject> types = defineTypes(module, moduleClass);

            allTypes.addAll(types);
        }

        for (PyObject module : moduleMap.values()) {
            module.initialize();
        }

        for (PyObject type : allTypes) {
            type.initialize();
        }
    }

    @SuppressWarnings("unchecked")
    private List<PyObject> defineTypes(PyObject module, Class<? extends PyObject> moduleClass, Class<? extends PyObject>... ignores) {
        Set<Class<?>> builtinsClasses;
        try {
            builtinsClasses = ReflectionUtils.getClasses(moduleClass.getPackage().getName());

        } catch (IOException e) {
            throw new CafeBabePyException("Fail initialize package '" + moduleClass.getPackage().getName() + "'");
        }

        List<PyObject> types = new ArrayList<>();
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

            if (checkDuplicateTypes.contains(definePyType.name())) {
                throw new CafeBabePyException("Duplicate type '" + definePyType.name() + "'");
            }

            PyObject type = createJavaPyObject((Class<PyObject>) c);
            types.add(type);

            if (definePyType.appear()) {
                module.getFrame().getLocals().put(type.getName(), type);

            } else {
                module.getFrame().getNotAppearLocals().put(type.getName(), type);
            }

            checkDuplicateTypes.add(definePyType.name());
        }

        return types;
    }

    public PyObject createJavaPyObject(Class<? extends PyObject> clazz) {
        try {
            Constructor<? extends PyObject> constructor = clazz.getConstructor(Python.class);
            constructor.setAccessible(true);

            return constructor.newInstance(this);

        } catch (InstantiationException |
                InvocationTargetException |
                NoSuchMethodException |
                IllegalAccessException e) {
            throw new CafeBabePyException(
                    "Fail initialize '" + clazz.getName() + "'", e);
        }
    }

    private void initializeObjects() {
        this.noneObject = new PyNoneObject(this);
        this.trueObject = new PyTrueObject(this);
        this.falseObject = new PyFalseObject(this);
        this.notImplementedObject = new PyNotImplementedObject(this);
        this.ellipsisObject = new PyEllipsisObject(this);

        this.noneObject.initialize();
        this.trueObject.initialize();
        this.falseObject.initialize();
        this.notImplementedObject.initialize();
        this.ellipsisObject.initialize();
    }

    public LinkedHashMap<PyObject, PyObject> getSysModuleMap() {
        return this.sysModules;
    }

    public PyObject str(PyObject value) {
        if (value instanceof PyStrObject) {
            return value;
        }

        if (value.isType()) {
            PyObject type = typeOrThrow("builtins.type");
            return getattr(type, __str__).call(value);

        } else {
            return getattr(value, __str__).call();
        }
    }

    public PyObject repr(PyObject value) {
        PyObject str = getattr(value, __repr__);
        if (value.isType()) {
            return str.call(value);

        } else {
            return str.call();
        }
    }

    public PyObject str(String value) {
        PyStrObject object = new PyStrObject(this, value);
        object.initialize();

        return object;
    }

    public PyObject repr(String value) {
        PyStrObject object = new PyStrObject(this, "'" + value + "'");
        object.initialize();

        return object;
    }

    public PyObject bytes(int[] bytes) {
        return new PyBytesObject(this, bytes);
    }

    public PyObject bytes(byte[] bytes) {
        int[] ints = new int[bytes.length];

        for (int i = 0; i < bytes.length; i++) {
            ints[i] = Byte.toUnsignedInt(bytes[i]);
        }

        return bytes(ints);
    }

    public PyObject format(PyObject value, String formatSpecOpt) {
        // FIXME not dict
        PyObject format = getattr(type(value), __format__);

        return format.call(value, str(formatSpecOpt));
    }

    // FIXME int over max value
    public PyIntObject number(int value) {
        PyIntObject object = new PyIntObject(this, value);
        object.initialize();

        return object;
    }

    // FIXME float over max value
    public PyFloatObject number(float value) {
        PyFloatObject object = new PyFloatObject(this, value);
        object.initialize();

        return object;
    }

    public PyObject tuple(Collection<PyObject> value) {
        PyObject[] array = new PyObject[value.size()];
        value.toArray(array);

        return tuple(array);
    }

    public PyObject tuple(PyObject value) {
        if (isIterable(value)) {
            List<PyObject> list = new ArrayList<>();
            iter(value, list::add);

            return tuple(list);

        } else {
            PyTupleObject object = new PyTupleObject(this, value);
            object.initialize();

            return object;
        }
    }

    public PyObject tuple(PyObject... value) {
        PyTupleObject object = new PyTupleObject(this, value);
        object.initialize();

        return object;
    }

    public PyObject list(Collection<PyObject> value) {
        PyObject[] array = new PyObject[value.size()];
        value.toArray(array);

        return list(array);
    }

    public PyObject list(PyObject... value) {
        PyListObject object = new PyListObject(this, value);
        object.initialize();

        return object;
    }

    public PyObject set(Collection<PyObject> value) {
        PyObject[] array = new PyObject[value.size()];
        value.toArray(array);

        return set(array);
    }

    public PyObject set(PyObject... value) {
        PySetObject object = new PySetObject(this, value);
        object.initialize();

        return object;
    }

    public PyObject dict() {
        return dict(new LinkedHashMap<>());
    }

    public PyObject dictStringKey(LinkedHashMap<String, PyObject> map) {
        LinkedHashMap<PyObject, PyObject> pymap = new LinkedHashMap<>();
        map.forEach((k, v) -> pymap.put(str(k), v));

        return dict(pymap);
    }

    public PyObject dict(Map<PyObject, PyObject> map) {
        PyDictObject object = new PyDictObject(this, map);
        object.initialize();

        return object;
    }

    public PyBoolObject bool(boolean bool) {
        return bool ? this.trueObject : this.falseObject;
    }

    public PyGeneratorObject generator(Function<PyGeneratorObject.YieldStopper, PyObject> iter) {
        PyGeneratorObject object = new PyGeneratorObject(this, iter);
        object.initialize();

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
        return Optional.ofNullable(this.sysModules.get(str(name)));
    }

    public PyObject typeOrThrow(String name) {
        return typeOrThrow(name, true);
    }

    public PyObject typeOrThrow(String name, boolean appear) {
        String[] splitLastDot = StringUtils.splitLastDot(name);

        PyObject module;

        if (StringUtils.isEmpty(splitLastDot[0])) {
            module = moduleOrThrow("builtins");
        } else {
            module = moduleOrThrow(splitLastDot[0]);
        }

        PyObject type;
        if (appear) {
            type = module.getFrame().getLocals().get(splitLastDot[1]);

        } else {
            type = module.getFrame().getNotAppearLocals().get(splitLastDot[1]);
        }

        if (type != null) {
            return type;
        }

        if (StringUtils.isEmpty(splitLastDot[0])) {
            throw newRaiseException("builtins.NameError", "name '" + name + "' is not defined");

        } else {
            throw newRaiseException("builtins.AttributeError",
                    "module '" + module.getName() + "' has no attribute '" + splitLastDot[1] + "'");
        }
    }

    public PyObject type(PyObject object) {
        PyObject type = typeOrThrow("builtins.type");
        return getattr(typeOrThrow("builtins.type"), __call__).call(type, object);
    }

    public Optional<PyObject> type(String name) {
        return type(name, true);
    }

    public Optional<PyObject> type(String name, boolean appear) {
        String[] splitDot = StringUtils.splitLastDot(name);

        if (StringUtils.isEmpty(splitDot[0])) {
            throw new CafeBabePyException("'" + name + "' is not type");
        }

        Optional<PyObject> moduleOpt = module(splitDot[0]);
        if (!moduleOpt.isPresent()) {
            return Optional.empty();
        }

        PyObject module = moduleOpt.get();
        PyObject type;
        if (appear) {
            type = module.getFrame().getLocals().get(splitDot[1]);

        } else {
            type = module.getFrame().getNotAppearLocals().get(splitDot[1]);
        }

        return Optional.ofNullable(type);
    }

    public PyObject newPyObject(String typeName, PyObject... args) {
        return newPyObject(typeName, true, args);
    }

    public PyObject newPyObject(String typeName, boolean appear, PyObject... args) {
        return typeOrThrow(typeName, appear).call(args);
    }

    public PyObject newPyObject(String typeName, PyObject[] args, LinkedHashMap<String, PyObject> kwargs) {
        return newPyObject(typeName, true, args, kwargs);
    }

    public PyObject newPyObject(String typeName, boolean appear, PyObject[] args, LinkedHashMap<String, PyObject> kwargs) {
        return typeOrThrow(typeName, appear).call(args, kwargs);
    }

    private PyObject callFunction(String name, PyObject... args) {
        String[] splitLastDot = StringUtils.splitLastDot(name);
        if (StringUtils.isEmpty(splitLastDot[0])) {
            throw newRaiseException("builtins.NameError",
                    "name '" + splitLastDot[1] + "'is not defined");
        }

        PyObject module = moduleOrThrow(splitLastDot[0]);
        PyObject function = module.getFrame().getLocals().get(splitLastDot[1]);
        if (function == null) {
            throw newRaiseException(
                    "builtins.AttributeError", "module '" + module.getFullName() + "' has no attribute '" + splitLastDot[1] + "'");
        }

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
        List<PyObject> list = listObject.getRawValues();
        for (int i = 0; i < list.size(); i++) {
            action.accept(list.get(i), i);
        }
    }

    public PyObject iter(PyObject object) {
        return getIterType(object);
    }

    public void iter(PyObject object, Consumer<PyObject> action) {
        PyObject next;

        Optional<PyObject> nextOpt = getNext(object);
        if (nextOpt.isPresent()) {
            next = nextOpt.get();

        } else {
            PyObject iterType = getIterType(object);
            PyObject iter = iterType.call();
            next = getIterNext(iter);
        }

        do {
            PyObject value;

            try {
                value = next.call();

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

    public PyObject reversed(PyObject seq) {
        return getattrOptional(seq, __reversed__).map(PyObject::call).orElseThrow(() ->
                newRaiseTypeError("argument to reversed() must be a sequence"));
    }

    public Optional<PyObject> getattrOptional(PyObject object, String name) {
        try {
            return Optional.of(getattr(object, name));

        } catch (RaiseException e) {
            if (isInstance(e.getException(), "builtins.AttributeError")) {
                return Optional.empty();
            }

            throw e;
        }
    }

    public PyObject getattr(PyObject object, String name) {
        PyObject getattribute = lookupType(object.getType(), __getattribute__);
        if (getattribute != null) {
            return getattribute.call(object, str(name));
        }

        // FIXME module
        throw newRaiseException("builtins.AttributeError",
                "module '" + object.getName() + "' has no attribute '" + name + "'");
    }

    public void setattr(PyObject object, String name, PyObject value) {
        PyObject getattr = getattr(object, __setattr__);
        if (object.isType()) {
            getattr.call(object, str(name), value);

        } else {
            getattr.call(str(name), value);
        }
    }

    public void delattr(PyObject object, String name) {
        PyObject delattr = getattr(object, __delattr__);
        if (object.isType()) {
            delattr.call(object, str(name));

        } else {
            delattr.call(str(name));
        }
    }

    public boolean hasattr(PyObject object, String name) {
        try {
            // FIXME remove code
            if (object.getModule().getName().equals("builtins")) {
                if (object.isType()) {
                    return builtins_type__getattribute__(object, name).isPresent();

                } else {
                    return builtins_object__getattribute__(object, name).isPresent();
                }
            }

            getattr(object, name);
            return true;

        } catch (RaiseException e) {
            if (isInstance(e.getException(), "builtins.AttributeError")) {
                return false;
            }

            throw e;
        }
    }

    public Optional<PyObject> getitemOptional(PyObject object, PyObject key) {
        try {
            return Optional.of(getitem(object, key));

        } catch (RaiseException e) {
            if (isInstance(e.getException(), "builtins.KeyError")) {
                return Optional.empty();
            }

            throw e;
        }
    }

    public PyObject getitem(PyObject object, PyObject key) {
        Optional<PyObject> getattrOpt = getattrOptional(object, __getitem__);
        if (!getattrOpt.isPresent()) {
            throw newRaiseTypeError("'" + object.getFullName() + "' object has no attribute '__getitem__'");
        }

        return getattrOpt.get().call(key);
    }

    public void setitem(PyObject object, PyObject key, PyObject value) {
        Optional<PyObject> setattrOpt = getattrOptional(object, __setitem__);
        if (!setattrOpt.isPresent()) {
            throw newRaiseTypeError("'" + object.getFullName() + "' object has no attribute '__setitem__'");
        }

        setattrOpt.get().call(key, value);
    }

    public void delitem(PyObject object, PyObject key) {
        Optional<PyObject> delitemOpt = getattrOptional(object, __delitem__);
        if (!delitemOpt.isPresent()) {
            throw newRaiseTypeError("'" + object.getFullName() + "' object has no attribute '__delitem__'");
        }

        delitemOpt.get().call(key);
    }

    public PyObject contains(PyObject object, PyObject key) {
        Optional<PyObject> containsOpt = getattrOptional(object, __contains__);
        if (!containsOpt.isPresent()) {
            throw newRaiseTypeError("'" + object.getFullName() + "' object has no attribute '__contains__'");
        }

        return containsOpt.get().call(key);
    }

    public PyObject hash(PyObject object) {
        return getattr(object, __hash__).call();
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

    public PyObject len(PyObject object) {
        return callFunction("builtins.len", object);
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

    // //
    public PyObject floorDiv(PyObject x, PyObject y) {
        return refOp(x, y, "//", __floordiv__, __rfloordiv__);
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
        PyObject operator = getattrOptional(x, opFunctionName).orElseThrow(() ->
                newRaiseTypeError("unsupported operand type(s) for " + op + ": '"
                        + x.getType().getFullName()
                        + "' and '"
                        + y.getType().getFullName()
                        + "'")
        );

        PyObject result;
        if (x.isType()) {
            result = operator.call(x, y);

        } else {
            result = operator.call(y);
        }

        if (result.isNotImplemented()) {
            PyObject rop = getattr(y, ropFunctionName);
            if (y.isType()) {
                result = rop.call(y, x);

            } else {
                result = rop.call(x);
            }
        }

        return result;
    }

    private PyObject getIterType(PyObject object) {
        return getattrOptional(object, __iter__).orElseThrow(() ->
                newRaiseException("builtins.TypeError",
                        "'" + object.getName() + "' object is not iterable"));
    }

    private PyObject getIterNext(PyObject iter) {
        return getattrOptional(iter, __next__).orElseThrow(() ->
                newRaiseException("builtins.TypeError",
                        "iter() returned non-iterator of type '" + iter.getType().getName() + "'"));
    }

    private Optional<PyObject> getNext(PyObject object) {
        return getattrOptional(object, __next__);
    }

    public boolean isInstance(PyObject instance, String typeName) {
        return isInstance(instance, typeOrThrow(typeName, true));
    }

    public boolean isInstance(PyObject instance, String typeName, boolean appear) {
        return isInstance(instance, typeOrThrow(typeName, appear));
    }

    public boolean isInstance(PyObject instance, PyObject classInfo) {
        return callFunction("builtins.isinstance", instance, classInfo).isTrue();
    }

    public boolean isSubClass(PyObject instance, String typeName) {
        return isSubClass(instance, typeOrThrow(typeName, true));
    }

    public boolean isSubClass(PyObject instance, String typeName, boolean appear) {
        return isSubClass(instance, typeOrThrow(typeName, appear));
    }

    public boolean isSubClass(PyObject clazz, PyObject classInfo) {
        return callFunction("builtins.issubclass", clazz, classInfo).isTrue();
    }

    public boolean isIterable(PyObject object) {
        return getattrOptional(object, __iter__).isPresent();
    }

    public RaiseException newRaiseTypeError(String message) {
        return newRaiseException("builtins.TypeError", message);
    }

    public RaiseException newRaiseException(String exceptionType) {
        return new RaiseException(newPyObject(exceptionType));
    }

    public RaiseException newRaiseException(String exceptionType, String message) {
        return newRaiseException(exceptionType, str(message));
    }

    public RaiseException newRaiseException(String exceptionType, PyObject... args) {
        PyObject exception = newPyObject(exceptionType, args);
        if (!exception.isException()) {
            throw new CafeBabePyException("'" + exception.getFullName() + "' is not exception");
        }

        return new RaiseException(exception);
    }

    public RaiseException newRaiseException(PyObject exception) {
        if (!exception.isException()) {
            throw new CafeBabePyException("'" + exception.getFullName() + "' is not exception");
        }

        return new RaiseException(exception);
    }

    public void defineModule(PyObject module) {
        if (!module.isModule()) {
            throw new CafeBabePyException("No module named '" + module.getFullName() + "'");
        }

        String[] moduleNames = StringUtils.splitDot(module.getName());

        if (moduleNames.length == 1) {
            this.sysModules.put(str(moduleNames[0]), module);
            return;
        }

        StringBuilder moduleBuilder = new StringBuilder();
        moduleBuilder.append(moduleNames[0]);

        for (int i = 1; i < moduleNames.length; i++) {
            String moduleName = moduleBuilder.toString();
            if (!this.sysModules.containsKey(str(moduleName))) {
                throw new CafeBabePyException("module '" + moduleName + "' is not found");
            }

            moduleBuilder.append('.').append(moduleNames[i]);

            if (i == moduleNames.length - 1) {
                this.sysModules.put(str(moduleBuilder.toString()), module);
            }
        }
    }

    public Optional<PyObject> builtins_object__getattribute__(PyObject self, String key) {
        PyObject type = self.getType();
        PyObject attr = lookup(type, key);
        if (attr != null) {
            if (self != attr) {
                if (hasattr(attr, __get__) && hasattr(attr, __set__)) {
                    if (!__get__.equals(key)) {
                        return Optional.of(getattr(attr, __get__).call(attr, self, type));
                    }
                }
            }
        }

        PyObject object = self.getFrame().lookup(key);
        if (object != null) {
            return Optional.of(object);
        }

        if (attr != null) {
            if (self != attr) {
                if (hasattr(attr, __get__) && !__get__.equals(key)) {
                    return Optional.of(getattr(attr, __get__).call(attr, self, type));
                }
            }

            return Optional.of(attr);
        }

        return Optional.empty();
    }

    public Optional<PyObject> builtins_type__getattribute__(PyObject cls, String key) {
        PyObject meta = cls.getType();
        PyObject metaattr = lookup(meta, key);
        if (metaattr != null) {
            if (cls != metaattr) {
                if (hasattr(metaattr, __get__) && hasattr(metaattr, __set__)) {
                    if (!__get__.equals(key)) {
                        return Optional.of(getattr(metaattr, __get__).call(metaattr, cls, meta));
                    }
                }
            }
        }

        PyObject attr = lookup(cls, key);
        if (attr != null) {
            if (cls != attr) {
                if (hasattr(attr, __get__) && !__get__.equals(key)) {
                    return Optional.of(getattr(attr, __get__).call(attr, None(), cls));
                }
            }

            return Optional.of(attr);
        }

        if (metaattr != null) {
            if (cls != metaattr) {
                if (hasattr(metaattr, __get__) && !__get__.equals(key)) {
                    return Optional.of(getattr(metaattr, __get__).call(metaattr, cls, meta));
                }
            }

            return Optional.of(metaattr);
        }

        throw newRaiseException("builtins.AttributeError",
                "type object '" + cls.getName() + "' object has no attribute '" + key + "'");
    }
}
