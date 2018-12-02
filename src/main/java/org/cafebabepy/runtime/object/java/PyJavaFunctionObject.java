package org.cafebabepy.runtime.object.java;

import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.RaiseException;
import org.cafebabepy.runtime.internal.AbstractFunction;
import org.cafebabepy.runtime.object.PyObjectObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by yotchang4s on 2017/05/31.
 */
public class PyJavaFunctionObject extends AbstractFunction {

    private final PyObject target;
    private final Method method;

    public PyJavaFunctionObject(Python runtime, String name, PyObject target, Method method, Map<String, Method> defaultArgumentMap) {
        super(runtime, name, createArguments(runtime, target, method, new HashMap<>(defaultArgumentMap)));

        this.target = target;
        this.method = method;

        if (!Modifier.isPublic(method.getModifiers())) {
            this.method.setAccessible(true);
        }
    }

    private static PyObject createArguments(Python runtime, Object target, Method method, Map<String, Method> defaultArgumentMap) {
        PyObject arguments = new PyObjectObject(runtime);
        arguments.initialize();

        PyObject vararg = runtime.None();
        PyObject kwarg = runtime.None();

        List<PyObject> argList = new ArrayList<>();
        List<PyObject> kwonlyargList = new ArrayList<>();

        List<PyObject> argDefaultValueList = new ArrayList<>();
        List<PyObject> kwonlyargDefaultList = new ArrayList<>();

        boolean defineVararg = false;
        boolean defineKwarg = false;

        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];

            PyObject defaultValue = null;
            Method defaultValueMethod = defaultArgumentMap.get(parameter.getName());
            if (defaultValueMethod != null) {
                try {
                    // FIXME Java method to PyObject
                    defaultValue = (PyObject) defaultValueMethod.invoke(target);
                    defaultArgumentMap.remove(parameter.getName());

                    if (defaultValue == null) {
                        defaultValue = runtime.None();
                    }

                } catch (IllegalAccessException | InvocationTargetException | ClassCastException e) {
                    throw new CafeBabePyException("" +
                            "Fail invoke defalut value " + defaultValueMethod.getDeclaringClass().getName() + "#" + defaultValueMethod.getName() + "method ");
                }
            }

            PyObject arg = new PyObjectObject(runtime);
            arg.initialize();

            arg.getFrame().putToLocals("arg", runtime.str(parameter.getName()));
            arg.getFrame().putToLocals("annotation", runtime.None());

            Class<?> type = parameter.getType();
            if (PyObject.class.isAssignableFrom(type)) {
                if (defineKwarg) {
                    throw runtime.newRaiseException("SyntaxError", "invalid syntax");
                }

                if (defineVararg) {
                    kwonlyargList.add(arg);
                    if (defaultValue != null) {
                        kwonlyargDefaultList.add(defaultValue);
                    }

                } else {
                    argList.add(arg);
                    if (defaultValue != null) {
                        argDefaultValueList.add(defaultValue);
                    }
                }

            } else if (PyObject[].class.isAssignableFrom(type)) {
                if (defineVararg || defineKwarg || defaultValue != null) {
                    throw runtime.newRaiseException("SyntaxError", "invalid syntax");
                }

                vararg = arg;
                defineVararg = true;

            } else if (Map.class.isAssignableFrom(type)) {
                if (defineKwarg || defaultValue != null) {
                    throw runtime.newRaiseException("SyntaxError", "invalid syntax");
                }

                kwarg = arg;
                defineKwarg = true;
            }
        }

        if (!defaultArgumentMap.isEmpty()) {
            throw runtime.newRaiseException("SyntaxError", "invalid syntax");
        }

        arguments.getFrame().putToLocals("defaults", runtime.list(argDefaultValueList));
        arguments.getFrame().putToLocals("kw_defaults", runtime.list(kwonlyargDefaultList));
        arguments.getFrame().putToLocals("vararg", vararg);
        arguments.getFrame().putToLocals("kwarg", kwarg);
        arguments.getFrame().putToLocals("kwonlyargs", runtime.list(kwonlyargList));
        arguments.getFrame().putToLocals("args", runtime.list(argList));

        return arguments;
    }

    @Override
    protected PyObject evalDefaultValue(PyObject defaultValue) {
        return defaultValue;
    }

    @Override
    protected PyObject getattr(PyObject object, String key) {
        return object.getFrame().getFromGlobals(key).orElseThrow(() ->
                new CafeBabePyException(object + " key '" + key + "' is not found")
        );
    }

    @Override
    protected PyObject callImpl() {
        Map<String, PyObject> argumentMap = this.runtime.getCurrentContext().getFrame().getLocals();

        Object[] arguments = new Object[argumentMap.size()];
        Parameter[] parameters = this.method.getParameters();

        for (int i = 0; i < parameters.length; i++) {
            PyObject argument = argumentMap.get(parameters[i].getName());
            Parameter parameter = parameters[i];

            if (PyObject.class.isAssignableFrom(parameter.getType())) {
                arguments[i] = argument;

            } else if (Map.class.isAssignableFrom(parameter.getType())) {
                LinkedHashMap<PyObject, PyObject> pyArgumentTmpMap = argument.toJava(LinkedHashMap.class);

                LinkedHashMap<String, PyObject> argumentTmpMap = new LinkedHashMap<>();
                pyArgumentTmpMap.forEach((k, v) -> argumentTmpMap.put(k.toJava(String.class), v));

                arguments[i] = argumentTmpMap;

            } else if (List.class.isAssignableFrom(parameter.getType())) {
                arguments[i] = argument.toJava(List.class);

            } else if (PyObject[].class.isAssignableFrom(parameter.getType())) {
                List<PyObject> list = argument.toJava(List.class);
                arguments[i] = list.toArray(new PyObject[0]);

            } else {
                throw new CafeBabePyException("Illegal argument");
            }
        }

        try {
            Object result = this.method.invoke(this.target, (Object[]) arguments);
            if (result == null) {
                return this.runtime.None();
            }

            return (PyObject) result;

        } catch (ClassCastException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | UnsupportedOperationException e) {
            if (e instanceof InvocationTargetException) {
                Throwable t = ((InvocationTargetException) e).getTargetException();
                if (t instanceof RaiseException) {
                    throw (RaiseException) t;
                }
            }

            throw new CafeBabePyException("Fail invoke Java method "
                    + this.method.getDeclaringClass().getName()
                    + "#" + this.method.getName()
                    + Arrays.stream(this.method.getParameterTypes())
                    .map(Class::getName)
                    .collect(Collectors.joining(", ", " (", ")")), e);

        }
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("builtins.wrapper_descriptor", false);
    }
}
