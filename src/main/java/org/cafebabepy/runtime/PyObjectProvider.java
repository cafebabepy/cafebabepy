package org.cafebabepy.runtime;

/**
 * Created by yotchang4s on 2018/06/05.
 */
@FunctionalInterface
public interface PyObjectProvider {
    PyObject get();
}