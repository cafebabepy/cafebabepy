package org.cafebabepy.runtime.module;

import org.cafebabepy.annotation.DefineCafeBabePyModule;
import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.util.ModuleOrClassSplitter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by yotchang4s on 2017/05/30.
 */
public abstract class AbstractCafeBabePyModule extends AbstractAbstractCafeBabePyAny {

    private final static String[] BASE_NAMES = {"builtins.object"};

    private String moduleName;

    private String name;

    private boolean appear;

    private List<PyObject> types;

    protected AbstractCafeBabePyModule(Python runtime) {
        super(runtime);
    }

    @Override
    String[] getBaseNames() {
        return BASE_NAMES;
    }

    @Override
    public List<PyObject> getTypes() {
        PyObject object = typeOrThrow("builtins.object");
        this.types = Arrays.asList(this, object);
        this.types = Collections.unmodifiableList(Collections.synchronizedList(this.types));

        return this.types;
    }

    @Override
    public void defineClass() {
        Class<?> clazz = getClass();

        DefineCafeBabePyModule defineCafeBabePyModule = clazz.getAnnotation(DefineCafeBabePyModule.class);
        if (defineCafeBabePyModule == null) {
            throw new CafeBabePyException(
                    "DefineCafeBabePyModule or DefineCafeBabePyModule annotation is not defined " + clazz.getName());
        }

        this.moduleName = defineCafeBabePyModule.name();
        ModuleOrClassSplitter splitter = new ModuleOrClassSplitter(defineCafeBabePyModule.name());
        this.name = splitter.getSimpleName();
        this.appear = true;
        this.runtime.defineModule(this);
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
        throw getRuntime().newRaiseTypeError("'" + getName() + "' object is not callable");
    }

    @Override
    public PyObject getType() {
        return this.runtime.getBuiltinsModule().getObjectOrThrow("module");
    }

    @Override
    public Optional<String> getModuleName() {
        return Optional.ofNullable(this.moduleName);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getFullName() {
        return this.moduleName;
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
