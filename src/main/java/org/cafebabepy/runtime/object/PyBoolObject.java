package org.cafebabepy.runtime.object;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;

/**
 * Created by yotchang4s on 2017/06/23.
 */
public abstract class PyBoolObject extends AbstractPyObjectObject {
    PyBoolObject(Python runtime, PyObject type) {
        super(runtime, type);
    }
}
