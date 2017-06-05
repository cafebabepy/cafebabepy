package org.cafebabepy.runtime.object.java;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.PyObjectScope;
import org.cafebabepy.runtime.Python;

/**
 * Created by yotchang4s on 2017/06/04.
 */
public class JavaPyObject extends AbstractJavaPyObject {

    public JavaPyObject(Python runtime, PyObject type) {
        super(runtime, type);
    }

    public JavaPyObject(Python runtime, PyObject type, PyObjectScope parentScope) {
        super(runtime, type, parentScope);
    }

    @Override
    public PyObject call(PyObject... args) {
        return getObject("__call__").orElseThrow(
                () -> this.runtime.newRaiseException("builtins.TypeError",
                        "'" + getType().getName() + "' object is not callabl"))
                .call(args);
    }
}
