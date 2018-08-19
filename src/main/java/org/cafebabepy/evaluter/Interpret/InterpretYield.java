package org.cafebabepy.evaluter.Interpret;

import org.cafebabepy.runtime.PyObject;

/**
 * Created by yotchang4s on 2018/08/14.
 */
final class InterpretYield extends RuntimeException {
    final PyObject value;

    InterpretYield(PyObject value) {
        this.value = value;
    }
}
