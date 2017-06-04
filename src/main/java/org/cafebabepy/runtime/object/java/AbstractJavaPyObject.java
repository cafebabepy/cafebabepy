package org.cafebabepy.runtime.object.java;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.PyObjectScope;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.types.PyNoneTypeType;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yotchang4s on 2017/06/03.
 */
abstract class AbstractJavaPyObject implements PyObject {

    protected final Python runtime;

    protected final PyObject type;

    protected final PyObjectScope scope;

    private volatile Map<String, Object> javaObjectMap;

    protected AbstractJavaPyObject(Python runtime, PyObject type) {
        this.runtime = runtime;
        this.type = type;
        this.scope = new PyObjectScope();
    }

    protected AbstractJavaPyObject(Python runtime, PyObject type, PyObjectScope parentScope) {
        this.runtime = runtime;
        this.type = type;
        this.scope = new PyObjectScope(parentScope);
    }

    @Override
    public PyObject getType() {
        return this.type;
    }

    @Override
    public Python getRuntime() {
        return this.runtime;
    }

    @Override
    public PyObjectScope getScope() {
        return this.scope;
    }

    @Override
    public Optional<String> getModuleName() {
        return Optional.empty();
    }

    @Override
    public void putJavaObject(String name, Object object) {
        if (this.javaObjectMap == null) {
            synchronized (this) {
                if (this.javaObjectMap == null) {
                    this.javaObjectMap = new ConcurrentHashMap<>();
                }
            }
        }

        this.javaObjectMap.put(name, object);
    }

    @Override
    public Optional<Object> getJavaObject(String name) {
        if (this.javaObjectMap == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(this.javaObjectMap.get(name));
    }

    @Override
    public final boolean isType() {
        return false;
    }

    @Override
    public final boolean isModule() {
        return false;
    }

    @Override
    public final boolean isAppear() {
        return true;
    }

    @Override
    public final boolean isNone() {
        return (getType() instanceof PyNoneTypeType);
    }

    @Override
    public final PyObject getStr() {
        return this.runtime.str(asJavaString());
    }

    @Override
    public String asJavaString() {
        int hashCode = System.identityHashCode(this);

        return "0x" + Integer.toHexString(hashCode);
    }

    @Override
    public boolean isException() {
        // FIXME 親がBaseExceptionかどうかを判定する
        return false;
    }
}
