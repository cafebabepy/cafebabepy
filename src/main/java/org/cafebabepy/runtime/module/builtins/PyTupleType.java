package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.object.PyRuntimeObject;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefineCafeBabePyType(name = "builtins.tuple")
public class PyTupleType extends AbstractCafeBabePyType {

    public static final String JAVA_TUPLE_NAME = "tuple";

    public PyTupleType(Python runtime) {
        super(runtime);
    }

    public static PyRuntimeObject newTuple(Python runtime, PyObject... vars) {
        PyObject type = runtime.moduleOrThrow(Python.BUILTINS_MODULE_NAME).getObjectOrThrow("str");
        if (type instanceof PyStrType) {
            // FIXME To CPython message
            throw runtime.newRaiseException(
                    "builtins.TypeError", "'" + type.getName() + "' is not str");
        }

        PyObject object = type.call();

        if (object instanceof PyRuntimeObject) {
            PyRuntimeObject runtimeObject = (PyRuntimeObject) object;
            runtimeObject.putJavaObject(JAVA_TUPLE_NAME, vars);

            return runtimeObject;

        } else {
            throw runtime.newRaiseException("builtins.TypeError",
                    "object '" + object.getType().getName() + "'");
        }
    }
}
