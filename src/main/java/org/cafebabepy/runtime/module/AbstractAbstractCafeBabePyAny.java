package org.cafebabepy.runtime.module;

import org.cafebabepy.annotation.DefineCafeBabePyFunction;
import org.cafebabepy.runtime.*;
import org.cafebabepy.runtime.object.java.JavaPyFunctionObject;

import java.lang.reflect.Method;
import java.util.*;

import static org.cafebabepy.util.ProtocolNames.__call__;

/**
 * Created by yotchang4s on 2017/05/30.
 */
abstract class AbstractAbstractCafeBabePyAny extends AbstractPyObject {

    AbstractAbstractCafeBabePyAny(Python runtime) {
        super(runtime, new PyObjectScope());
    }

    @Override
    public void preInitialize() {
        defineClass();
        defineClassMember();
    }

    @Override
    public void postInitialize() {
        List<PyObject> superTypes = getSuperTypes();
        for (int i = superTypes.size() - 1; i >= 0; i--) {
            PyObject superType = superTypes.get(i);

            synchronized (this) {
                if (!getScope().containsKey(superType.getName())) {
                    for (Map.Entry<String, PyObject> e : superType.getScope().gets().entrySet()) {
                        getScope().put(e.getKey(), e.getValue());
                    }
                }
            }
        }
    }

    public abstract void defineClass();

    private void defineClassMember() {
        Class<?> clazz = getClass();

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

                if (__call__.equals(f.getName())) {
                    f.getScope().put(__call__, f);
                }

                getScope().put(f.getName(), f);

                defineMemberNames.add(name);
            }
        }
    }

    @Override
    public boolean isNone() {
        return false;
    }
}
