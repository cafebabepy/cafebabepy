package org.cafebabepy.runtime.object;

import org.cafebabepy.runtime.AbstractPyObject;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by yotchang4s on 2017/06/03.
 */
public abstract class AbstractPyObjectObject extends AbstractPyObject {

    protected AbstractPyObjectObject(Python runtime) {
        super(runtime, true);
    }

    @Override
    public final List<PyObject> getTypes() {
        return getType().getTypes();
    }

    @Override
    public final List<PyObject> getBases() {
        return getType().getBases();
    }

    @Override
    public final PyObject getModule() {
        return getType().getModule();
    }

    @Override
    public String getName() {
        return getType().getName();
    }

    @Override
    public final boolean isType() {
        return false;
    }

    @Override
    public final boolean isModule() {
        return false;
    }

    @Override
    public boolean isNone() {
        return false;
    }

    @Override
    public boolean isNotImplemented() {
        return false;
    }

    @Override
    public boolean isEllipsis() {
        return false;
    }

    @Override
    public PyObject call(PyObject... args) {
        throw this.runtime.newRaiseTypeError("'" + getFullName() + "' object is not callable");
    }

    @Override
    public PyObject callSubstance(PyObject[] args, LinkedHashMap<String, PyObject> keywords) {
        throw this.runtime.newRaiseTypeError("'" + getFullName() + "' object is not callable");
    }
}
