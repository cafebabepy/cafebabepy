package org.cafebabepy.runtime.object.literal;

import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.AbstractPyObjectObject;

/**
 * Created by yotchang4s on 2017/06/08.
 */
public class PyNoneObject extends AbstractPyObjectObject {

    public PyNoneObject(Python runtime) {
        super(runtime, runtime.typeOrThrow("builtins.None", false));
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
