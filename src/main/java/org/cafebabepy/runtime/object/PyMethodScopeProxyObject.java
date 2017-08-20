package org.cafebabepy.runtime.object;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.PyObjectScope;

public class PyMethodScopeProxyObject extends PyProxyObject {

    private PyObjectScope scope;

    public PyMethodScopeProxyObject(PyObject source) {
        super(source);

        this.scope = new PyObjectScope(source);
    }

    @Override
    public PyObjectScope getScope() {
        return this.scope;
    }
}
