package org.cafebabepy.runtime.object.java;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.AbstractPyObjectObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by yotchang4s on 2017/06/19.
 */
public class PyDictObject extends AbstractPyObjectObject {

    private LinkedHashMap<PyObject, PyObject> map;

    public PyDictObject(Python runtime) {
        super(runtime);

        this.map = new LinkedHashMap<>();
    }

    public PyDictObject(Python runtime, Map<PyObject, PyObject> map) {
        super(runtime);

        if (map instanceof LinkedHashMap) {
            this.map = (LinkedHashMap<PyObject, PyObject>) map;

        } else {
            this.map = new LinkedHashMap<>(map);
        }
    }

    public Map<PyObject, PyObject> getValue() {
        return Collections.unmodifiableMap(this.map);
    }

    public Map<PyObject, PyObject> getView() {
        return this.map;
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("builtins.dict");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T toJava(Class<T> clazz) {
        if (clazz == LinkedHashMap.class) {
            return (T) new LinkedHashMap<>(this.map);

        } else if (clazz == Map.class) {
            return (T) new LinkedHashMap<>(this.map);
        }

        return super.toJava(clazz);
    }
}
