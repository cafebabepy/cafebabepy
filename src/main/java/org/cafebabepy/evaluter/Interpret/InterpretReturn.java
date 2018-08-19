package org.cafebabepy.evaluter.Interpret;

import org.cafebabepy.runtime.PyObject;

/**
 * Created by yotchang4s on 2017/05/13.
 */
class InterpretReturn extends RuntimeException {
    final PyObject value;

    InterpretReturn(PyObject value) {
        this.value = value;
    }
}
