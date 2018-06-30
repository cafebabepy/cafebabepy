package org.cafebabepy.runtime.object.java;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.AbstractPyObjectObject;

import java.util.*;

/**
 * Created by yotchang4s on 2017/06/19.
 */
public class PyTupleObject extends AbstractPyObjectObject {

    private final List<PyObject> values;

    public PyTupleObject(Python runtime, PyObject... value) {
        this(runtime, Arrays.asList(value));
    }

    public PyTupleObject(Python runtime, List<PyObject> values) {
        super(runtime);

        this.values = new ArrayList<>(values);
    }

    public List<PyObject> getRawValues() {
        return this.values;
    }

    public List<PyObject> getValues() {
        return Collections.unmodifiableList(this.values);
    }

    public PyObject getLen() {
        return this.runtime.number(values.size());
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("builtins.tuple");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T toJava(Class<T> clazz) {
        if (clazz == List.class) {
            return (T) new ArrayList<>(this.values);

        } else if (clazz == Set.class) {
            return (T) new LinkedHashSet<>(this.values);
        }

        return super.toJava(clazz);
    }
}
