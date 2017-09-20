package org.cafebabepy.runtime.object.java;

import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.AbstractPyObjectObject;

/**
 * Created by yotchang4s on 2017/06/19.
 */
public class PyStrObject extends AbstractPyObjectObject {

    private final String value;

    public PyStrObject(Python runtime, String value) {
        super(runtime, runtime.typeOrThrow("builtins.str"));

        this.value = value;
    }

    public PyStrObject add(PyStrObject str) {
        return this.runtime.str(this.value + str.value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T toJava(Class<T> clazz) {
        if (String.class.isAssignableFrom(clazz)) {
            return (T) this.value;
        }

        return super.toJava(clazz);
    }
}
