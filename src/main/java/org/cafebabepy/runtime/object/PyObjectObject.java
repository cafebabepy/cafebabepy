package org.cafebabepy.runtime.object;

import org.cafebabepy.evaluter.Interpret.PyObjectMethodScope;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.PyObjectScope;
import org.cafebabepy.runtime.Python;

/**
 * Created by yotchang4s on 2017/06/04.
 */
public class PyObjectObject extends AbstractPyObjectObject {

    private volatile PyObjectMethodScope methodScope;

    public PyObjectObject(Python runtime, PyObject type) {
        super(runtime, type);
    }

    @Override
    public PyObjectScope getScope() {
        if (this.methodScope == null) {
            synchronized (this) {
                if (this.methodScope == null) {
                    this.methodScope = new PyObjectMethodScope(this);
                }
            }
        }

        return this.methodScope;
    }
}
