package org.cafebabepy.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Created by yotchang4s on 2017/05/24.
 */
public class PyObjectScope {

    private Supplier<Optional<PyObject>> emptyObjectReadAccessor = () -> Optional.empty();

    private PyObjectScope parent;

    private Map<String, Supplier<Optional<PyObject>>> objectMap;

    private volatile Map<String, Supplier<Optional<PyObject>>> notAppearObjectMap;

    public PyObjectScope() {
        this(null);
    }

    public PyObjectScope(PyObjectScope parent) {
        this.parent = parent;
        this.objectMap = new ConcurrentHashMap<>();
    }

    public void put(String name, PyObject object) {
        put(name, object, true);
    }

    public void put(String name, PyObject object, boolean appear) {
        put(name, () -> Optional.ofNullable(object), appear);
    }

    public void put(String name, Supplier<Optional<PyObject>> objectReadAccessor) {
        put(name, objectReadAccessor, true);
    }

    public void put(String name, Supplier<Optional<PyObject>> objectReadAccessor, boolean appear) {
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

    public Map<String, PyObject> gets(boolean appear) {
        Map<String, PyObject> map;
        if (appear) {
            synchronized (this) {
                map = new HashMap<>(this.objectMap.size());
                for (Map.Entry<String, Supplier<Optional<PyObject>>> e : this.objectMap.entrySet()) {
                    Optional<PyObject> value = e.getValue().get();
                    if (value.isPresent()) {
                        map.put(e.getKey(), value.get());
                    }
                }
            }

        } else {
            synchronized (this) {
                map = new HashMap<>(this.objectMap.size() + this.notAppearObjectMap.size());
                for (Map.Entry<String, Supplier<Optional<PyObject>>> e : this.notAppearObjectMap.entrySet()) {
                    Optional<PyObject> value = e.getValue().get();
                    if (value.isPresent()) {
                        map.put(e.getKey(), value.get());
                    }
                }
                for (Map.Entry<String, Supplier<Optional<PyObject>>> e : this.objectMap.entrySet()) {
                    Optional<PyObject> value = e.getValue().get();
                    if (value.isPresent()) {
                        map.put(e.getKey(), value.get());
                    }
                }
            }
        }

        return map;
    }

    public Optional<PyObject> get(String name) {
        return get(name, true);
    }

    public Optional<PyObject> get(String name, boolean appear) {
        return getRaw(name, appear).get();
    }

    public Supplier<Optional<PyObject>> getRaw(String name) {
        return getRaw(name, true);
    }

    public Supplier<Optional<PyObject>> getRaw(String name, boolean appear) {
        Supplier<Optional<PyObject>> objectReadAccessor = this.objectMap.get(name);
        if (objectReadAccessor == null) {
            if (!appear) {
                return getRawAppearOnly(name);
            }
        }

        if (objectReadAccessor == null) {
            if (this.parent != null) {
                return this.parent.getRaw(name, appear);
            }

            return this.emptyObjectReadAccessor;
        }

        return objectReadAccessor;
    }

    public Optional<PyObject> getAppearOnly(String name) {
        return getRawAppearOnly(name).get();
    }

    public Supplier<Optional<PyObject>> getRawAppearOnly(String name) {
        Supplier<Optional<PyObject>> objectReadAccessor = null;
        if (this.notAppearObjectMap != null) {
            objectReadAccessor = this.notAppearObjectMap.get(name);
        }
        if (objectReadAccessor == null) {
            if (this.parent != null) {
                return this.parent.getRawAppearOnly(name);
            }

            return this.emptyObjectReadAccessor;
        }

        return objectReadAccessor;
    }
}
