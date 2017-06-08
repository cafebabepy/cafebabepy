package org.cafebabepy.runtime.object.java;

import org.cafebabepy.runtime.AbstractPyObject;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.PyObjectScope;
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

        this.type = type;
    }

    protected AbstractJavaPyObject(Python runtime, PyObject type, PyObjectScope parentScope) {
        super(runtime, true, parentScope);

        this.type = type;
    }

    @Override
    public final PyObject getType() {
        return this.type;
    }

    @Override
    public List<PyObject> getSuperTypes() {
        return this.type.getSuperTypes();
    }

    @Override
    public final Optional<String> getModuleName() {
        return Optional.empty();
    }

    @Override
    public String asJavaString() {
        int hashCode = System.identityHashCode(this);

        return "0x" + Integer.toHexString(hashCode);
    }
}
