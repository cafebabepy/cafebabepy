package org.cafebabepy.runtime.object.proxy;

import org.cafebabepy.runtime.Frame;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;

import java.util.LinkedHashMap;
import java.util.List;

public class PyProxyObject implements PyObject {

    private final PyObject source;

    public PyProxyObject(PyObject source) {
        this.source = source;
    }

    public PyObject getSource() {
        return this.source;
    }

    @Override
    public PyObject getType() {
        return this.source.getType();
    }

    @Override
    public List<PyObject> getBases() {
        return this.source.getBases();
    }

    @Override
    public List<PyObject> getTypes() {
        return this.source.getTypes();
    }

    @Override
    public Python getRuntime() {
        return this.source.getRuntime();
    }

    @Override
    public Frame getFrame() {
        return this.source.getFrame();
    }

    @Override
    public PyObject getModule() {
        return this.source.getModule();
    }

    @Override
    public String getName() {
        return this.source.getName();
    }

    @Override
    public String getFullName() {
        return this.source.getFullName();
    }

    @Override
    public void initialize() {
        this.source.initialize();
    }

    @Override
    public boolean isType() {
        return this.source.isType();
    }

    @Override
    public boolean isFromClass() {
        return this.source.isFromClass();
    }

    @Override
    public boolean isModule() {
        return this.source.isModule();
    }

    @Override
    public boolean isCallable() {
        return this.source.isCallable();
    }

    @Override
    public boolean isNone() {
        return this.source.isNone();
    }

    @Override
    public boolean isNotImplemented() {
        return this.source.isNotImplemented();
    }

    @Override
    public boolean isEllipsis() {
        return false;
    }

    @Override
    public boolean isException() {
        return this.source.isException();
    }

    @Override
    public boolean isTrue() {
        return this.source.isTrue();
    }

    @Override
    public boolean isFalse() {
        return this.source.isFalse();
    }

    @Override
    public boolean existsDict() {
        return this.source.existsDict();
    }

    @Override
    public <T> T toJava(Class<T> clazz) {
        return this.source.toJava(clazz);
    }

    @Override
    public PyObject call(PyObject... args) {
        return this.source.call(args);
    }

    @Override
    public PyObject call(PyObject[] args, LinkedHashMap<String, PyObject> keywords) {
        return this.source.call(args, keywords);
    }

    @Override
    public boolean equals(Object other) {
        return this.source.equals(other);
    }
}
