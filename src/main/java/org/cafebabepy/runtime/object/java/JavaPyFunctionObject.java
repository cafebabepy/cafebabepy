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
        super(runtime, runtime.moduleOrThrow(Python.TYPES_MODULE_NAME).getObjectOrThrow("FunctionType"));

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
            if (args.length == 0) {
                if (paramClasses.length == 1 && paramClasses[0].isArray()) {
                    compArgs = new Object[]{args};

                } else {
                    throw this.runtime.newRaiseException("builtins.TypError",
                            target.getName() + "() takes at most "
                                    + paramClasses.length + " arguments (" + args.length + " given)");
                }

            } else {
                if (paramClasses.length == args.length + 1) {
                    compArgs = new Object[args.length + 1];
                    if (paramClasses[paramClasses.length - 1].isArray()) {
                        compArgs[compArgs.length - 1] = new PyObject[0];
                    }

                } else {
                    compArgs = new Object[args.length];
                }
                for (int i = 0; i < args.length; i++) {
                    Class<?> paramClass = paramClasses[i];
                    if (paramClass.isArray()) {
                        compArgs[i] = new PyObject[]{args[i]};

                    } else {
                        compArgs[i] = args[i];
                    }
                }
            }

            Object result = this.method.invoke(target, compArgs);
            if (result == null) {
                return this.runtime.none();

            } else if (result instanceof PyObject) {
                return (PyObject) result;

            } else {
                // FIXME Java object to Python object
                throw new UnsupportedOperationException(
                        "FIXME Java object to Python object !!!!!!!!");
            }

        } catch (IllegalAccessException | InvocationTargetException |
                IllegalArgumentException e) {
            throw new CafeBabePyException("Not accessible method "
                    + this.method.getDeclaringClass().getName()
                    + "#" + method.getName(), e);
        }

    }

    public String getName() {
        return this.name;
    }
}
