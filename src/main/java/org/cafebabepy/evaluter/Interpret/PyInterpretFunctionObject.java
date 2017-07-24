package org.cafebabepy.evaluter.Interpret;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.PyObjectScope;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.AbstractPyObjectObject;

import static org.cafebabepy.util.ProtocolNames.__call__;

/**
 * Created by yotchang4s on 2017/06/30.
 */
public class PyInterpretFunctionObject extends AbstractPyObjectObject {
    private PyObject context;
    private InterpretEvaluator evaluator;
    private PyObject args;
    private PyObject body;

    public PyInterpretFunctionObject(Python runtime, InterpretEvaluator evaluator, PyObject context, PyObject args, PyObject body) {
        super(runtime, runtime.typeOrThrow("builtins.FunctionType"));

        this.evaluator = evaluator;
        this.context = context;
        this.args = args;
        this.body = body;

        getScope().put(__call__, this);
    }

    @Override
    public PyObject call(PyObject... args) {
        this.context.pushScope();
        try {
            PyObjectScope scope = this.context.getScope();
            PyObject argslist = this.args.getObjectOrThrow("args");
            this.runtime.iterIndex(argslist, (a, i) -> {
                PyObject arg = a.getObjectOrThrow("arg");
                scope.put(arg.asJavaString(), args[i]);
            });

            return evaluator.eval(this.context, body);

        } finally {
            this.context.popScope();
        }
    }
}