package org.cafebabepy.runtime.object;

import org.cafebabepy.runtime.Python;

/**
 * Created by yotchang4s on 2017/06/16.
 */
public class PyFalseObject extends PyBoolObject {

    public PyFalseObject(Python runtime) {
        super(runtime, runtime.typeOrThrow("builtins.bool"));
    }

    @Override
    public boolean isFalse() {
        return true;
    }

    @Override
    public String asJavaString() {
        return "False";
    }
}
