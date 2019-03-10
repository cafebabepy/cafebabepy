package org.cafebabepy.runtime.object.proxy;

import org.cafebabepy.runtime.PyFunctionObject;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.AbstractPyObjectObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.cafebabepy.util.ProtocolNames.__call__;

/**
 * Created by yotchang4s on 2017/06/23.
 */
public class PyMethodObject extends AbstractPyObjectObject implements PyFunctionObject {

    private final PyObject source;

    private final PyFunctionObject function;

    public PyMethodObject(Python runtime, PyFunctionObject function, PyObject source) {
        super(runtime);

        this.function = function;
        this.source = source;

        getFrame().getLocals().put(__call__, this);
    }

    public PyObject getSource() {
        return this.source;
    }

    public PyObject getFunction() {
        return this.function;
    }

    @Override
    public List<String> getArguments() {
        return this.function.getArguments();
    }

    @Override
    public PyObject call(PyObject... args) {
        return call(args, new LinkedHashMap<>());
    }

    @Override
    public PyObject call(PyObject[] args, LinkedHashMap<String, PyObject> keywords) {
        PyObject[] newArgs = new PyObject[args.length + 1];
        System.arraycopy(args, 0, newArgs, 1, args.length);
        newArgs[0] = this.source;

        return this.function.call(newArgs, keywords);
    }

    @Override
    public boolean isFromClass() {
        return false;
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("method", false);
    }
}
