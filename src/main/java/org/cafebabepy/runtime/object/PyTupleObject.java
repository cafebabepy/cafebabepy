package org.cafebabepy.runtime.object;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by yotchang4s on 2017/06/19.
 */
public class PyTupleObject extends AbstractPyObjectObject {

    private final List<PyObject> tuple;

    public PyTupleObject(Python runtime, PyObject... value) {
        super(runtime, runtime.typeOrThrow("builtins.tuple"));

        this.tuple = Collections.unmodifiableList(Arrays.asList(value));
    }

    public List<PyObject> getList() {
        return this.tuple;
    }

    @Override
    public String asJavaString() {
        return this.tuple.stream()
                .map(PyObject::asJavaString)
                .collect(Collectors.joining(",", "(", ")"));
    }
}
