package org.cafebabepy.runtime.module;

import org.cafebabepy.annotation.DefineCafeBabePyModule;
import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.util.ModuleOrClassSplitter;

import java.util.Optional;

/**
 * Created by yotchang4s on 2017/05/30.
 */
public class AbstractCafeBabePyModule extends AbstractAbstractCafeBabePyAny {

    private String moduleName;

    private String name;

    private boolean appear;

    public AbstractCafeBabePyModule(Python runtime) {
        super(runtime);
    }

    void defineClass(Class<?> clazz) {
        DefineCafeBabePyModule defineCafeBabePyModule = clazz.getAnnotation(DefineCafeBabePyModule.class);
        if (defineCafeBabePyModule != null) {
            ModuleOrClassSplitter splitter = new ModuleOrClassSplitter(defineCafeBabePyModule.name());

            this.moduleName = splitter.getModuleName().orElse(null);
            this.name = splitter.getSimpleName();
            this.runtime.defineModule(this);
            this.appear = true;

        } else {
            throw new CafeBabePyException(
                    "DefineCafeBabePyModule or DefineCafeBabePyModule annotation is not defined " + clazz.getName());
        }
    }

    @Override
    public final boolean isType() {
        return false;
    }

    @Override
    public boolean isModule() {
        return true;
    }

    @Override
    public boolean isAppear() {
        return this.appear;
    }

    @Override
    public String asJavaString() {
        return "<module '" + getFullName() + "' (built-in)>";
    }

    @Override
    public PyObject getType() {
        return this.runtime.moduleOrThrow(Python.BUILTINS_MODULE_NAME).getObjectOrThrow("module");
    }

    @Override
    public Optional<String> getModuleName() {
        return Optional.ofNullable(this.moduleName);
    }

    public String getName() {
        return this.name;
    }
}
