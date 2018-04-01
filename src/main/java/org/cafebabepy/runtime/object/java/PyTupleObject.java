package org.cafebabepy.runtime.object.java;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.AbstractPyObjectObject;

import java.util.*;

/**
 * Created by yotchang4s on 2017/06/19.
 */
public class PyTupleObject extends AbstractPyObjectObject {

    private final List<PyObject> list;

    public PyTupleObject(Python runtime, PyObject... value) {
        super(runtime);

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

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("builtins.tuple");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T toJava(Class<T> clazz) {
        if (clazz == List.class) {
            return (T) new ArrayList<>(this.list);

        } else if (clazz == Set.class) {
            return (T) new LinkedHashSet<>(this.list);
        }

        return super.toJava(clazz);
    }
}
