package org.cafebabepy.runtime;

/**
 * Created by yotchang4s on 2017/05/13.
 */
public class RaiseException extends RuntimeException {
    private PyObject exception;

    public RaiseException(PyObject exception) {
        super();

        this.exception = exception;
    }

    // FIXME remove
    public RaiseException(PyObject exception, String message) {
        super(message);

        this.exception = exception;
    }

    public PyObject getException() {
        return this.exception;
    }
}
