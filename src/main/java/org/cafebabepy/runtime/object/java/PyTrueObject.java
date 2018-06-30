package org.cafebabepy.runtime.object.java;

import org.cafebabepy.runtime.Python;

/**
 * Created by yotchang4s on 2017/06/16.
 */
public class PyTrueObject extends PyBoolObject {

    public PyTrueObject(Python runtime) {
        super(runtime);
    }

    @Override
    public boolean isFalse() {
        return false;
    }
}
