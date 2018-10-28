package org.cafebabepy.runtime.object.java;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.AbstractPyObjectObject;

import java.util.Map;
import java.util.Set;

/**
 * Created by yotchang4s on 2018/10/28.
 */
public class PyDictItemsObject extends AbstractPyObjectObject {

    private Map<PyObject, PyObject> items;

    public PyDictItemsObject(Python runtime, Map<PyObject, PyObject> items) {
        super(runtime);

        this.items = items;
    }

    public Map<PyObject, PyObject> getValue() {
        return this.items;
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("builtins.dict_items");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T toJava(Class<T> clazz) {
        if (clazz == Map.class) {
            return (T) this.items;
        }

        return super.toJava(clazz);
    }
}
