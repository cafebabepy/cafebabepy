package org.cafebabepy.runtime;

/**
 * Created by yotchang4s on 2017/06/04.
 */
@FunctionalInterface
public interface PyObjectReadAccessor {

    PyObject getObject();
}
