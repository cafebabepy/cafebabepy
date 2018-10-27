package org.cafebabepy.runtime.object.java;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.AbstractPyObjectObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by yotchang4s on 2017/06/19.
 */
public class PyListObject extends AbstractPyObjectObject {

    private final List<PyObject> values;

    public PyListObject(Python runtime, PyObject... value) {
        this(runtime, Arrays.asList(value));
    }

    public PyListObject(Python runtime, List<PyObject> values) {
        super(runtime);

        this.values = new ArrayList<>(values);
    }

    public List<PyObject> getValues() {
        return Collections.unmodifiableList(this.values);
    }

    public List<PyObject> getRawValues() {
        return this.values;
    }

    public PyObject get(PyIntObject i) {
        int value = i.getIntValue();

        if (value < 0 || this.values.size() <= value) {
            throw this.runtime.newRaiseException("IndexError",
                    "list index out of range");
        }

        return this.values.get(value);
    }

    public PyObject getLen() {
        return this.runtime.number(values.size());
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("builtins.list");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T toJava(Class<T> clazz) {
        if (clazz == List.class) {
            return (T) new ArrayList<>(this.values);
        }

        return super.toJava(clazz);
    }
}
