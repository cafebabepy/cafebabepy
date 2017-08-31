package org.cafebabepy.runtime.object;

import org.cafebabepy.runtime.object.proxy.PyMethodObjectScope;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.PyObjectScope;
import org.cafebabepy.runtime.Python;

/**
 * Created by yotchang4s on 2017/06/04.
 */
public class PyObjectObject extends AbstractPyObjectObject {

    private volatile PyMethodObjectScope methodScope;

    public PyObjectObject(Python runtime, PyObject type) {
        super(runtime, type);
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
