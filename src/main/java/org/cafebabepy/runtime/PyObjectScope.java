package org.cafebabepy.runtime;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by yotchang4s on 2017/05/24.
 */
public class PyObjectScope {

    private final PyObject source;

    private volatile Map<String, PyObject> objectMap;

    private volatile Map<String, PyObject> notAppearObjectMap;

    public PyObjectScope(PyObject source) {
        this.source = source;
    }

    public final void put(String name, PyObject object) {
        put(name, object, true);
    }

    public void put(String name, PyObject object, boolean appear) {
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

    public final PyObject getSource() {
        return this.source;
    }

    public final Map<String, PyObject> gets() {
        return gets(true);
    }

    public Map<String, PyObject> gets(boolean appear) {
        Map<String, PyObject> map = Collections.synchronizedMap(new LinkedHashMap<>());

        if (this.objectMap != null) {
            map.putAll(this.objectMap);
        }

        if (!appear && this.notAppearObjectMap != null) {
            map.putAll(this.notAppearObjectMap);
        }

        return map;
    }

    public final Optional<PyObject> getThisOnly(String name) {
        return getThisOnly(name, true);
    }

    public Optional<PyObject> getThisOnly(String name, boolean appear) {
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

    public final PyObject getThisOnlyOrThrow(String name) {
        return getThisOnlyOrThrow(name, true);
    }

    public final PyObject getThisOnlyOrThrow(String name, boolean appear) {
        Optional<PyObject> objectOpt = getThisOnly(name, appear);
        if (objectOpt.isPresent()) {
            return objectOpt.get();
        }

        throw newNotFoundException(getSource(), name);
    }

    public final Optional<PyObject> get(String name) {
        return get(name, true);
    }

    public Optional<PyObject> get(String name, boolean appear) {
        Optional<PyObject> thisOnlyOpt = getThisOnly(name, appear);
        if (thisOnlyOpt.isPresent()) {
            return thisOnlyOpt;
        }

        Optional<PyObject> typesOpt = getFromTypes(name, appear);
        if (typesOpt.isPresent()) {
            return typesOpt;
        }

        Optional<PyObject> typeOpt = getFromType(name, appear);
        if (typeOpt.isPresent()) {
            return typeOpt;
        }

        Optional<PyObject> parentOpt = getFromParent(name, appear);
        if (parentOpt.isPresent()) {
            return parentOpt;
        }

        return Optional.empty();
    }

    public Optional<PyObject> getFromTypes(String name, boolean appear) {
        for (PyObject type : getSource().getTypes()) {
            Optional<PyObject> typeObject = type.getScope().getThisOnly(name, appear);
            if (typeObject.isPresent()) {
                return typeObject;
            }
        }

        return Optional.empty();
    }

    public Optional<PyObject> getFromType(String name, boolean appear) {
        if (getSource().getRuntime().isInstance(getSource(), "builtins.type")) {
            Optional<PyObject> typeObject =
                    getSource().getRuntime().typeOrThrow("builtins.type").getScope().get(name, appear);
            if (typeObject.isPresent()) {
                return typeObject;
            }
        }

        return Optional.empty();
    }

    public Optional<PyObject> getFromParent(String name, boolean appear) {
        PyObjectScope sourceScope = getSource().getScope();
        if (sourceScope != this) { // not self
            return sourceScope.get(name, appear);
        }

        return Optional.empty();
    }

    public final PyObject getOrThrow(String name) {
        return getOrThrow(name, true);
    }

    public final PyObject getOrThrow(String name, boolean appear) {
        Optional<PyObject> objectOpt = get(name, appear);
        if (objectOpt.isPresent()) {
            return objectOpt.get();
        }

        throw newNotFoundException(getSource(), name);
    }

    public Optional<PyObject> getAppearOnly(String name) {
        PyObject object = null;
        if (this.notAppearObjectMap != null) {
            object = this.notAppearObjectMap.get(name);
        }
        if (object == null) {
            return Optional.empty();
        }

        return Optional.of(object);
    }

    public boolean containsKey(String name) {
        return containsKey(name, true);
    }

    public boolean containsKey(String name, boolean appear) {
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

    private RaiseException newNotFoundException(PyObject source, String name) {
        if (source.isModule()) {
            throw source.getRuntime().newRaiseException("builtins.AttributeError",
                    "module '" + source.getName() + "' has no attribute '" + name + "'");

        } else if (source.isType()) {
            throw source.getRuntime().newRaiseException("builtins.AttributeError",
                    "type object '" + source.getName() + "' has no attribute '" + name + "'");

        } else {
            throw source.getRuntime().newRaiseException("builtins.AttributeError",
                    "'" + source.getName() + "' object has no attribute '" + name + "'");

        }
    }
}
