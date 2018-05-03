package org.cafebabepy.runtime.object.java;

import org.cafebabepy.runtime.Python;

/**
 * Created by yotchang4s on 2017/06/16.
 */
public class PyFalseObject extends PyBoolObject {

    public PyFalseObject(Python runtime) {
        super(runtime, false);
    }

    @Override
    public boolean isFalse() {
        return true;
    }
}
