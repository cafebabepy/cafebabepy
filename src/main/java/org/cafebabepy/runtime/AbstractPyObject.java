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
    private volatile Frame frame;
    private volatile List<PyObject> types;
    private volatile boolean dict;

    protected AbstractPyObject(Python runtime) {
        this(runtime, true);
    }

    protected AbstractPyObject(Python runtime, boolean dict) {
        this.runtime = runtime;
        this.dict = dict;
    }

    private static List<PyObject> getC3AlgorithmTypes(PyObject object) {
        if (!object.isType() && !object.isModule()) {
            throw new CafeBabePyException("'" + object.getName() + "' is not type");
        }
        if (object.isModule()) {
            object = object.getType();
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
    public boolean existsDict() {
        return this.dict;
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
    public Frame getFrame() {
        if (this.frame == null) {
            synchronized (this) {
                if (this.frame == null) {
                    if (isModule()) {
                        this.frame = new Frame();

                    } else {
                        this.frame = new Frame(getModule().getFrame());
                    }
                }
            }
        }

        return this.frame;
    }

    @Override
    public final String getFullName() {
        if (isModule() || "builtins".equals(getModule().getName())) {
            return getName();

        } else {
            return getModule().getName() + "." + getName();
        }
    }

    @Override
    public void initialize() {
    }

    @Override
    public final boolean isCallable() {
        return getFrame().getLocals().containsKey(__call__);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T toJava(Class<T> clazz) {
        if (clazz == String.class) {
            return (T) this.runtime.str(this).toJava(String.class);

        } else if (clazz == List.class) {
            if (this.runtime.isIterable(this)) {
                List<PyObject> result = new ArrayList<>();
                this.runtime.iter(this, result::add);

                return (T) result;
            }
        }

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
            PyObject result = bool.call();
            return result.isFalse();
        }

        Optional<PyObject> lenOpt = this.runtime.getattrOptional(this, __len__);
        if (lenOpt.isPresent()) {
            PyObject len = lenOpt.get();
            PyObject result = len.call();

            return this.runtime.eq(result, this.runtime.number(0)).isTrue();
        }

        return false;
    }

    @Override
    public PyObject call(PyObject... args) {
        return call(args, new LinkedHashMap<>());
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

    @Override
    public String toString() {
        return toJava(String.class);
    }
}
