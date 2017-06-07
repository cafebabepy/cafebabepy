package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefineCafeBabePyType(name = "builtins.tuple")
public class PyTupleType extends AbstractCafeBabePyType {

    public static final String JAVA_LIST_NAME = "tuple";

    public PyTupleType(Python runtime) {
        super(runtime);
    }

    public static PyObject newTuple(Python runtime, Collection value) {
        PyObject[] array = new PyObject[value.size()];
        value.toArray(array);

        return newTuple(runtime, array);
    }

    public static PyObject newTuple(Python runtime, PyObject... value) {
        PyObject type = runtime.moduleOrThrow(Python.BUILTINS_MODULE_NAME).getObjectOrThrow("tuple");
        if (!(type instanceof PyTupleType)) {
            // FIXME To CPython message
            throw runtime.newRaiseException(
                    "builtins.TypeError", "'" + type.getName() + "' is not tuple");
        }

        PyObject object = type.call();
        object.putJavaObject(JAVA_LIST_NAME, Collections.unmodifiableList(Arrays.asList(value)));

        return object;
    }
}
