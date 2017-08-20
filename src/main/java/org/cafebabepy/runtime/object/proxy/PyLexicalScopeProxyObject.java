package org.cafebabepy.runtime.object.proxy;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.PyObjectScope;

public class PyLexicalScopeProxyObject extends PyProxyObject {

    private PyObjectScope scope;

    public PyLexicalScopeProxyObject(PyObject source) {
        super(source);

        this.scope = new PyObjectScope(source);
    }

    @Override
    public PyObjectScope getScope() {
        return this.scope;
    }
}
