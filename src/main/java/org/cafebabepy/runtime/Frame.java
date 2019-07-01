package org.cafebabepy.runtime;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by yotchang4s on 2018/11/26.
 */
public class Frame {
    private final Frame module;
    private final Frame back;

    private Map<String, PyObject> locals;
    private Map<String, PyObject> notAppearLocals;

    public Frame() {
        this(null, null);
    }

    public Frame(Frame module) {
        this(module, null);
    }

    public Frame(Frame module, Frame back) {
        this.module = module;
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

    public Map<String, PyObject> getGlobals() {
        if (this.module == null) {
            return getLocals();
        }

        return this.module.getLocals();
    }

    public Map<String, PyObject> getNotAppearLocals() {
        initializeNotAppearLocals();
        return this.notAppearLocals;
    }

    public PyObject lookup(String key) {
        if (this.locals != null) {
            PyObject value = this.locals.get(key);
            if (value != null) {
                return value;
            }
        }

        if (this.back != null) {
            return this.back.lookup(key);
        }

        return null;
    }

    public Optional<Frame> getBack() {
        return Optional.ofNullable(this.back);
    }

    public Map<PyObject, PyObject> getLocalsPyObjectMap() {
        return new PyObjectMap(getLocals());
    }

    public Map<PyObject, PyObject> getGlobalsPyObjectMap() {
        return new PyObjectMap(getGlobals());
    }

    static class PyObjectMap implements Map<PyObject, PyObject> {
        private Map<String, PyObject> map;

        PyObjectMap(Map<String, PyObject> map) {
            this.map = map;
        }

        @Override
        public int size() {
            return this.map.size();
        }

        @Override
        public boolean isEmpty() {
            return this.map.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            if (key instanceof PyObject) {
                return this.map.containsKey(((PyObject) key).toJava(String.class));
            }

            return false;
        }

        @Override
        public boolean containsValue(Object value) {
            if (value instanceof PyObject) {
                return this.map.containsValue(((PyObject) value).toJava(String.class));
            }

            return false;
        }

        @Override
        public PyObject get(Object key) {
            if (key instanceof PyObject) {
                return this.map.get(((PyObject) key).toJava(String.class));
            }

            return null;
        }

        @Override
        public PyObject put(PyObject key, PyObject value) {
            return this.map.put(key.toJava(String.class), value);
        }

        @Override
        public PyObject remove(Object key) {
            if (key instanceof PyObject) {
                return this.map.remove(((PyObject) key).toJava(String.class));
            }

            return null;
        }

        @Override
        public void putAll(Map<? extends PyObject, ? extends PyObject> m) {
            for (Entry<? extends PyObject, ? extends PyObject> e : m.entrySet()) {
                this.map.put(e.getKey().toJava(String.class), e.getValue());
            }
        }

        @Override
        public void clear() {
            this.map.clear();
        }

        @Override
        public Set<PyObject> keySet() {
            throw new UnsupportedOperationException("keySet");
        }

        @Override
        public Collection<PyObject> values() {
            return this.map.values();
        }

        @Override
        public Set<Entry<PyObject, PyObject>> entrySet() {
            return this.map.entrySet().stream().map(e -> {
                Python runtime = e.getValue().getRuntime();

                PyObject key = runtime.str(e.getKey());
                PyObject value = e.getValue();

                return new Entry<PyObject, PyObject>() {
                    @Override
                    public PyObject getKey() {
                        return key;
                    }

                    @Override
                    public PyObject getValue() {
                        return value;
                    }

                    @Override
                    public PyObject setValue(PyObject value) {
                        return put(key, value);
                    }
                };

            }).collect(Collectors.toSet());
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