package org.cafebabepy.runtime.object.java;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.AbstractPyObjectObject;

/**
 * Created by yotchang4s on 2018/08/11.
 */
public class PyFloatObject extends AbstractPyObjectObject {

    // FIXME CPythonのsys.maxsizeに対応する
    private final float value;

    public PyFloatObject(Python runtime, float value) {
        super(runtime);

        this.value = value;
    }

    public float getValue() {
        return this.value;
    }

    public PyBoolObject eq(PyFloatObject other) {
        return this.runtime.bool(this.value == other.value);
    }

    public PyBoolObject ne(PyFloatObject other) {
        return this.runtime.bool(this.value != other.value);
    }

    public PyFloatObject add(PyFloatObject other) {
        return this.runtime.number(this.value + other.value);
    }

    public PyFloatObject sub(PyFloatObject other) {
        return this.runtime.number(this.value - other.value);
    }

    public PyFloatObject mul(PyFloatObject other) {
        return this.runtime.number(this.value * other.value);
    }

    public PyFloatObject mod(PyFloatObject other) {
        return this.runtime.number(this.value % other.value);
    }

    public PyBoolObject lt(PyFloatObject other) {
        return this.runtime.bool(this.value < other.value);
    }

    public PyBoolObject le(PyFloatObject other) {
        return this.runtime.bool(this.value <= other.value);
    }

    public PyBoolObject gt(PyFloatObject other) {
        return this.runtime.bool(this.value > other.value);
    }

    public PyBoolObject ge(PyFloatObject other) {
        return this.runtime.bool(this.value >= other.value);
    }

    public PyBoolObject bool() {
        return this.runtime.bool(this.value != 0);
    }

    public PyObject pos() {
        return this.runtime.number(+this.value);
    }

    public PyObject neg() {
        return this.runtime.number(-this.value);
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("builtins.float");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T toJava(Class<T> clazz) {
        if (float.class.isAssignableFrom(clazz) || Integer.class.isAssignableFrom(clazz)) {
            return (T) Float.valueOf(this.value);

        } else if (String.class.isAssignableFrom(clazz)) {
            return (T) String.valueOf(this.value);
        }

        return super.toJava(clazz);
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }
}
