package org.cafebabepy.evaluter.Interpret;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.PyObjectScope;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.AbstractPyObjectObject;
import org.cafebabepy.runtime.object.proxy.PyLexicalScopeProxyObject;

import java.util.ArrayList;
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
    private List<PyObject> defaultArgs;

    PyInterpretFunctionObject(Python runtime, InterpretEvaluator evaluator, PyObject context, PyObject name, PyObject args, PyObject body) {
        super(runtime);

        this.evaluator = evaluator;
        this.context = new PyLexicalScopeProxyObject(context);
        this.name = name;
        this.args = args;
        this.body = body;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initialize() {
        List<PyObject> defaultsList = this.runtime.getattr(this.args, "defaults").toJava(List.class);

        int count = defaultsList.size();
        this.defaultArgs = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            PyObject value = this.evaluator.eval(this.context, defaultsList.get(i));
            this.defaultArgs.add(value);
        }

        getScope().put(this.runtime.str(__call__), this);
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("builtins.FunctionType", false);
    }

    @Override
    @SuppressWarnings("unchecked")
    public PyObject call(PyObject... args) {
        PyObject lexicalContext = new PyLexicalScopeProxyObject(this.context);

        PyObjectScope scope = lexicalContext.getScope();

        List<PyObject> argsList = this.runtime.getattr(this.args, "args").toJava(List.class);

        if (args.length + this.defaultArgs.size() < argsList.size()) {
            List<PyObject> notEnoughArguments = argsList.subList(args.length, argsList.size());

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

        } else if (args.length > argsList.size()) {
            String error = name.toJava(String.class) + "() takes " + argsList.size() + " positional arguments but " + args.length + " were given";

            throw this.runtime.newRaiseTypeError(error);
        }

        int count = argsList.size();

        int defaultArgumentIndex;
        if (argsList.size() == this.defaultArgs.size()) {
            defaultArgumentIndex = args.length;

        } else {
            defaultArgumentIndex = argsList.size() - this.defaultArgs.size() - args.length;
        }

        for (int i = 0; i < count; i++) {
            PyObject arg = this.runtime.getattr(argsList.get(i), "arg");

            if (i < args.length) {
                scope.put(arg, args[i]);

            } else {
                scope.put(arg, this.defaultArgs.get(defaultArgumentIndex++));
            }
        }

        return this.evaluator.eval(lexicalContext, body);
    }
}