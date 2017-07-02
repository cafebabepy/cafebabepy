package org.cafebabepy.runtime;

import org.cafebabepy.util.LazyMap;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Created by yotchang4s on 2017/05/24.
 */
public class PyObjectScope {

    private Supplier<Optional<PyObject>> emptyObjectReadAccessor = () -> Optional.empty();

    private PyObjectScope parent;

    private LazyMap<String, Supplier<PyObject>> objectMap;

    private volatile LazyMap<String, Supplier<PyObject>> notAppearObjectMap;

    public PyObjectScope() {
        this(null);
    }

    public PyObjectScope(PyObjectScope parent) {
        this.parent = parent;
        this.objectMap = new LazyMap(LinkedHashMap::new);
    }

    public void put(String name, PyObject object) {
        put(name, object, true);
    }

    public void put(String name, PyObject object, boolean appear) {
        put(name, () -> object, appear);
    }

    public void put(String name, Supplier<PyObject> objectReadAccessor) {
        put(name, objectReadAccessor, true);
    }

    public void put(String name, Supplier<PyObject> objectReadAccessor, boolean appear) {
        if (appear) {
            this.objectMap.put(name, objectReadAccessor);

        } else {
            if (this.notAppearObjectMap == null) {
                synchronized (this) {
                    if (this.notAppearObjectMap == null) {
                        this.notAppearObjectMap = new LazyMap<>(LinkedHashMap::new);
                    }
                }
            }

            this.notAppearObjectMap.put(name, objectReadAccessor);
        }
    }

    public PyObjectScope getParentScope() {
        return this.parent;
    }

    public Map<String, Supplier<PyObject>> getsLazy() {
        return getsLazy(true);
    }

    public LazyMap<String, Supplier<PyObject>> getsLazy(boolean appear) {
        LazyMap<String, Supplier<PyObject>> map;
        if (appear) {
            synchronized (this) {
                map = new LazyMap<>(LinkedHashMap::new);

                for (Map.Entry<String, Supplier<PyObject>> e : this.objectMap.entrySet()) {
                    map.put(e.getKey(), e.getValue());
                }
            }

        } else {
            synchronized (this) {
                map = new LazyMap<>(LinkedHashMap::new);

                for (Map.Entry<String, Supplier<PyObject>> e : this.notAppearObjectMap.entrySet()) {
                    map.put(e.getKey(), e.getValue());
                }

                for (Map.Entry<String, Supplier<PyObject>> e : this.objectMap.entrySet()) {
                    map.put(e.getKey(), e.getValue());
                }
            }
        }

        return map;
    }

    public Map<String, PyObject> gets() {
        return gets(true);
    }

    public Map<String, PyObject> gets(boolean appear) {
        Map<String, PyObject> map;
        if (appear) {
            synchronized (this) {
                map = new LinkedHashMap<>(this.objectMap.size());

                for (Map.Entry<String, Supplier<PyObject>> e : this.objectMap.entrySet()) {
                    map.put(e.getKey(), e.getValue().get());
                }
            }

        } else {
            synchronized (this) {
                map = new LinkedHashMap<>(this.objectMap.size() + this.notAppearObjectMap.size());

                for (Map.Entry<String, Supplier<PyObject>> e : this.notAppearObjectMap.entrySet()) {
                    map.put(e.getKey(), e.getValue().get());
                }

                for (Map.Entry<String, Supplier<PyObject>> e : this.objectMap.entrySet()) {
                    map.put(e.getKey(), e.getValue().get());
                }
            }
        }

        return map;
    }

    public Supplier<Optional<PyObject>> getLazy(String name) {
        return getLazy(name, true);
    }

    public Supplier<Optional<PyObject>> getLazy(String name, boolean appear) {
        return getRaw(name, appear);
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
        Supplier<PyObject> getter = this.objectMap.get(name);
        if (getter == null) {
            if (!appear) {
                return getRawAppearOnly(name);
            }
        }

        if (getter == null) {
            if (this.parent != null) {
                return this.parent.getRaw(name, appear);
            }

            return this.emptyObjectReadAccessor;
        }

        return () -> Optional.ofNullable(getter.get());
    }

    public Optional<PyObject> getAppearOnly(String name) {
        return getRawAppearOnly(name).get();
    }

    public Supplier<Optional<PyObject>> getRawAppearOnly(String name) {
        Supplier<PyObject> getter = null;
        if (this.notAppearObjectMap != null) {
            getter = this.notAppearObjectMap.get(name);
        }
        if (getter == null) {
            if (this.parent != null) {
                return this.parent.getRawAppearOnly(name);
            }

            return this.emptyObjectReadAccessor;
        }

        final Supplier<PyObject> g = getter;

        return () -> Optional.ofNullable(g.get());
    }

    public boolean containsKey(String name) {
        return containsKey(name, true);
    }

    public boolean containsKey(String name, boolean appear) {
        if (appear) {
            if (this.notAppearObjectMap == null || !this.notAppearObjectMap.containsKey(name)) {
                return false;
            }
        }

        return this.objectMap.containsKey(name);
    }
}
