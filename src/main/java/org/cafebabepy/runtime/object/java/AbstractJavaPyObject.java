package org.cafebabepy.runtime.object.java;

import org.cafebabepy.runtime.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Created by yotchang4s on 2017/06/03.
 */
abstract class AbstractJavaPyObject extends AbstractPyObject {

    protected final Supplier<Optional<PyObject>> typeReadAccessor;

    protected AbstractJavaPyObject(Python runtime, Supplier<Optional<PyObject>> typeReadAccessor) {
        super(runtime, true);

        this.typeReadAccessor = typeReadAccessor;
    }

    protected AbstractJavaPyObject(Python runtime, Supplier<Optional<PyObject>> typeReadAccessor, PyObjectScope parentScope) {
        super(runtime, true, parentScope);

        this.typeReadAccessor = typeReadAccessor;
    }

    @Override
    public final PyObject getType() {
        return this.typeReadAccessor.get().orElseThrow(() ->
                this.runtime.newRaiseException("builtins.TypeError", "type is not found")
        );
    }

    @Override
    public List<PyObject> getSuperTypes() {
        return getType().getSuperTypes();
    }

    @Override
    public final Optional<String> getModuleName() {
        return Optional.empty();
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
