package org.cafebabepy.runtime.object.java;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.AbstractPyObjectObject;
import org.cafebabepy.runtime.object.java.PyIntObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by yotchang4s on 2017/06/19.
 */
public class PyTupleObject extends AbstractPyObjectObject {

    private final List<PyObject> list;

    public PyTupleObject(Python runtime, PyObject... value) {
        super(runtime, runtime.typeOrThrow("builtins.tuple"));

        this.list = Collections.unmodifiableList(Arrays.asList(value));
    }

    public List<PyObject> getList() {
        return this.list;
    }

    public PyObject get(PyIntObject i) {
        return this.list.get(i.getIntValue());
    }

    public PyObject getLen() {
        return this.runtime.number(list.size());
    }
}
