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
        super(runtime);

        this.evaluator = evaluator;
        this.context = context;
        this.args = args;
        this.body = body;

        getScope().put(__call__, this);
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("builtins.FunctionType");
    }

    @Override
    public PyObject call(PyObject... args) {
        PyObject lexicalContext = new PyLexicalScopeProxyObject(this.context);
        PyObjectScope scope = lexicalContext.getScope();

        this.args.getScope().get("args").ifPresent(argslist ->
                this.runtime.iterIndex(argslist, (a, i) -> {
                    PyObject arg = this.runtime.getattr(a, "arg");
                    scope.put(arg.toJava(String.class), args[i]);
                })
        );

        return evaluator.eval(lexicalContext, body);
    }
}