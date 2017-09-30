package org.cafebabepy.runtime.object.proxy;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.PyObjectScope;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.AbstractPyObjectObject;

/**
 * Created by yotchang4s on 2017/06/23.
 */
public class PyMethodTypeObject extends AbstractPyObjectObject {

    private final PyObject source;

    private final PyObject function;

    private final PyObjectScope scope;

    public PyMethodTypeObject(Python runtime, PyObject source, PyObject function) {
        super(runtime);

        this.source = source;
        this.function = function;

        this.scope = new PyObjectScope(function);
    }

    @Override
    public PyObject call(PyObject... args) {
        return this.function.call(this.source, args);
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("types.MethodType");
    }

    @Override
    public PyObjectScope getScope() {
        return this.scope;
    }
}
