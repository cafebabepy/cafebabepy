package org.cafebabepy.runtime.object;

import org.cafebabepy.runtime.Python;

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

    public PyIntObject add(PyIntObject other) {
        return new PyIntObject(this.runtime, this.value + other.value);
    }

    public PyIntObject sub(PyIntObject other) {
        return new PyIntObject(this.runtime, this.value - other.value);
    }

    @Override
    public String asJavaString() {
        return String.valueOf(this.value);
    }
}
