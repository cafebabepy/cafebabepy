package org.cafebabepy.runtime;

import org.cafebabepy.runtime.PyObject;

/**
 * Created by yotchang4s on 2017/05/13.
 */
// TODO
public class RaiseException extends RuntimeException{
    private PyObject exception;

    public RaiseException(PyObject exception, String message) {
        super(message);

        this.exception = exception;
    }

    public PyObject getException() {
        return this.exception;
    }
}
