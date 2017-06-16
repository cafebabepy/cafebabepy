package org.cafebabepy.runtime.object.java;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;

/**
 * Created by yotchang4s on 2017/06/04.
 */
public class JavaPyObject extends AbstractJavaPyObject {

    public JavaPyObject(Python runtime, PyObject type) {
        super(runtime, type);
    }

    @Override
    public boolean isNone() {
        return false;
    }

    @Override
    public PyObject call(PyObject self, PyObject... args) {
        return getCallable().call(self, args);
    }
}
