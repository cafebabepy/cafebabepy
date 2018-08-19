package org.cafebabepy.evaluter.Interpret;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.internal.AbstractFunction;
import org.cafebabepy.runtime.object.iterator.PyGeneratorObject;

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
        YieldSearcher yieldSearcher = new YieldSearcher(this.runtime);

        if (yieldSearcher.search(this.body)) {
            return new PyGeneratorObject(this.runtime, s -> {
                try {
                    this.runtime.getEvaluator().eval(this.context, this.body);
                    s.stop(this.runtime);
                    return this.runtime.None(); // ignore

                } catch (InterpretYield e) {
                    return e.value;

                } catch (InterpretReturn e) {
                    s.stop(this.runtime);
                    return this.runtime.None(); // ignore
                }
            });

        } else {

            try {
                return this.runtime.getEvaluator().eval(this.context, this.body);

            } catch (InterpretReturn e) {
                return e.value;
            }
        }
    }
}