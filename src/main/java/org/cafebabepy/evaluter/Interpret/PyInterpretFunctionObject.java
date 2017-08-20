package org.cafebabepy.evaluter.Interpret;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.PyObjectScope;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.AbstractPyObjectObject;
import org.cafebabepy.runtime.object.proxy.PyLexicalScopeProxyObject;

import static org.cafebabepy.util.ProtocolNames.__call__;

/**
 * Created by yotchang4s on 2017/06/30.
 */
class PyInterpretFunctionObject extends AbstractPyObjectObject {
    private PyObject context;
    private InterpretEvaluator evaluator;
    private PyObject args;
    private PyObject body;

    PyInterpretFunctionObject(Python runtime, InterpretEvaluator evaluator, PyObject context, PyObject args, PyObject body) {
        super(runtime, runtime.typeOrThrow("builtins.FunctionType"));

        this.evaluator = evaluator;
        this.context = context;
        this.args = args;
        this.body = body;

        getScope().put(__call__, this);
    }

    @Override
    public PyObject call(PyObject... args) {
        PyObject lexicalContext = new PyLexicalScopeProxyObject(this.context);
        PyObjectScope scope = lexicalContext.getScope();

        this.args.getScope().get("args").ifPresent(argslist ->
                this.runtime.iterIndex(argslist, (a, i) -> {
                    PyObject arg = a.getScope().getOrThrow("arg");
                    scope.put(arg.asJavaString(), args[i]);
                })
        );

        return evaluator.eval(lexicalContext, body);
    }
}