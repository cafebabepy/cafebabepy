package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefineCafeBabePyType(name = "builtins.int")
public class PyIntType extends AbstractCafeBabePyType {

    public static final String JAVA_INT_NAME = "int";

    public PyIntType(Python runtime) {
        super(runtime);
    }

    public static PyObject newInt(Python runtime, int value) {
        PyObject type = runtime.moduleOrThrow(Python.BUILTINS_MODULE_NAME).getObjectOrThrow("int");
        if (!(type instanceof PyIntType)) {
            // FIXME To CPython message
            throw runtime.newRaiseException(
                    "builtins.TypeError", "'" + type.getName() + "' is not int");
        }

        PyObject object = PyObject.callStatic(type);
        object.putJavaObject(JAVA_INT_NAME, value);

        return object;
    }
}
