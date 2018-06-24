package org.cafebabepy.evaluter.Interpret;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.PyObjectScope;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.AbstractPyObjectObject;
import org.cafebabepy.runtime.object.proxy.PyLexicalScopeProxyObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.cafebabepy.util.ProtocolNames.__call__;

/**
 * Created by yotchang4s on 2017/06/30.
 */
class PyInterpretFunctionObject extends AbstractPyObjectObject {
    private InterpretEvaluator evaluator;

    private PyObject context;
    private PyObject name;
    private PyObject arguments;
    private PyObject body;

    private List<PyObject> kw_defaults;
    private List<PyObject> defaultArgs;

    PyInterpretFunctionObject(Python runtime, InterpretEvaluator evaluator, PyObject context, PyObject name, PyObject arguments, PyObject body) {
        super(runtime);

        this.evaluator = evaluator;
        this.context = new PyLexicalScopeProxyObject(context); // arguments scope
        this.name = name;
        this.arguments = arguments;
        this.body = body;
    }

    public PyObject getArguments() {
        return this.arguments;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initialize() {
        List<PyObject> kw_defaultsList = this.runtime.getattr(this.arguments, "kw_defaults").toJava(List.class);
        this.kw_defaults = new ArrayList<>(kw_defaultsList.size());
        for (PyObject kw_default : kw_defaultsList) {
            PyObject value = this.evaluator.eval(this.context, kw_default);
            this.kw_defaults.add(value);
        }

        List<PyObject> defaultsList = this.runtime.getattr(this.arguments, "defaults").toJava(List.class);
        this.defaultArgs = new ArrayList<>(defaultsList.size());
        for (PyObject defaultArg : defaultsList) {
            PyObject value = this.evaluator.eval(this.context, defaultArg);
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
    public PyObject callSubstance(PyObject[] args, LinkedHashMap<String, PyObject> keywords) {
        PyObjectScope scope = this.context.getScope();

        PyObject vararg = this.runtime.getattr(this.arguments, "vararg");
        PyObject kwarg = this.runtime.getattr(this.arguments, "kwarg");

        List<PyObject> defineArgsList = this.runtime.getattr(this.arguments, "args").toJava(List.class);
        for (int i = 0; i < defineArgsList.size(); i++) {
            PyObject defineArg = this.runtime.getattr(defineArgsList.get(i), "arg");
            defineArgsList.set(i, defineArg);
        }

        List<PyObject> defineKwOnlyArgList = this.runtime.getattr(this.arguments, "kwonlyargs").toJava(List.class);
        for (int i = 0; i < defineKwOnlyArgList.size(); i++) {
            PyObject defineArg = this.runtime.getattr(defineKwOnlyArgList.get(i), "arg");
            defineKwOnlyArgList.set(i, defineArg);
        }

        if (args.length + this.defaultArgs.size() + keywords.size() + (vararg.isNone() ? 0 : 1) < defineArgsList.size()) {
            List<PyObject> notEnoughArguments = defineArgsList.subList(args.length, defineArgsList.size());

            StringBuilder error = new StringBuilder(this.name.toJava(String.class) + "() missing " + notEnoughArguments.size() + " required positional argument: ");

            if (notEnoughArguments.size() == 1) {
                error.append(notEnoughArguments.get(0));

            } else {
                for (int i = 0; i < notEnoughArguments.size() - 2; i++) {
                    if (i != 0) {
                        error.append(", ");
                    }
                    error.append(notEnoughArguments.get(i));
                }
                if (notEnoughArguments.size() > 2) {
                    error.append(", ");
                }

                error.append(notEnoughArguments.get(notEnoughArguments.size() - 2))
                        .append(" and ").append(notEnoughArguments.get(notEnoughArguments.size() - 1));
            }

            throw this.runtime.newRaiseTypeError(error.toString());

        } else if (vararg.isNone() && args.length > defineArgsList.size()) {
            String error = name.toJava(String.class) + "() takes " + defineArgsList.size() + " positional arguments but " + args.length + " were given";

            throw this.runtime.newRaiseTypeError(error);
        }

        int defaultArgumentIndex = args.length + this.defaultArgs.size() - defineArgsList.size() - defineKwOnlyArgList.size();
        int assignIndex = 0;

        // args
        for (int i = 0; i < defineArgsList.size(); i++) {
            PyObject arg = defineArgsList.get(i);

            if (i < args.length) {
                scope.put(arg, args[i]);
                assignIndex++;

            } else if (i < defineArgsList.size() - keywords.size()) {
                scope.put(arg, this.defaultArgs.get(defaultArgumentIndex++));
                assignIndex++;
            }
        }

        // variable argument
        if (!vararg.isNone()) {
            PyObject arg = this.runtime.getattr(vararg, "arg");

            PyObject[] varargs = new PyObject[args.length - assignIndex];
            int count = args.length - assignIndex;
            for (int i = 0; i < count; i++) {
                varargs[i] = args[assignIndex++];
            }

            scope.put(arg, this.runtime.tuple(varargs));
        }

        // keyword arguments
        for (String keywordString : keywords.keySet()) {
            PyObject keyword = this.runtime.str(keywordString);

            if (!defineArgsList.contains(keyword) && !defineKwOnlyArgList.contains(keyword) && kwarg.isNone()) {
                throw this.runtime.newRaiseTypeError(this.name.toJava(String.class) + "() got an unexpected keyword argument " + keyword);
            }

            if (scope.containsKey(keyword)) {
                throw this.runtime.newRaiseTypeError(this.name.toJava(String.class) + "() got multiple values for argument " + keyword);

            } else {
                if (defineArgsList.contains(keyword) && !defineKwOnlyArgList.contains(keyword)) {
                    scope.put(keyword, keywords.get(keywordString));
                }
            }
        }

        int kw_defaultIndex = 0;
        int kwonlyargsCount = defineKwOnlyArgList.size();
        for (PyObject kwonlyarg : defineKwOnlyArgList) {
            PyObject kwonlyargValue = keywords.get(kwonlyarg.toJava(String.class));
            if (kwonlyargValue == null) {
                if (kw_defaultIndex < this.kw_defaults.size()) {
                    scope.put(kwonlyarg, this.kw_defaults.get(kw_defaultIndex++));
                    kwonlyargsCount--;

                } else {
                    throw this.runtime.newRaiseTypeError(this.name.toJava(String.class)
                            + "() missing " + kwonlyargsCount + " required keyword-only argument: " + kwonlyarg);
                }

            } else {
                scope.put(kwonlyarg, kwonlyargValue);
            }
        }

        // variable keyword
        if (!kwarg.isNone()) {
            LinkedHashMap<PyObject, PyObject> kwargsMap = new LinkedHashMap<>();

            for (String keywordString : keywords.keySet()) {
                PyObject keyword = this.runtime.str(keywordString);
                if (!scope.containsKey(keyword)) {
                    kwargsMap.put(keyword, keywords.get(keywordString));
                }
            }

            PyObject arg = this.runtime.getattr(kwarg, "arg");
            scope.put(arg, this.runtime.dict(kwargsMap));
        }

        return this.evaluator.eval(this.context, body);
    }
}