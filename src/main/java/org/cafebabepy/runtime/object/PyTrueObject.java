package org.cafebabepy.runtime.object;

import org.cafebabepy.runtime.Python;

/**
 * Created by yotchang4s on 2017/06/16.
 */
public class PyTrueObject extends AbstractPyObjectObject {

    public PyTrueObject(Python runtime) {
        super(runtime, runtime.typeOrThrow("builtins.bool"));
    }

    @Override
    public boolean isFalse() {
        return false;
    }

    @Override
    public String asJavaString() {
        return "True";
    }
}
