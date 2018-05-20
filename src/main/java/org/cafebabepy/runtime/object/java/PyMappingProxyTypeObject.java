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
public class PyMappingProxyTypeObject extends AbstractPyObjectObject {

    private Map<PyObject, PyObject> map;

    public PyMappingProxyTypeObject(Python runtime) {
        this(runtime, Collections.synchronizedMap(new LinkedHashMap<>()));
    }

    public PyMappingProxyTypeObject(Python runtime, Map<PyObject, PyObject> map) {
        super(runtime);

        this.map = map;
    }

    public Map<PyObject, PyObject> getMap() {
        return Collections.unmodifiableMap(this.map);
    }

    public Map<PyObject, PyObject> getRawMap() {
        return this.map;
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("builtins.dict");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T toJava(Class<T> clazz) {
        if (clazz == Map.class) {
            return (T) new LinkedHashMap<>(this.map);
        }

        return super.toJava(clazz);
    }
}
