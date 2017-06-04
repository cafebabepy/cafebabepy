package org.cafebabepy.runtime;

import org.cafebabepy.runtime.PyObject;

/**
 * Created by yotchang4s on 2017/05/13.
 */
// TODO
public class RaiseException extends RuntimeException{
    public RaiseException(PyObject exception, String msg) {
        super(msg);
    }
}
