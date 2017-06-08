package org.cafebabepy.runtime.module;

import org.cafebabepy.annotation.DefineCafeBabePyFunction;
import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.PyObjectScope;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.java.JavaPyObject;
import org.cafebabepy.util.ModuleOrClassSplitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.cafebabepy.util.ProtocolNames.*;

/**
 * Created by yotchang4s on 2017/05/30.
 */
public abstract class AbstractCafeBabePyType extends AbstractAbstractCafeBabePyAny {

    private String moduleName;

    private String name;

    private boolean appear;

    private String[] superTypeNames;

    private List<PyObject> superTypes;

    public AbstractCafeBabePyType(Python runtime) {
        super(runtime);
    }

    void defineClass(Class<?> clazz) {
        DefineCafeBabePyType defineCafeBabePyType = clazz.getAnnotation(DefineCafeBabePyType.class);
        if (defineCafeBabePyType == null) {
            throw new CafeBabePyException(
                    "DefineCafeBabePyModule annotation is not defined " + clazz.getName());
        }

        this.superTypeNames = defineCafeBabePyType.parent();

        ModuleOrClassSplitter splitter = new ModuleOrClassSplitter(defineCafeBabePyType.name());
        this.moduleName = splitter.getModuleName().orElseThrow(() ->
                new CafeBabePyException("name '"
                        + defineCafeBabePyType.name()
                        + "' is not found module")
        );

        this.name = splitter.getSimpleName();
        this.appear = defineCafeBabePyType.appear();

        PyObject module = this.runtime.module(this.moduleName).orElseThrow(() ->
                new CafeBabePyException(
                        "module '" + this.moduleName + "' is not found " + clazz.getName()));

        module.getScope().put(this.name, this, this.appear);
    }

    @Override
    public List<PyObject> getSuperTypes() {
        if (this.superTypes == null) {
            synchronized (this) {
                if (this.superTypes == null) {
                    this.superTypes = new ArrayList<>(this.superTypeNames.length);
                    for (String superTypeName : this.superTypeNames) {
                        PyObject type = this.runtime.type(superTypeName).orElseThrow(() ->
                                new CafeBabePyException(
                                        "type '" + this.name + "' parent '" + superTypeName + "' is not found")
                        );

                        this.superTypes.add(type);
                    }
                }
            }
        }

        return Collections.unmodifiableList(this.superTypes);
    }

    @Override
    public String asJavaString() {
        return "<class '" + getFullName() + "'>";
    }

    @Override
    public PyObject getType() {
        return this.runtime.moduleOrThrow("builtins").getObjectOrThrow("type");
    }

    public Optional<String> getModuleName() {
        // moduleName is not null
        return Optional.of(this.moduleName);
    }

    public String getName() {
        return this.name;
    }

    @Override
    public PyObject call(PyObject... args) {
        return getObjectOrThrow(__call__).call(args);
    }

    @DefineCafeBabePyFunction(name = __call__)
    public PyObject __call__(PyObject... args) {
        PyObject object = getObjectOrThrow(__new__).call(this);

        getObjectOrThrow(__init__).call(object, args);

        return object;
    }

    @DefineCafeBabePyFunction(name = __new__)
    public PyObject __new__(PyObject cls) {
        return new JavaPyObject(this.runtime, cls);
    }

    @DefineCafeBabePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject... args) {
    }
}
