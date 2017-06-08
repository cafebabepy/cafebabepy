package org.cafebabepy.runtime.object.java;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.PyObjectScope;
import org.cafebabepy.runtime.Python;

import java.util.Optional;

/**
 * Created by yotchang4s on 2017/06/04.
 */
public class JavaPyObject extends AbstractJavaPyObject {

    public JavaPyObject(Python runtime, PyObject type) {
        super(runtime, () -> Optional.ofNullable(type));
    }

    public JavaPyObject(Python runtime, PyObject type, PyObjectScope parentScope) {
        super(runtime, () -> Optional.ofNullable(type), parentScope);
    }

    @Override
    public PyObject call(PyObject... args) {
        return getCallable().call(args);
    }

    @Override
    public boolean isNone() {
        return false;
    }
}
