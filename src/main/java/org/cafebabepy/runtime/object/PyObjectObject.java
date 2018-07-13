package org.cafebabepy.runtime.object;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;

/**
 * Created by yotchang4s on 2017/06/04.
 */
public class PyObjectObject extends AbstractPyObjectObject {

    private final PyObject type;

    public PyObjectObject(Python runtime) {
        super(runtime);

        this.type = null;
    }

    public PyObjectObject(Python runtime, PyObject type) {
        super(runtime);

        this.type = type;
    }

    @Override
    public PyObject getType() {
        if (this.type == null) {
            return this.runtime.typeOrThrow("builtins.object");
        }

        return this.type;
    }
}
