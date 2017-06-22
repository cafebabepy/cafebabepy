package org.cafebabepy.runtime.object;

import org.cafebabepy.runtime.Python;

/**
 * Created by yotchang4s on 2017/06/08.
 */
public class PyNoneObject extends AbstractPyObjectObject {

    public PyNoneObject(Python runtime) {
        super(runtime, runtime.typeOrThrow("builtins.NoneType", false));
    }

    @Override
    public boolean isNone() {
        return true;
    }

    @Override
    public String asJavaString() {
        return "None";
    }
}
