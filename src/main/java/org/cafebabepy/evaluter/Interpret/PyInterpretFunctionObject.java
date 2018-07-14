package org.cafebabepy.evaluter.Interpret;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.internal.AbstractFunction;

/**
 * Created by yotchang4s on 2017/06/30.
 */
class PyInterpretFunctionObject extends AbstractFunction {
    private PyObject body;

    PyInterpretFunctionObject(Python runtime, String name, PyObject context, PyObject arguments, PyObject body) {
        super(runtime, context, name, arguments);

        this.body = body;
    }

    @Override
    protected PyObject evalDefaultValue(PyObject defaultValue) {
        return this.runtime.getEvaluator().eval(this.context, defaultValue);
    }

    @Override
    protected PyObject callImpl(PyObject context) {
        try {
            return this.runtime.getEvaluator().eval(this.context, body);

        } catch (InterpretReturn e) {
            return e.getValue();
        }
    }
}