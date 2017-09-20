package org.cafebabepy.runtime.object.java;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.AbstractPyObjectObject;

/**
 * Created by yotchang4s on 2017/06/19.
 */
public class PyIntObject extends AbstractPyObjectObject {

    // FIXME CPythonのsys.maxsizeに対応する
    private final int value;

    public PyIntObject(Python runtime, int value) {
        super(runtime, runtime.typeOrThrow("builtins.int"));

        this.value = value;
    }

    public int getIntValue() {
        return this.value;
    }

    public PyBoolObject eq(PyIntObject other) {
        return this.runtime.bool(this.value == other.value);
    }

    public PyIntObject add(PyIntObject other) {
        return this.runtime.number(this.value + other.value);
    }

    public PyIntObject sub(PyIntObject other) {
        return this.runtime.number(this.value - other.value);
    }

    public PyIntObject mul(PyIntObject other) {
        return this.runtime.number(this.value * other.value);
    }

    public PyIntObject mod(PyIntObject other) {
        return this.runtime.number(this.value % other.value);
    }

    public PyBoolObject lt(PyIntObject other) {
        return this.runtime.bool(this.value < other.value);
    }

    public PyBoolObject le(PyIntObject other) {
        return this.runtime.bool(this.value <= other.value);
    }

    public PyBoolObject gt(PyIntObject other) {
        return this.runtime.bool(this.value > other.value);
    }

    public PyBoolObject ge(PyIntObject other) {
        return this.runtime.bool(this.value >= other.value);
    }

    public PyBoolObject bool() {
        return this.runtime.bool(this.value != 0);
    }

    public PyIntObject invert() {
        return this.runtime.number(~this.value);
    }

    public PyObject pos() {
        return this.runtime.number(+this.value);
    }

    public PyObject neg() {
        return this.runtime.number(-this.value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T toJava(Class<T> clazz) {
        if (int.class.isAssignableFrom(clazz) || Integer.class.isAssignableFrom(clazz)) {
            return (T) Integer.valueOf(this.value);

        } else if (String.class.isAssignableFrom(clazz)) {
            return (T)String.valueOf(this.value);
        }

        return super.toJava(clazz);
    }
}
