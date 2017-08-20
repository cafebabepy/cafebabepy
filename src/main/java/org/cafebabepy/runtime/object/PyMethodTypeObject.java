package org.cafebabepy.runtime.object;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;

/**
 * Created by yotchang4s on 2017/06/23.
 */
public class PyMethodTypeObject extends AbstractPyObjectObject {

    private final PyObject source;

    private final PyObject function;

    public PyMethodTypeObject(Python runtime, PyObject source, PyObject function) {
        super(runtime, runtime.typeOrThrow("types.MethodType"));

        this.source = source;
        this.function = function;
    }

    @Override
    public PyObject call(PyObject... args) {
        return this.function.call(this.source, args);
    }
}
