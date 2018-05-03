package org.cafebabepy.runtime.object.java;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.AbstractPyObjectObject;

/**
 * Created by yotchang4s on 2017/06/23.
 */
public abstract class PyBoolObject extends PyIntObject {

    PyBoolObject(Python runtime, boolean bool) {
        super(runtime, bool ? 1 : 0);
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("builtins.bool");
    }
}
