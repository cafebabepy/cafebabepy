package org.cafebabepy.runtime;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by yotchang4s on 2017/05/24.
 */
public class PyObjectScope {

    private final PyObjectScope parent;

    private volatile Map<PyObject, PyObjectProvider> objectProviderMap;

    private volatile Map<PyObject, PyObjectProvider> notAppearObjectProviderMap;

    private volatile Map<PyObject, PyObject> objectMap;

    private volatile Map<PyObject, PyObject> notAppearObjectMap;

    public PyObjectScope() {
        this.parent = null;
    }

    public PyObjectScope(PyObjectScope parent) {
        this.parent = parent;
    }

    public final void put(PyObjectScope scope) {
        put(scope, true);
    }

    public final void put(PyObjectScope scope, boolean appear) {
        if (appear) {
            if (scope.objectMap != null) {
                if (this.objectMap == null) {
                    synchronized (this) {
                        if (this.objectMap == null) {
                            this.objectMap = Collections.synchronizedMap(new LinkedHashMap<>());
                        }
                    }
                }

                this.objectMap.putAll(scope.objectMap);
            }

            if (scope.objectProviderMap != null) {
                if (this.objectProviderMap != null) {
                    this.objectProviderMap.putAll(scope.objectProviderMap);
                }
            }

        } else {
            if (scope.notAppearObjectMap != null) {
                if (this.notAppearObjectMap == null) {
                    synchronized (this) {
                        if (this.notAppearObjectMap == null) {
                            this.notAppearObjectMap = Collections.synchronizedMap(new LinkedHashMap<>());
                        }
                    }
                }

                this.notAppearObjectMap.putAll(scope.notAppearObjectMap);
            }

            if (scope.notAppearObjectProviderMap != null) {
                if (this.notAppearObjectProviderMap != null) {
                    this.notAppearObjectProviderMap.putAll(scope.notAppearObjectProviderMap);
                }
            }
        }
    }

    private Map<PyObject, PyObject> initializeObjectMap(boolean appear) {
        if (appear) {
            if (this.objectMap == null) {
                synchronized (this) {
                    if (this.objectMap == null) {
                        this.objectMap = Collections.synchronizedMap(new LinkedHashMap<>());
                    }
                }
            }

            return this.objectMap;

        } else {
            if (this.notAppearObjectMap == null) {
                synchronized (this) {
                    if (this.notAppearObjectMap == null) {
                        this.notAppearObjectMap = Collections.synchronizedMap(new LinkedHashMap<>());
                    }
                }
            }

            return this.notAppearObjectMap;
        }
    }

    private Map<PyObject, PyObjectProvider> initializeObjectProviderMap(boolean appear) {
        if (appear) {
            if (this.objectProviderMap == null) {
                synchronized (this) {
                    if (this.objectProviderMap == null) {
                        this.objectProviderMap = Collections.synchronizedMap(new LinkedHashMap<>());
                    }
                }
            }

            return this.objectProviderMap;

        } else {
            if (this.notAppearObjectProviderMap == null) {
                synchronized (this) {
                    if (this.notAppearObjectProviderMap == null) {
                        this.notAppearObjectProviderMap = Collections.synchronizedMap(new LinkedHashMap<>());
                    }
                }
            }

            return this.notAppearObjectProviderMap;
        }
    }

    public final void put(PyObject name, PyObjectProvider provider) {
        put(name, provider, true);
    }

    public final void put(PyObject name, PyObjectProvider provider, boolean appear) {
        initializeObjectProviderMap(appear).put(name, provider);
    }

    public final void put(PyObject name, PyObject object) {
        put(name, object, true);
    }

    public void put(PyObject name, PyObject object, boolean appear) {
        if (appear) {
            if (this.objectProviderMap != null) {
                this.objectProviderMap.remove(name);
            }

        } else {
            if (this.notAppearObjectProviderMap != null) {
                this.notAppearObjectProviderMap.remove(name);
            }
        }
        initializeObjectMap(appear).put(name, object);
    }

    public final Optional<PyObject> remove(PyObject name) {
        return remove(name, true);
    }

    public final Optional<PyObject> remove(PyObject name, boolean appear) {
        if (appear) {
            if (this.objectProviderMap != null) {
                PyObjectProvider o = this.objectProviderMap.remove(name);
                if (o != null) {
                    return Optional.of(o.get());
                }
            }

            if (this.objectMap != null) {
                PyObject o = this.objectMap.remove(name);
                if (o != null) {
                    return Optional.of(o);
                }
            }

        } else {
            if (this.notAppearObjectProviderMap != null) {
                PyObjectProvider o = this.notAppearObjectProviderMap.remove(name);
                if (o != null) {
                    return Optional.of(o.get());
                }
            }

            if (this.notAppearObjectMap != null) {
                PyObject o = this.notAppearObjectMap.remove(name);
                if (o != null) {
                    return Optional.of(o);
                }
            }
        }

        return Optional.empty();
    }

    public final Optional<PyObjectScope> getParent() {
        return Optional.ofNullable(this.parent);
    }

    public final Map<PyObject, PyObject> getsRaw() {
        return getsRaw(true);
    }

    public Map<PyObject, PyObject> getsRaw(boolean appear) {
        return initializeObjectMap(appear);
    }

    public final Map<PyObject, PyObject> gets() {
        return gets(true);
    }

    public Map<PyObject, PyObject> gets(boolean appear) {
        Map<PyObject, PyObject> map = Collections.synchronizedMap(new LinkedHashMap<>());

        if (appear) {
            if (this.objectProviderMap != null) {
                this.objectProviderMap.forEach((key, value) -> map.put(key, value.get()));
            }
            if (this.objectMap != null) {
                map.putAll(this.objectMap);
            }

        } else {
            if (this.notAppearObjectProviderMap != null) {
                this.notAppearObjectProviderMap.forEach((key, value) -> map.put(key, value.get()));
            }
            if (this.notAppearObjectMap != null) {
                map.putAll(this.notAppearObjectMap);
            }
        }

        return map;
    }

    public final Optional<PyObject> get(PyObject name) {
        return get(name, true);
    }

    public Optional<PyObject> get(PyObject name, boolean appear) {
        if (this.objectProviderMap != null) {
            synchronized (this) {
                PyObjectProvider provider = this.objectProviderMap.get(name);
                if (provider != null) {
                    PyObject object = provider.get();
                    this.objectProviderMap.remove(name);
                    this.objectMap.put(name, object);
                }
            }
        }
        if (this.objectMap != null) {
            PyObject object = this.objectMap.get(name);
            if (object != null) {
                return Optional.of(object);
            }
        }

        if (!appear) {
            if (this.notAppearObjectProviderMap != null) {
                synchronized (this) {
                    PyObjectProvider provider = this.notAppearObjectProviderMap.get(name);
                    if (provider != null) {
                        PyObject object = provider.get();
                        this.notAppearObjectProviderMap.remove(name);
                        this.notAppearObjectMap.put(name, object);
                    }
                }
            }
            if (this.notAppearObjectMap != null) {
                PyObject object = this.notAppearObjectMap.get(name);
                if (object != null) {
                    return Optional.of(object);
                }
            }
        }

        return Optional.empty();
    }

    public boolean containsKey(PyObject name) {
        return containsKey(name, true);
    }

    public boolean containsKey(PyObject name, boolean appear) {
        if (this.objectProviderMap != null && this.objectProviderMap.containsKey(name)) {
            return true;
        }
        if (this.objectMap != null && this.objectMap.containsKey(name)) {
            return true;
        }
        if (!appear) {
            if (this.notAppearObjectProviderMap != null && this.notAppearObjectProviderMap.containsKey(name)) {
                return true;
            }
            if (this.notAppearObjectMap != null && this.notAppearObjectMap.containsKey(name)) {
                return true;
            }
        }

        return false;
    }
}
