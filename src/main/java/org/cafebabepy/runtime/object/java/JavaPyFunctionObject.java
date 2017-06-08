package org.cafebabepy.runtime.object.java;

import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Created by yotchang4s on 2017/05/31.
 */
public class JavaPyFunctionObject extends AbstractJavaPyObject {

    private PyObject target;

    private Method method;

    private String name;

    public JavaPyFunctionObject(Python runtime, PyObject target, String name, Method method) {
        super(runtime, runtime.getBuiltinsModule().getScope().getRaw("FunctionType"));

        if (!Modifier.isPublic(method.getModifiers())) {
            this.method.setAccessible(true);
        }

        this.target = target;
        this.name = name;
        this.method = method;
    }

    @Override
    public PyObject call(PyObject... args) {
        try {
            Object[] compArgs;
            Class<?>[] paramClasses = this.method.getParameterTypes();

            if (paramClasses.length == 0) {
                compArgs = new Object[0];

            } else {
                if (paramClasses.length == 1 && paramClasses[0].isArray()) {
                    compArgs = new Object[]{args};

                } else if (paramClasses[paramClasses.length - 1].isArray()) {
                    // Split argument and variable argument from args array
                    if (args.length > 0) {
                        if (args.length - paramClasses.length >= 0) {
                            // Last argument is variable argument
                            compArgs = new Object[paramClasses.length];

                            PyObject[] lastArrayArg = new PyObject[args.length - compArgs.length + 1];
                            System.arraycopy(args, 0, compArgs, 0, compArgs.length - 1);

                            for (int i = 0; i < lastArrayArg.length; i++) {
                                lastArrayArg[i] = args[i + compArgs.length - 1];
                            }

                            compArgs[paramClasses.length - 1] = lastArrayArg;

                        } else {
                            compArgs = new Object[args.length + 1];

                            for (int i = 0; i < args.length; i++) {
                                if (args[i].getClass().isArray()) {
                                    compArgs[i] = new PyObject[]{args[i]};

                                } else {
                                    compArgs[i] = args[i];
                                }
                            }

                            compArgs[compArgs.length - 1] = new PyObject[0];
                        }
                    } else {
                        compArgs = new PyObject[0];
                    }

                } else if (paramClasses.length != args.length) {
                    throw this.runtime.newRaiseException("builtins.TypeError",
                            target.getName() + "() takes at most "
                                    + paramClasses.length + " arguments (" + args.length + " given)");

                } else {
                    compArgs = new Object[paramClasses.length];
                    for (int i = 0; i < paramClasses.length; i++) {
                        Class<?> paramClass = paramClasses[i];
                        if (paramClass.isArray()) {
                            compArgs[i] = new PyObject[]{args[i]};

                        } else {
                            compArgs[i] = args[i];
                        }
                    }
                }
            }

            Object result = this.method.invoke(this.target, compArgs);
            if (result == null) {
                return this.runtime.None();

            } else if (result instanceof PyObject) {
                return (PyObject) result;

            } else {
                // FIXME Java object to Python object
                throw new UnsupportedOperationException(
                        "FIXME Java object to Python object !!!!!!!!");
            }

        } catch (IllegalAccessException | InvocationTargetException |
                IllegalArgumentException e) {
            // FIXME CPython message???
            throw new CafeBabePyException("Not accessible method "
                    + this.method.getDeclaringClass().getName()
                    + "#" + method.getName(), e);
        }

    }

    public String getName() {
        return this.name;
    }
}
