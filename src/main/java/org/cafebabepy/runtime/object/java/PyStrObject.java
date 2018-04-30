package org.cafebabepy.runtime.object.java;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.AbstractPyObjectObject;

/**
 * Created by yotchang4s on 2017/06/19.
 */
public class PyStrObject extends AbstractPyObjectObject {

    private final String value;

    public PyStrObject(Python runtime, String value) {
        super(runtime);

        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public PyStrObject add(PyStrObject str) {
        return this.runtime.str(this.value + str.value);
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("builtins.str");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T toJava(Class<T> clazz) {
        if (String.class.isAssignableFrom(clazz)) {
            return (T) this.value;
        }

        return super.toJava(clazz);
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;

        } else if (other instanceof PyStrObject) {
            PyStrObject otherStrObject = (PyStrObject) other;

            return this.value.equals(otherStrObject.value);

        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "'" + this.value + "'";
    }
}
