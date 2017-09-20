package org.cafebabepy.runtime.module;

import org.cafebabepy.annotation.DefinePyModule;
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
    String[] getBaseNames() {
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
    public PyObject call(PyObject... args) {
        throw getRuntime().newRaiseTypeError("'" + getName() + "' object is not callable");
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("builtins.module", false);
    }

    @Override
    public PyObject getModule() {
        return this;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isType() {
        return false;
    }

    @Override
    public boolean isModule() {
        return true;
    }
}
