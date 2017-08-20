package org.cafebabepy.runtime.object;

import org.cafebabepy.runtime.Python;

/**
 * Created by yotchang4s on 2017/06/08.
 */
public class PyEllipsisObject extends AbstractPyObjectObject {

    public PyEllipsisObject(Python runtime) {
        super(runtime, runtime.typeOrThrow("builtins.Ellipsis"));
    }

    @Override
    public boolean isEllipsis() {
        return true;
    }

    @Override
    public String asJavaString() {
        return "Ellipsis";
    }
}
