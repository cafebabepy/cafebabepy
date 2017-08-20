package org.cafebabepy.runtime.module;

import org.cafebabepy.annotation.DefineCafeBabePyFunction;
import org.cafebabepy.runtime.AbstractPyObject;
import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.PyJavaFunctionObject;

import java.lang.reflect.Method;
import java.util.*;

import static org.cafebabepy.util.ProtocolNames.__call__;

/**
 * Created by yotchang4s on 2017/05/30.
 */
abstract class AbstractAbstractCafeBabePyAny extends AbstractPyObject {

    private enum InitializeStage {
        NONE, PRE, POST
    }

    private volatile List<PyObject> bases;

    private InitializeStage initialize;

    AbstractAbstractCafeBabePyAny(Python runtime) {
        super(runtime);

        this.initialize = InitializeStage.NONE;
    }

    AbstractAbstractCafeBabePyAny(Python runtime, boolean appear) {
        super(runtime, appear);

        this.initialize = InitializeStage.NONE;
    }

    @Override
    public void preInitialize() {
        if (this.initialize != InitializeStage.NONE) {
            return;
        }

        this.initialize = InitializeStage.PRE;
        defineClass();
    }

    @Override
    public void postInitialize() {
        if (this.initialize != InitializeStage.PRE) {
            return;
        }

        this.initialize = InitializeStage.POST;
        defineClassMembers();
    }

    @Override
    public final boolean isNone() {
        return false;
    }

    @Override
    public final boolean isNotImplemented() {
        return false;
    }

    @Override
    public List<PyObject> getBases() {
        if (this.bases == null) {
            synchronized (this) {
                if (this.bases == null) {
                    String[] baseNames = getBaseNames();
                    List<PyObject> bases = new ArrayList<>(baseNames.length);

                    for (String baseName : getBaseNames()) {
                        PyObject base = this.runtime.type(baseName).orElseThrow(() ->
                                new CafeBabePyException(
                                        "type '" + getName() + "' parent '" + baseName + "' is not found")
                        );

                        bases.add(base);
                    }

                    this.bases = Collections.unmodifiableList(Collections.synchronizedList(bases));
                }
            }
        }

        return this.bases;
    }

    abstract String[] getBaseNames();

    public abstract void defineClass();

    private void defineClassMembers() {
        Class<?> clazz = getClass();

        this.runtime.type("builtins.object")
                .orElseThrow(() -> new CafeBabePyException("'builtins.object' is not found"))
                .getScope();

        for (Class<?> c = clazz.getSuperclass(); c != Object.class; c = c.getSuperclass()) {
            defineClassMember(c);
        }

        defineClassMember(clazz);
    }

    private void defineClassMember(Class<?> clazz) {
        // Check duplicate
        Set<String> defineClassMemberNamesSet = new HashSet<>();

        for (Method method : clazz.getMethods()) {
            // Same class method only
            if (clazz != method.getDeclaringClass()) {
                continue;
            }

            DefineCafeBabePyFunction defineCafeBabePyFunction = method.getAnnotation(DefineCafeBabePyFunction.class);
            if (defineCafeBabePyFunction == null) {
                continue;
            }

            if (defineClassMemberNamesSet.contains(defineCafeBabePyFunction.name())) {
                throw new CafeBabePyException(
                        "Duplicate '" + defineCafeBabePyFunction.name() + "' function");
            }

            PyJavaFunctionObject f = new PyJavaFunctionObject(
                    getRuntime(),
                    this,
                    defineCafeBabePyFunction.name(),
                    method);

            if (__call__.equals(f.getName())) {
                f.getScope().put(__call__, f);
            }

            getScope().put(f.getName(), f);

            defineClassMemberNamesSet.add(defineCafeBabePyFunction.name());
        }
    }
}
