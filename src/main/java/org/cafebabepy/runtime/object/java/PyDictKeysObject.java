package org.cafebabepy.runtime.object.java;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.AbstractPyObjectObject;

import java.util.Set;

/**
 * Created by yotchang4s on 2018/10/28.
 */
public class PyDictKeysObject extends AbstractPyObjectObject {

    private Set<PyObject> keys;

    public PyDictKeysObject(Python runtime, Set<PyObject> keys) {
        super(runtime);

        this.keys = keys;
    }

    public Set<PyObject> getValue() {
        return this.keys;
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("builtins.dict_keys");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T toJava(Class<T> clazz) {
        if (clazz == Set.class) {
            return (T) this.keys;
        }

        return super.toJava(clazz);
    }
}
