package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefineCafeBabePyFunction;
import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.cafebabepy.util.ProtocolNames.__iter__;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefineCafeBabePyType(name = "builtins.tuple")
public class PyTupleType extends AbstractCafeBabePyType {

    public static final String JAVA_LIST_NAME = "tuple";
    static final String PY_OBjECT_TUPLE_NAME = "_tuple";

    public PyTupleType(Python runtime) {
        super(runtime);
    }

    @DefineCafeBabePyFunction(name = __iter__)
    public PyObject __iter__tuple(PyObject self) {
        return this.runtime.newPyObject("builtins.tuple_iterator", false, self);
    }

    public static PyObject newTuple(Python runtime, Collection value) {
        PyObject[] array = new PyObject[value.size()];
        value.toArray(array);

        return newTuple(runtime, array);
    }

    public static PyObject newTuple(Python runtime, PyObject... value) {
        PyObject object = runtime.newPyObject("builtins.tuple");

        object.putJavaObject(JAVA_LIST_NAME, Collections.unmodifiableList(Arrays.asList(value)));

        return object;
    }
}
