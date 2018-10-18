package org.cafebabepy.runtime.object.java;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.AbstractPyObjectObject;

/**
 * Created by yotchang4s on 2018/10/13.
 */
public class PyBytesObject extends AbstractPyObjectObject {

    private final byte[] value;

    public PyBytesObject(Python runtime, byte[] value) {
        super(runtime);

        this.value = value;
    }

    public byte[] getValue() {
        return this.value;
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("bytes");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T toJava(Class<T> clazz) {
        if (byte[].class.isAssignableFrom(clazz)) {
            return (T)this.value;

        }

        return super.toJava(clazz);
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }
}
