package org.cafebabepy.runtime.object.java;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.AbstractPyObjectObject;

import java.util.*;

/**
 * Created by yotchang4s on 2018/10/28.
 */
public class PyFrozensetObject extends AbstractPyObjectObject {

    private final Set<PyObject> value;

    public PyFrozensetObject(Python runtime, PyObject... value) {
        super(runtime);

        this.value = new LinkedHashSet<>();

        for (int i = 0; i < value.length; i++) {
            this.value.add(value[i]);
        }
    }

    public PyFrozensetObject(Python runtime, Collection<PyObject> value) {
        super(runtime);

        this.value = new LinkedHashSet<>(value);
    }

    public Set<PyObject> getValue() {
        return Collections.unmodifiableSet(this.value);
    }

    public Set<PyObject> getView() {
        return this.value;
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("builtins.frozenset");
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
