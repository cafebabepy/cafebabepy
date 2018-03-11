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
    private final boolean appear;
    private volatile PyObjectScope scope;
    private volatile List<PyObject> types;

    protected AbstractPyObject(Python runtime) {
        this(runtime, true);
    }

    protected AbstractPyObject(Python runtime, boolean appear) {
        this.runtime = runtime;
        this.appear = appear;
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

                    boolean tailContains = false;

                    for (PyObject te : tail) {
                        if (te == h) {
                            tailContains = true;
                            break;
                        }
                    }

                    inNotTail &= !tailContains;
                }
            }

            if (inNotTail) {
                for (List<PyObject> list : listOfLinearization) {
                    // Remove head
                    if (h == list.get(0)) {
                        list.removeIf(e -> e == h);
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
                    this.scope = new PyObjectScope();
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
    public void initialize() {
    }

    @Override
    public final boolean isAppear() {
        return this.appear;
    }

    @Override
    public final boolean isCallable() {
        return getScope().containsKey(__call__);
    }

    @Override
    public <T> T toJava(Class<T> clazz) {
        throw new CafeBabePyException("'" + getClass().getName() + "#toJava' not support '" + clazz.getName() + "'");
    }

    @Override
    public final boolean isException() {
        PyObject baseExceptionType = this.runtime.typeOrThrow("builtins.BaseException");

        return this.runtime.isSubClass(getType(), baseExceptionType);
    }

    @Override
    public final boolean isTrue() {
        return !isFalse();
    }

    @Override
    public boolean isFalse() {
        Optional<PyObject> boolOpt = this.runtime.getattrOptional(this, __bool__);
        if (boolOpt.isPresent()) {
            PyObject bool = boolOpt.get();
            PyObject result = bool.call(this);
            return result.isFalse();
        }

        Optional<PyObject> lenOpt = this.runtime.getattrOptional(this, __len__);
        if (lenOpt.isPresent()) {
            PyObject len = lenOpt.get();
            PyObject result = len.call(this);

            return this.runtime.eq(result, this.runtime.number(0)).isTrue();
        }

        return false;
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

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;

        } else if (!(other instanceof PyObject)) {
            return false;

        } else {
            return this.runtime.eq(this, (PyObject) other).isTrue();
        }
    }

    @Override
    public int hashCode() {
        return this.runtime.hash(this).toJava(int.class);
    }
}
