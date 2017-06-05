package org.cafebabepy.runtime.module;

import org.cafebabepy.annotation.DefineCafeBabePyFunction;
import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.PyObjectScope;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.java.JavaPyFunctionObject;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yotchang4s on 2017/05/30.
 */
abstract class AbstractAbstractCafeBabePyAny implements PyObject {

    protected final Python runtime;

    protected final PyObjectScope scope;

    private Map<String, Object> javaObjectMap;

    public AbstractAbstractCafeBabePyAny(Python runtime) {
        this.runtime = runtime;
        this.scope = new PyObjectScope();

        Class<?> clazz = getClass();
        defineClass(clazz);
        defineClassMember(clazz);
    }

    abstract void defineClass(Class<?> clazz);

    protected void defineClassMember(Class<?> clazz) {
        // Check duplicate
        Map<Class<?>, Set<String>> defineNamesMemberNamesMap = new HashMap<>();

        for (Class<?> c = clazz; c != Object.class; c = c.getSuperclass()) {
            for (Method method : c.getMethods()) {
                // Same class only
                if(c != method.getDeclaringClass()){
                    continue;
                }
                String name;

                DefineCafeBabePyFunction defineCafeBabePyFunction = method.getAnnotation(DefineCafeBabePyFunction.class);

                if (defineCafeBabePyFunction != null) {
                    name = defineCafeBabePyFunction.name();

                } else {
                    continue;
                }

                Set<String> defineMemberNames = defineNamesMemberNamesMap.computeIfAbsent(
                        c, k -> new HashSet<>());

                if (defineCafeBabePyFunction != null) {
                    if (defineMemberNames.contains(defineCafeBabePyFunction.name())) {
                        throw new CafeBabePyException(
                                "Duplicate '" + defineCafeBabePyFunction.name() + "' function");
                    }

                    JavaPyFunctionObject f = new JavaPyFunctionObject(
                            getRuntime(),
                            this,
                            name,
                            method);

                    getScope().put(f.getName(), f);
                }

                defineMemberNames.add(name);
            }
        }
    }

    @Override
    public final boolean isNone() {
        return false;
    }

    @Override
    public final PyObject getStr() {
        return this.runtime.str(asJavaString());
    }

    @Override
    public final boolean isException() {
        return false;
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
}
