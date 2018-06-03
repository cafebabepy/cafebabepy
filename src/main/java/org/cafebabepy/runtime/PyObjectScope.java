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
        Map<PyObject, PyObject> source = scope.gets(appear);

        if (appear) {
            if (this.objectMap == null) {
                synchronized (this) {
                    if (this.objectMap == null) {
                        this.objectMap = Collections.synchronizedMap(new LinkedHashMap<>());
                    }
                }
            }

            this.objectMap.putAll(source);

        } else {
            if (this.notAppearObjectMap == null) {
                synchronized (this) {
                    if (this.notAppearObjectMap == null) {
                        this.notAppearObjectMap = Collections.synchronizedMap(new LinkedHashMap<>());
                    }
                }
            }

            this.notAppearObjectMap.putAll(source);
        }
    }

    public final void put(PyObject name, PyObject object) {
        put(name, object, true);
    }

    public void put(PyObject name, PyObject object, boolean appear) {
        if (appear) {
            if (this.objectMap == null) {
                synchronized (this) {
                    if (this.objectMap == null) {
                        this.objectMap = Collections.synchronizedMap(new LinkedHashMap<>());
                    }
                }
            }

            this.objectMap.put(name, object);

        } else {
            if (this.notAppearObjectMap == null) {
                synchronized (this) {
                    if (this.notAppearObjectMap == null) {
                        this.notAppearObjectMap = Collections.synchronizedMap(new LinkedHashMap<>());
                    }
                }
            }

            this.notAppearObjectMap.put(name, object);
        }
    }

    public final Optional<PyObject> remove(PyObject name) {
        return remove(name, true);
    }

    public final Optional<PyObject> remove(PyObject name, boolean appear) {
        Map<PyObject, PyObject> map;
        if (appear) {
            map = this.objectMap;

        } else {
            map = this.notAppearObjectMap;
        }

        if (map != null) {
            PyObject removeElement = map.remove(name);

            return Optional.ofNullable(removeElement);
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

    public final Map<PyObject, PyObject> gets() {
        return gets(true);
    }

    public Map<PyObject, PyObject> gets(boolean appear) {
        Map<PyObject, PyObject> map = Collections.synchronizedMap(new LinkedHashMap<>());

        if (appear) {
            if (this.objectMap != null) {
                map.putAll(this.objectMap);
            }

        } else {
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
        if (this.objectMap != null) {
            PyObject object = this.objectMap.get(name);
            if (object != null) {
                return Optional.of(object);
            }
        }

        if (!appear) {
            return getAppearOnly(name);
        }

        return Optional.empty();
    }

    public Optional<PyObject> getAppearOnly(PyObject name) {
        PyObject object = null;
        if (this.notAppearObjectMap != null) {
            object = this.notAppearObjectMap.get(name);
        }
        if (object == null) {
            return Optional.empty();
        }

        return Optional.of(object);
    }

    public boolean containsKey(PyObject name) {
        return containsKey(name, true);
    }

    public boolean containsKey(PyObject name, boolean appear) {
        if (this.objectMap != null && this.objectMap.containsKey(name)) {
            return true;
        }
        if (!appear) {
            if (this.notAppearObjectMap != null && this.notAppearObjectMap.containsKey(name)) {
                return true;
            }
        }

        return false;
    }
}
