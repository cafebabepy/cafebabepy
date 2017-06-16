package org.cafebabepy.runtime;

/**
 * Created by yotchang4s on 2017/05/15.
 */
public class CafeBabePyException extends RuntimeException {

    public CafeBabePyException() {
        super();
    }

    public CafeBabePyException(String message) {
        super(message);
    }

    public CafeBabePyException(Throwable t) {
        super(t);
    }

    public CafeBabePyException(String message, Throwable t) {
        super(message, t);
    }
}
