package org.cafebabepy.runtime.object;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by yotchang4s on 2017/06/19.
 */
public class PyListObject extends AbstractPyObjectObject {

    private final List<PyObject> list;

    public PyListObject(Python runtime, PyObject... value) {
        super(runtime, runtime.typeOrThrow("builtins.list"));

        this.list = new ArrayList<>(Arrays.asList(value));
    }

    public List<PyObject> getList() {
        return Collections.unmodifiableList(this.list);
    }

    public List<PyObject> getRawList() {
        return this.list;
    }

    public PyObject get(PyIntObject i) {
        return this.list.get(i.getIntValue());
    }

    public PyObject getLen() {
        return this.runtime.number(list.size());
    }

    @Override
    public String asJavaString() {
        return this.list.stream()
                .map(PyObject::asJavaString)
                .collect(Collectors.joining(", ", "[", "]"));
    }
}
