package org.cafebabepy.runtime.object;

import org.cafebabepy.runtime.AbstractPyObject;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;

import java.util.List;
import java.util.Optional;

/**
 * Created by yotchang4s on 2017/06/03.
 */
abstract class AbstractPyObjectObject extends AbstractPyObject {

    protected final PyObject type;

    private volatile String string;

    protected AbstractPyObjectObject(Python runtime, PyObject type) {
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
    public final List<PyObject> getTypes() {
        return getType().getTypes();
    }

    @Override
    public final List<PyObject> getBases() {
        return getType().getBases();
    }

    @Override
    public final Optional<String> getModuleName() {
        return getType().getModuleName();
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
    public String asJavaString() {
        if (this.string == null) {
            synchronized (this) {
                int hashCode = System.identityHashCode(this);
                this.string = "<" + getFullName() + " object at 0x" + Integer.toHexString(hashCode) + ">";
            }
        }

        return this.string;
    }

    @Override
    public PyObject call(PyObject self, PyObject... args) {
        return getCallable().call(self, args);
    }
}
