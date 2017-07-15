package org.cafebabepy.evaluter.ast;

import org.cafebabepy.runtime.PyObject;

/**
 * Created by yotchang4s on 2017/05/13.
 */
public final class AstReturn extends RuntimeException {
    private final PyObject value;

    public AstReturn(PyObject value) {
        this.value = value;
    }

    public PyObject getValue() {
        return this.value;
    }
}
