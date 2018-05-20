package org.cafebabepy.evaluter.Interpret;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.PyObjectScope;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.AbstractPyObjectObject;
import org.cafebabepy.runtime.object.proxy.PyLexicalScopeProxyObject;

import java.util.List;

import static org.cafebabepy.util.ProtocolNames.__call__;

/**
 * Created by yotchang4s on 2017/06/30.
 */
class PyInterpretFunctionObject extends AbstractPyObjectObject {
    private InterpretEvaluator evaluator;

    private PyObject context;
    private PyObject name;
    private PyObject args;
    private PyObject body;

    PyInterpretFunctionObject(Python runtime, InterpretEvaluator evaluator, PyObject context, PyObject name, PyObject args, PyObject body) {
        super(runtime);

        this.evaluator = evaluator;
        this.context = context;
        this.name = name;
        this.args = args;
        this.body = body;

        getScope().put(this.runtime.str(__call__), this);
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("builtins.FunctionType");
    }

    @Override
    public PyObject call(PyObject... args) {
        PyObject lexicalContext = new PyLexicalScopeProxyObject(this.context);
        PyObjectScope scope = lexicalContext.getScope();

        this.runtime.getattrOptional(this.args, "args").ifPresent(argslist -> {
                    List<PyObject> arguments = (List<PyObject>) argslist.toJava(List.class);
                    if (args.length < arguments.size()) {
                        List<PyObject> notEnoughArguments = arguments.subList(args.length, arguments.size());

                        StringBuilder error = new StringBuilder(name.toJava(String.class) + "() missing " + notEnoughArguments.size() + " required positional argument: ");

                        if (notEnoughArguments.size() == 1) {
                            error.append(this.runtime.getattr(notEnoughArguments.get(0), "arg"));

                        } else {
                            for (int i = 0; i < notEnoughArguments.size() - 2; i++) {
                                if (i != 0) {
                                    error.append(", ");
                                }
                                error.append(this.runtime.getattr(notEnoughArguments.get(i), "arg"));
                            }
                            if (notEnoughArguments.size() > 2) {
                                error.append(", ");
                            }

                            error.append(this.runtime.getattr(notEnoughArguments.get(notEnoughArguments.size() - 2), "arg"))
                                    .append(" and ").append(this.runtime.getattr(notEnoughArguments.get(notEnoughArguments.size() - 1), "arg"));
                        }

                        throw this.runtime.newRaiseTypeError(error.toString());

                    } else if (args.length > arguments.size()) {
                        String error = name.toJava(String.class) + "() takes " + arguments.size() + " positional arguments but " + args.length + " were given";

                        throw this.runtime.newRaiseTypeError(error);
                    }

                    this.runtime.iterIndex(argslist, (a, i) -> {
                        PyObject arg = this.runtime.getattr(a, "arg");
                        scope.put(arg, args[i]);
                    });
                }
        );

        return evaluator.eval(lexicalContext, body);
    }
}