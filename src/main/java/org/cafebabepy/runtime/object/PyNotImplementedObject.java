package org.cafebabepy.runtime.object;

import org.cafebabepy.runtime.Python;

/**
 * Created by yotchang4s on 2017/06/08.
 */
public class PyNotImplementedObject extends AbstractPyObjectObject {

    public PyNotImplementedObject(Python runtime) {
        super(runtime, runtime.typeOrThrow("builtins.NotImplemented", false));
    }

    @Override
    public boolean isNotImplemented() {
        return true;
    }

    @Override
    public String asJavaString() {
        return "NotImplemented";
    }
}
