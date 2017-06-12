package org.cafebabepy.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Created by yotchang4s on 2017/06/09.
 */
public class LazyHashMap<K, V> implements Map<K, V> {

    private Map<K, V> source;

    public LazyHashMap() {
        this.source = new HashMap<>();
    }

    public LazyHashMap(int initialCapacity) {
        this.source = new HashMap<>(initialCapacity);
    }

    public LazyHashMap(Map<K, V> source) {
        this.source = new HashMap<>(source);
    }

    @Override
    public int size() {
        return this.source.size();
    }

    @Override
    public boolean isEmpty() {
        return this.source.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.source.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.source.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return this.source.get(key);
    }

    public Supplier<V> getLazy(Object key) {
        return () -> this.source.get(key);
    }

    @Override
    public V put(K key, V value) {
        return this.source.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return this.source.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        this.source.putAll(m);
    }

    @Override
    public void clear() {
        this.source.clear();
    }

    @Override
    public Set<K> keySet() {
        return this.source.keySet();
    }

    @Override
    public Collection<V> values() {
        return this.source.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return this.source.entrySet();
    }
}