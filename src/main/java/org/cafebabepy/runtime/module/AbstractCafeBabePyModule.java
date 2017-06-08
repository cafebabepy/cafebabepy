package org.cafebabepy.runtime.module;

import org.cafebabepy.annotation.DefineCafeBabePyModule;
import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.util.ModuleOrClassSplitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by yotchang4s on 2017/05/30.
 */
public abstract class AbstractCafeBabePyModule extends AbstractAbstractCafeBabePyAny {

    private String moduleName;

    private String name;

    private boolean appear;

    private List<PyObject> superTypes;

    public AbstractCafeBabePyModule(Python runtime) {
        super(runtime);
    }

    void defineClass(Class<?> clazz) {
        DefineCafeBabePyModule defineCafeBabePyModule = clazz.getAnnotation(DefineCafeBabePyModule.class);
        if (defineCafeBabePyModule == null) {
            throw new CafeBabePyException(
                    "DefineCafeBabePyModule or DefineCafeBabePyModule annotation is not defined " + clazz.getName());
        }

        ModuleOrClassSplitter splitter = new ModuleOrClassSplitter(defineCafeBabePyModule.name());
        if (!splitter.getModuleName().isPresent()) {
            this.moduleName = splitter.getSimpleName();

        } else {
            this.moduleName = splitter.getModuleName().get();
        }
        this.name = splitter.getSimpleName();
        this.appear = true;
        this.runtime.defineModule(this);
    }

    @Override
    public List<PyObject> getSuperTypes() {
        if (this.superTypes == null) {
            synchronized (this) {
                if (this.superTypes == null) {
                    this.superTypes = new ArrayList<>(1);
                    PyObject type = this.runtime.type("object").orElseThrow(() ->
                            new CafeBabePyException(
                                    "type '" + this.name + "' parent 'object' is not found")
                    );

                    this.superTypes.add(type);
                }
            }
        }

        return Collections.unmodifiableList(this.superTypes);
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
    public PyObject call(PyObject... args) {
        throw getRuntime().newRaiseException("builtins.TypeError",
                "'" + getName() + "' object is not callable");
    }

    @Override
    public PyObject getType() {
        return this.runtime.getBuiltinsModule().getObjectOrThrow("module");
    }

    @Override
    public Optional<String> getModuleName() {
        return Optional.ofNullable(this.moduleName);
    }

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
