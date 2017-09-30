package org.cafebabepy.runtime.object;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.PyObjectScope;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.proxy.PyMethodObjectScope;

/**
 * Created by yotchang4s on 2017/06/04.
 */
public class PyObjectObject extends AbstractPyObjectObject {

    private final PyObject type;

    private volatile PyMethodObjectScope methodScope;

    public PyObjectObject(Python runtime, PyObject type) {
        super(runtime);

        this.type = type;
    }

    @Override
    public PyObject getType() {
        return this.type;
    }

    @Override
    public PyObjectScope getScope() {
        if (this.methodScope == null) {
            synchronized (this) {
                if (this.methodScope == null) {
                    this.methodScope = new PyMethodObjectScope(this);
                }
            }
        }

        return this.methodScope;
    }
}
