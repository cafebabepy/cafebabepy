package org.cafebabepy.runtime.object;

import org.cafebabepy.runtime.AbstractPyObject;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;

import java.util.List;
import java.util.Optional;

/**
 * Created by yotchang4s on 2017/06/16.
 */
public class PyTrueObject extends AbstractPyObject {

    public PyTrueObject(Python runtime) {
        super(runtime);
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("builtins.bool");
    }

    @Override
    public PyObject getTargetType() {
        return getType();
    }

    @Override
    public List<PyObject> getBases() {
        return getType().getBases();
    }

    @Override
    public Optional<String> getModuleName() {
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
    public boolean isFalse() {
        return false;
    }

    @Override
    public boolean isNone() {
        return false;
    }

    @Override
    public String asJavaString() {
        return "True";
    }

    @Override
    public PyObject call(PyObject self, PyObject... args) {
        return getCallable().call(self, args);
    }
}
