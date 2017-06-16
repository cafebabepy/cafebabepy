package org.cafebabepy.runtime.object.java;

import org.cafebabepy.runtime.AbstractPyObject;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;

import java.util.List;
import java.util.Optional;

/**
 * Created by yotchang4s on 2017/06/03.
 */
abstract class AbstractJavaPyObject extends AbstractPyObject {

    protected final PyObject type;

    protected AbstractJavaPyObject(Python runtime, PyObject type) {
        super(runtime, true);

        if (!type.isType()) {
            this.runtime.newRaiseTypeError("'" + type.getFullName() + "' is not type");
        }

        this.type = type;
    }

    @Override
    public final PyObject getType() {
        return this.type;
    }

    @Override
    public PyObject getTargetType() {
        return super.getTargetType();
    }

    @Override
    public List<PyObject> getTypes() {
        return getType().getTypes();
    }

    @Override
    public List<PyObject> getBases() {
        return getType().getBases();
    }

    @Override
    public final Optional<String> getModuleName() {
        return Optional.empty();
    }

    @Override
    public String getName() {
        return getType().getName();
    }

    @Override
    public boolean isType() {
        return false;
    }

    @Override
    public boolean isModule() {
        return false;
    }

    @Override
    public String asJavaString() {
        int hashCode = System.identityHashCode(this);

        return "0x" + Integer.toHexString(hashCode);
    }
}
