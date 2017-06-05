package org.cafebabepy.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by yotchang4s on 2017/05/24.
 */
public class PyObjectScope {

    private PyObjectScope parent;

    private Map<String, PyObjectReadAccessor> objectMap;

    private volatile Map<String, PyObjectReadAccessor> notAppearObjectMap;

    public PyObjectScope() {
        this(null);
    }

    public PyObjectScope(PyObjectScope parent) {
        this.parent = parent;
        this.objectMap = new HashMap<>();
    }

    public void put(String name, PyObject object) {
        put(name, object, true);
    }

    public void put(String name, PyObject object, boolean appear) {
        put(name, () -> object, appear);
    }

    public void put(String name, PyObjectReadAccessor objectReadAccessor) {
        put(name, objectReadAccessor, true);
    }

    public void put(String name, PyObjectReadAccessor objectReadAccessor, boolean appear) {
        if (appear) {
            this.objectMap.put(name, objectReadAccessor);

        } else {
            if (this.notAppearObjectMap == null) {
                synchronized (this) {
                    if (this.notAppearObjectMap == null) {
                        this.notAppearObjectMap = new HashMap<>();
                    }
                }
            }

            this.notAppearObjectMap.put(name, objectReadAccessor);
        }
    }

    public PyObjectScope getParentScope() {
        return this.parent;
    }

    public Optional<PyObject> get(String name) {
        return get(name, true);
    }

    public Optional<PyObject> get(String name, boolean appear) {
        return getRaw(name, appear).map(PyObjectReadAccessor::getObject);
    }

    public Optional<PyObjectReadAccessor> getRaw(String name, boolean appear) {
        PyObjectReadAccessor objectReadAccessor = this.objectMap.get(name);
        if (objectReadAccessor == null) {
            if (!appear) {
                return getRawAppearOnly(name);
            }
        }

        if (objectReadAccessor == null) {
            if (this.parent != null) {
                return this.parent.getRaw(name, appear);
            }
        }

        return Optional.ofNullable(objectReadAccessor);
    }

    public Optional<PyObject> getAppearOnly(String name) {
        return getRawAppearOnly(name).map(PyObjectReadAccessor::getObject);
    }

    public Optional<PyObjectReadAccessor> getRawAppearOnly(String name) {
        PyObjectReadAccessor objectReadAccessor = null;
        if (this.notAppearObjectMap != null) {
            objectReadAccessor = this.notAppearObjectMap.get(name);
        }
        if (objectReadAccessor == null) {
            if(this.parent != null) {
                return this.parent.getRawAppearOnly(name);
            }
        }

        return Optional.ofNullable(objectReadAccessor);
    }
}
