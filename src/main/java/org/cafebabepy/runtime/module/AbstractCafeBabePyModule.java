package org.cafebabepy.runtime.module;

import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;

/**
 * Created by yotchang4s on 2017/05/30.
 */
public abstract class AbstractCafeBabePyModule extends AbstractAbstractCafeBabePyAny {

    private final static String[] BASE_NAMES = {"builtins.module"};

    private String name;

    protected AbstractCafeBabePyModule(Python runtime) {
        super(runtime, true);
    }

    @Override
    final String[] getBaseNames() {
        return BASE_NAMES;
    }

    @Override
    public void defineClass() {
        Class<?> clazz = getClass();

        DefinePyModule definePyModule = clazz.getAnnotation(DefinePyModule.class);
        if (definePyModule == null) {
            throw new CafeBabePyException(
                    "DefinePyModule or DefinePyModule annotation is not defined " + clazz.getName());
        }

        this.name = definePyModule.name();
        this.runtime.defineModule(this);
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("builtins.module", false);
    }

    @Override
    public final PyObject getModule() {
        return this;
    }

    @Override
    public final String getName() {
        return this.name;
    }

    @Override
    public final boolean isType() {
        return false;
    }

    @Override
    public final boolean isModule() {
        return true;
    }

    @Override
    public final PyObject call(PyObject... args) {
        throw getRuntime().newRaiseTypeError("'" + getFullName() + "' object is not callable");
    }
}
