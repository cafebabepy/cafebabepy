package org.cafebabepy.runtime.object.java;

import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.PyRuntimeObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Created by yotchang4s on 2017/05/31.
 */
public class JavaPyFunctionObject extends AbstractJavaPyObject implements PyRuntimeObject {

    private Object target;

    private Method method;

    private String name;

    public JavaPyFunctionObject(Python runtime, Object target, String name, Method method) {
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
            Object result = this.method.invoke(this.target, (Object[]) args);
            if (result == null) {
                return this.runtime.none();

            } else if (result instanceof PyObject) {
                return (PyObject) result;

            } else {
                // FIXME Java object to Python object
                throw new UnsupportedOperationException(
                        "FIXME Java object to Python object !!!!!!!!");
            }

        } catch (IllegalAccessException e) {
            throw new CafeBabePyException("Not accessible method "
                    + this.method.getDeclaringClass().getName()
                    + "#" + method.getName(), e);

        } catch (InvocationTargetException e) {
            // TODO Pythonの例外を参考にする
            StringBuilder argNamesBuilder = new StringBuilder();
            throw new CafeBabePyException("Not invoke method "
                    + this.method.getDeclaringClass().getName()
                    + "#" + method.getName(), e);
        }
    }

    public String getName() {
        return this.name;
    }

    @Override
    public PyObject getType() {
        return this.runtime.moduleOrThrow(Python.BUILTINS_MODULE_NAME).getObjectOrThrow("");
    }
}
