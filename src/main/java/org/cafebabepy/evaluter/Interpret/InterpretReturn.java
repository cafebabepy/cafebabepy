package org.cafebabepy.evaluter.Interpret;

import org.cafebabepy.runtime.PyObject;

/**
 * Created by yotchang4s on 2017/05/13.
 */
public final class InterpretReturn extends RuntimeException {
    private final PyObject value;

    public InterpretReturn(PyObject value) {
        this.value = value;
    }

    public PyObject getValue() {
        return this.value;
    }
}
