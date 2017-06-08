package org.cafebabepy.runtime.module;

import org.cafebabepy.annotation.DefineCafeBabePyFunction;
import org.cafebabepy.runtime.*;
import org.cafebabepy.runtime.object.java.JavaPyFunctionObject;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yotchang4s on 2017/05/30.
 */
abstract class AbstractAbstractCafeBabePyAny extends AbstractPyObject {

    AbstractAbstractCafeBabePyAny(Python runtime) {
        super(runtime, new PyObjectScope());

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
                if (c != method.getDeclaringClass()) {
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

                defineMemberNames.add(name);
            }
        }
    }
}
