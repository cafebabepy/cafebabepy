package org.cafebabepy.runtime.object;

import org.cafebabepy.runtime.AbstractPyObject;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by yotchang4s on 2017/06/08.
 */
public class PyNoneObject extends AbstractPyObject {
    public PyNoneObject(Python runtime) {
        super(runtime);
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("builtins.NoneType");
    }

    @Override
    public List<PyObject> getSuperTypes() {
        return Arrays.asList(this.runtime.typeOrThrow("builtins.object"));
    }

    @Override
    public Optional<String> getModuleName() {
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
    public boolean isNone() {
        return true;
    }

    @Override
    public String asJavaString() {
        return "None";
    }

    @Override
    public PyObject call(PyObject... args) {
        // Always Exception
        return getCallable().call(args);
    }
}
