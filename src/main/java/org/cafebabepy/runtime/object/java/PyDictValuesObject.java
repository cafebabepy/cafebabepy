package org.cafebabepy.runtime.object.java;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.AbstractPyObjectObject;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by yotchang4s on 2018/10/28.
 */
public class PyDictValuesObject extends AbstractPyObjectObject {

    private LinkedHashSet<PyObject> values;

    public PyDictValuesObject(Python runtime, Set<PyObject> values) {
        super(runtime);

        if (values instanceof LinkedHashSet) {
            this.values = (LinkedHashSet<PyObject>) values;

        } else {
            this.values = new LinkedHashSet<>(values);
        }
    }

    public Set<PyObject> getValue() {
        return this.values;
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("builtins.dict_values");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T toJava(Class<T> clazz) {
        if (clazz == Set.class) {
            return (T) this.values;
        }

        return super.toJava(clazz);
    }
}
