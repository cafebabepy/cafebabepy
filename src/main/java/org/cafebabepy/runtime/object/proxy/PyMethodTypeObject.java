package org.cafebabepy.runtime.object.proxy;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.AbstractPyObjectObject;

import static org.cafebabepy.util.ProtocolNames.__call__;

/**
 * Created by yotchang4s on 2017/06/23.
 */
public class PyMethodTypeObject extends AbstractPyObjectObject {

    private final PyObject source;

    private final PyObject function;

    public PyMethodTypeObject(Python runtime, PyObject source, PyObject function) {
        super(runtime);

        this.source = source;
        this.function = function;
        ;

        getScope().put(__call__, this);
    }

    public PyObject getSource() {
        return this.source;
    }

    public PyObject getFunction() {
        return this.function;
    }

    @Override
    public PyObject call(PyObject... args) {
        return this.function.call(this.source, args);
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("types.MethodType");
    }
}
