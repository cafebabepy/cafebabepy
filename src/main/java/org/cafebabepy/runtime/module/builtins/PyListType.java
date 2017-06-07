package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by yotchang4s on 2017/06/03.
 */
@DefineCafeBabePyType(name = "builtins.list")
public class PyListType extends AbstractCafeBabePyType {

    public static final String JAVA_LIST_NAME = "list";

    public PyListType(Python runtime) {
        super(runtime);
    }

    public static PyObject newList(Python runtime, Collection<PyObject> value) {
        PyObject[] array = new PyObject[value.size()];
        value.toArray(array);

        return newList(runtime, array);
    }

    public static PyObject newList(Python runtime, PyObject... value) {

        PyObject type = runtime.moduleOrThrow(Python.BUILTINS_MODULE_NAME).getObjectOrThrow("list");
        if (!(type instanceof PyListType)) {
            // FIXME To CPython message
            throw runtime.newRaiseException(
                    "builtins.TypeError", "'" + type.getName() + "' is not list");
        }

        PyObject object = type.call();
        object.putJavaObject(JAVA_LIST_NAME, Collections.unmodifiableList(Arrays.asList(value)));

        return object;
    }
}
