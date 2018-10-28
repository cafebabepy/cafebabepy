package org.cafebabepy.runtime.object.java;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.AbstractPyObjectObject;

import java.util.*;

/**
 * Created by yotchang4s on 2018/10/28.
 */
public class PySetObject extends AbstractPyObjectObject {

    private final Set<PyObject> value;

    public PySetObject(Python runtime, PyObject... value) {
        this(runtime, toSet(value));
    }

    public PySetObject(Python runtime, Set<PyObject> value) {
        super(runtime);

        this.value = value;
    }

    private static Set<PyObject> toSet(PyObject... value) {
        Set<PyObject> set = new LinkedHashSet<>();

        for (PyObject v : value) {
            set.add(v);
        }

        return set;
    }

    public Set<PyObject> getValue() {
        return Collections.unmodifiableSet(this.value);
    }

    public Set<PyObject> getView() {
        return this.value;
    }

    public PyObject getLen() {
        return this.runtime.number(this.value.size());
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("builtins.set");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T toJava(Class<T> clazz) {
        if (clazz == Set.class) {
            return (T) this.value;

        } else if (clazz == List.class) {
            return (T) new ArrayList<>(this.value);
        }

        return super.toJava(clazz);
    }
}
