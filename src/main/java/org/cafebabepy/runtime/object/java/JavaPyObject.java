package org.cafebabepy.runtime.object.java;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.PyObjectScope;
import org.cafebabepy.runtime.Python;

/**
 * Created by yotchang4s on 2017/06/04.
 */
public class JavaPyObject extends AbstractJavaPyObject {

    protected JavaPyObject(Python runtime, PyObject type) {
        super(runtime, type);
    }

    protected JavaPyObject(Python runtime, PyObject type, PyObjectScope parentScope) {
        super(runtime, type, parentScope);
    }
}
