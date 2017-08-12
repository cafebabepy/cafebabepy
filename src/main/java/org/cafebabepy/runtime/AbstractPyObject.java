package org.cafebabepy.runtime;

import org.cafebabepy.runtime.module.builtins.PyObjectType;

import java.util.*;
import java.util.stream.Collectors;

import static org.cafebabepy.util.ProtocolNames.*;

/**
 * Created by yotchang4s on 2017/06/08.
 */
public abstract class AbstractPyObject implements PyObject {

    protected final Python runtime;

    private volatile PyObjectScope scope;

    private final boolean appear;

    private volatile List<PyObject> types;

    protected AbstractPyObject(Python runtime) {
        this(runtime, true);
    }

    protected AbstractPyObject(Python runtime, boolean appear) {
        this.runtime = runtime;
        this.appear = appear;
    }

    @Override
    public List<PyObject> getTypes() {
        if (this.types == null) {
            synchronized (this) {
                if (this.types == null) {
                    this.types = getC3AlgorithmTypes();
                    this.types = Collections.unmodifiableList(
                            Collections.synchronizedList(this.types));
                }
            }
        }

        return this.types;
    }

    @Override
    public final Python getRuntime() {
        return this.runtime;
    }

    @Override
    public PyObjectScope getScope() {
        if (this.scope == null) {
            synchronized (this) {
                if (this.scope == null) {
                    this.scope = new PyObjectScope(this);
                }
            }
        }

        return this.scope;
    }

    @Override
    public final String getFullName() {
        if (isModule()) {
            return getName();

        } else {
            return getModule().getName() + "." + getName();
        }
    }

    @Override
    public void preInitialize() {
    }

    @Override
    public void postInitialize() {
    }

    @Override
    public boolean isAppear() {
        return this.appear;
    }

    @Override
    public final PyObject getStr() {
        return this.runtime.str(asJavaString());
    }

    @Override
    public final boolean isException() {
        // FIXME 親がBaseExceptionかどうかを判定する
        PyObject module = this.runtime.typeOrThrow("builtins");
        PyObject call = module.getScope().getOrThrow("issubclass");

        PyObject baseExceptionType = module.getScope().getOrThrow("BaseException");

        return call.call(getType(), baseExceptionType).isTrue();
    }

    @Override
    public final boolean isTrue() {
        return !isFalse();
    }

    @Override
    public boolean isFalse() {
        Optional<PyObject> boolOpt = getScope().get(__bool__);
        if (boolOpt.isPresent()) {
            PyObject bool = boolOpt.get();
            PyObject result = bool.call(getType(), this);
            return result.isFalse();
        }

        Optional<PyObject> lenOpt = getScope().get(__len__);
        if (lenOpt.isPresent()) {
            PyObject len = lenOpt.get();
            PyObject result = len.call(len, this);

            return this.runtime.eq(result, this.runtime.number(0)).isTrue();
        }

        if (!isType()) {
            getType().isFalse();
        }

        return true;
    }

    public final Optional<PyObject> type(String name) {
        return this.runtime.type(name);
    }

    public final Optional<PyObject> type(String name, boolean appear) {
        return this.runtime.type(name, appear);
    }

    public final PyObject typeOrThrow(String name) {
        return getRuntime().typeOrThrow(name);
    }

    public final PyObject typeOrThrow(String name, boolean appear) {
        return getRuntime().typeOrThrow(name, appear);
    }

    @Override
    public final PyObject getCallable() {
        PyObject callable = null;
        for (PyObject type : getTypes()) {
            Optional<PyObject> callableOpt = type.getScope().get(__call__);
            if (callableOpt.isPresent()) {
                callable = callableOpt.get();
                break;
            }
        }

        if (callable != null) {
            return callable;
        }

        // FIXME getName()が必ずobjectになる
        throw this.runtime.newRaiseTypeError("'" + getName() + "' object is not callable");
    }

    @Override
    public PyObject call() {
        PyObject[] objects = new PyObject[0];

        return call(objects);
    }

    @Override
    public PyObject call(PyObject arg1) {
        PyObject[] objects = new PyObject[1];
        objects[0] = arg1;

        return call(objects);
    }

    @Override
    public PyObject call(PyObject arg1,
                         PyObject arg2) {
        PyObject[] objects = new PyObject[2];
        objects[0] = arg1;
        objects[1] = arg2;

        return call(objects);
    }

    @Override
    public PyObject call(PyObject arg1,
                         PyObject arg2,
                         PyObject arg3) {
        PyObject[] objects = new PyObject[3];
        objects[0] = arg1;
        objects[1] = arg2;
        objects[2] = arg3;

        return call(objects);
    }

    @Override
    public PyObject call(PyObject arg1,
                         PyObject arg2,
                         PyObject arg3,
                         PyObject arg4) {
        PyObject[] objects = new PyObject[4];
        objects[0] = arg1;
        objects[1] = arg2;
        objects[2] = arg3;
        objects[3] = arg4;

        return call(objects);
    }

    @Override
    public PyObject call(PyObject arg1,
                         PyObject arg2,
                         PyObject arg3,
                         PyObject arg4,
                         PyObject arg5) {
        PyObject[] objects = new PyObject[5];
        objects[0] = arg1;
        objects[1] = arg2;
        objects[2] = arg3;
        objects[3] = arg4;
        objects[4] = arg5;

        return call(objects);
    }

    @Override
    public PyObject call(PyObject arg1,
                         PyObject... args) {
        PyObject[] objects = new PyObject[args.length + 1];
        objects[0] = arg1;
        System.arraycopy(args, 0, objects, 1, args.length);

        return call(objects);
    }

    @Override
    public PyObject call(PyObject arg1,
                         PyObject arg2,
                         PyObject... args) {
        PyObject[] objects = new PyObject[args.length + 2];
        objects[0] = arg1;
        objects[1] = arg2;
        System.arraycopy(args, 0, objects, 2, args.length);

        return call(objects);
    }

    @Override
    public PyObject call(PyObject arg1,
                         PyObject arg2,
                         PyObject arg3,
                         PyObject... args) {
        PyObject[] objects = new PyObject[args.length + 3];
        objects[0] = arg1;
        objects[1] = arg2;
        objects[2] = arg3;
        System.arraycopy(args, 0, objects, 3, args.length);

        return call(objects);
    }

    @Override
    public PyObject call(PyObject arg1,
                         PyObject arg2,
                         PyObject arg3,
                         PyObject arg4,
                         PyObject... args) {
        PyObject[] objects = new PyObject[args.length + 4];
        objects[0] = arg1;
        objects[1] = arg2;
        objects[2] = arg3;
        objects[3] = arg4;
        System.arraycopy(args, 0, objects, 4, args.length);

        return call(objects);
    }

    @Override
    public PyObject call(PyObject arg1,
                         PyObject arg2,
                         PyObject arg3,
                         PyObject arg4,
                         PyObject arg5,
                         PyObject... args) {
        PyObject[] objects = new PyObject[args.length + 5];
        objects[0] = arg1;
        objects[1] = arg2;
        objects[2] = arg3;
        objects[3] = arg4;
        objects[4] = arg5;
        System.arraycopy(args, 0, objects, 5, args.length);

        return call(objects);
    }

    private List<PyObject> getC3AlgorithmTypes() {
        try {
            return getC3AlgorithmTypes(this);

        } catch (CafeBabePyException e) {
            throw this.runtime.newRaiseTypeError(e.getMessage());
        }
    }

    private static List<PyObject> getC3AlgorithmTypes(PyObject object) {
        if (!object.isType() && !object.isModule()) {
            throw new CafeBabePyException("'" + object.getName() + "' is not type");
        }
        List<PyObject> result = new ArrayList<>();
        result.add(object);

        if (object instanceof PyObjectType) {
            return result;
        }

        List<PyObject> bases = new LinkedList<>(object.getBases());

        List<List<PyObject>> listOfLinearization = new ArrayList<>();

        for (PyObject base : bases) {
            listOfLinearization.add(getC3AlgorithmTypes(base));
        }
        listOfLinearization.add(bases);

        try {
            result.addAll(merge(listOfLinearization));

        } catch (CafeBabePyException ignore) {
            String baseNames = bases.stream()
                    .map(PyObject::getName)
                    .collect(Collectors.joining(", "));

            throw new CafeBabePyException("Cannot create a consistent method resolution"
                    + System.lineSeparator() + "order (MRO) for bases " + baseNames);
        }

        return result;
    }

    private static List<PyObject> merge(List<List<PyObject>> listOfLinearization) {
        for (List<PyObject> il : listOfLinearization) {
            PyObject h = il.get(0);

            boolean inNotTail = true;
            for (List<PyObject> jl : listOfLinearization) {
                if (!jl.isEmpty()) {
                    List<PyObject> tail = jl.subList(1, jl.size());
                    inNotTail &= !tail.contains(h);
                }
            }

            if (inNotTail) {
                for (List<PyObject> list : listOfLinearization) {
                    // Remove head
                    if (h == list.get(0)) {
                        list.remove(h);
                    }
                }

                List<List<PyObject>> listOfStripped = new ArrayList<>();

                for (List<PyObject> list : listOfLinearization) {
                    if (!list.isEmpty()) {
                        listOfStripped.add(list);
                    }
                }

                List<PyObject> result = new LinkedList<>();
                result.add(h);

                if (!listOfStripped.isEmpty()) {
                    result.addAll(merge(listOfStripped));
                }

                return result;
            }
        }

        throw new CafeBabePyException();
    }
}
