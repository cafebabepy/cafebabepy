package org.cafebabepy.runtime;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yotchang4s on 2018/11/26.
 */
public class Frame {
    private final Frame back;

    private Map<String, PyObject> locals;
    private Map<String, PyObject> notAppearLocals;

    public Frame() {
        this(null);
    }

    public Frame(Frame back) {
        this.back = back;
    }

    private void initializeLocals() {
        if (this.locals == null) {
            synchronized (this) {
                if (this.locals == null) {
                    this.locals = new ConcurrentHashMap<>();
                }
            }
        }
    }

    private void initializeNotAppearLocals() {
        if (this.notAppearLocals == null) {
            synchronized (this) {
                if (this.notAppearLocals == null) {
                    this.notAppearLocals = new ConcurrentHashMap<>();
                }
            }
        }
    }

    public Map<String, PyObject> getLocals() {
        initializeLocals();
        return this.locals;
    }

    public Optional<PyObject> getFromNotAppearLocals(String key) {
        if (this.notAppearLocals != null) {
            return Optional.ofNullable(this.notAppearLocals.get(key));
        }

        return Optional.empty();
    }

    public Optional<PyObject> put(String key, PyObject value, boolean appear) {
        if (appear) {
            return putToLocals(key, value);

        } else {
            return putToNotAppearLocals(key, value);
        }
    }

    public Optional<PyObject> putToLocals(String key, PyObject value) {
        initializeLocals();
        return Optional.ofNullable(this.locals.put(key, value));
    }

    public Optional<PyObject> putToNotAppearLocals(String key, PyObject value) {
        initializeNotAppearLocals();
        return Optional.ofNullable(this.notAppearLocals.put(key, value));
    }

    public Optional<PyObject> removeToLocals(String key) {
        return Optional.ofNullable(this.locals.remove(key));
    }

    public boolean containsKeyFromLocals(String key) {
        if (this.locals == null) {
            return false;
        }

        return this.locals.containsKey(key);
    }

    public boolean containsKeyFromAppearLocals(String key) {
        if (this.notAppearLocals == null) {
            return false;
        }

        return this.notAppearLocals.containsKey(key);
    }

    public Optional<PyObject> getFromGlobals(String key) {
        return getFromGlobals(key, true);
    }

    public Optional<PyObject> getFromGlobals(String key, boolean appear) {
        if (this.locals != null) {
            PyObject value = this.locals.get(key);
            if (value != null) {
                return Optional.of(value);
            }
        }

        if (!appear) {
            if (this.notAppearLocals != null) {
                PyObject value = this.notAppearLocals.get(key);
                if (value != null) {
                    return Optional.of(value);
                }
            }
        }

        if (this.back != null) {
            return this.back.getFromGlobals(key, appear);
        }

        return Optional.empty();
    }

    public Optional<Frame> getBack() {
        return Optional.ofNullable(this.back);
    }

    public Map<PyObject, PyObject> toPyObjectMap() {
        return new PyObjectMap();
    }

    class PyObjectMap implements Map<PyObject, PyObject> {

        @Override
        public int size() {
            return getLocals().size();
        }

        @Override
        public boolean isEmpty() {
            return getLocals().isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            if (key instanceof PyObject) {
                return getLocals().containsKey(((PyObject) key).toJava(String.class));
            }

            return false;
        }

        @Override
        public boolean containsValue(Object value) {
            if (value instanceof PyObject) {
                return getLocals().containsValue(((PyObject) value).toJava(String.class));
            }

            return false;
        }

        @Override
        public PyObject get(Object key) {
            if (key instanceof PyObject) {
                return getLocals().get(((PyObject) key).toJava(String.class));
            }

            return null;
        }

        @Override
        public PyObject put(PyObject key, PyObject value) {
            return putToLocals(key.toJava(String.class), value).orElse(null);
        }

        @Override
        public PyObject remove(Object key) {
            if (key instanceof PyObject) {
                return getLocals().remove(((PyObject) key).toJava(String.class));
            }

            return null;
        }

        @Override
        public void putAll(Map<? extends PyObject, ? extends PyObject> m) {
            for (Entry<? extends PyObject, ? extends PyObject> e : m.entrySet()) {
                getLocals().put(e.getKey().toJava(String.class), e.getValue());
            }
        }

        @Override
        public void clear() {
            getLocals().clear();
        }

        @Override
        public Set<PyObject> keySet() {
            throw new UnsupportedOperationException("keySet");
        }

        @Override
        public Collection<PyObject> values() {
            return getLocals().values();
        }

        @Override
        public Set<Entry<PyObject, PyObject>> entrySet() {
            throw new UnsupportedOperationException("entrySet");
        }

        @Override
        public boolean equals(Object o) {
            throw new UnsupportedOperationException("equals");
        }

        @Override
        public int hashCode() {
            throw new UnsupportedOperationException("hashCode");
        }
    }
}