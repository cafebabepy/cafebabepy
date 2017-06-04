package org.cafebabepy.runtime.object;

import org.cafebabepy.runtime.PyObject;

import java.util.Optional;

/**
 * Created by yotchang4s on 2017/06/04.
 */
public interface PyRuntimeObject extends PyObject {
    void putJavaObject(String name, Object object);

    Optional<Object> getJavaObject(String name);
}
