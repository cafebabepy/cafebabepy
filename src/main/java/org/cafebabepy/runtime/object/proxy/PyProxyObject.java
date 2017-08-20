package org.cafebabepy.runtime.object.proxy;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.PyObjectScope;
import org.cafebabepy.runtime.Python;

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
    public PyObjectScope getScope() {
        return this.source.getScope();
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
    public void preInitialize() {
        this.source.preInitialize();
    }

    @Override
    public void postInitialize() {
        this.source.postInitialize();
    }

    @Override
    public boolean isType() {
        return this.source.isType();
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
    public boolean isAppear() {
        return this.source.isAppear();
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
    public PyObject getStr() {
        return this.source.getStr();
    }

    @Override
    public String asJavaString() {
        return this.source.asJavaString();
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
    public PyObject getCallable() {
        return this.source.getCallable();
    }

    @Override
    public PyObject call() {
        return this.source.call();
    }

    @Override
    public PyObject call(PyObject arg1) {
        return this.source.call(arg1);
    }

    @Override
    public PyObject call(PyObject arg1, PyObject arg2) {
        return this.source.call(arg1, arg2);
    }

    @Override
    public PyObject call(PyObject arg1, PyObject arg2, PyObject arg3) {
        return this.source.call(arg1, arg2, arg3);
    }

    @Override
    public PyObject call(PyObject arg1, PyObject arg2, PyObject arg3, PyObject arg4) {
        return this.source.call(arg1, arg2, arg3, arg4);
    }

    @Override
    public PyObject call(PyObject arg1, PyObject arg2, PyObject arg3, PyObject arg4, PyObject arg5) {
        return this.source.call(arg1, arg2, arg3, arg4, arg5);
    }

    @Override
    public PyObject call(PyObject arg1, PyObject... args) {
        return this.source.call(arg1, args);
    }

    @Override
    public PyObject call(PyObject arg1, PyObject arg2, PyObject... args) {
        return this.source.call(arg1, arg2, args);
    }

    @Override
    public PyObject call(PyObject arg1, PyObject arg2, PyObject arg3, PyObject... args) {
        return this.source.call(arg1, arg2, arg3, args);
    }

    @Override
    public PyObject call(PyObject arg1, PyObject arg2, PyObject arg3, PyObject arg4, PyObject... args) {
        return this.source.call(arg1, arg2, arg3, arg4, args);
    }

    @Override
    public PyObject call(PyObject arg1, PyObject arg2, PyObject arg3, PyObject arg4, PyObject arg5, PyObject... args) {
        return this.source.call(arg1, arg2, arg3, arg4, arg5, args);
    }

    @Override
    public PyObject call(PyObject... args) {
        return this.source.call(args);
    }
}
