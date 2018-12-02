package org.cafebabepy.runtime.object.proxy;

import org.cafebabepy.runtime.Frame;
import org.cafebabepy.runtime.PyObject;

public class PyLexicalScopeProxyObject extends PyProxyObject {

    private Frame frame;

    public PyLexicalScopeProxyObject(PyObject source) {
        super(source);

        this.frame = new Frame(source.getModule().getFrame(), source.getFrame());
    }

    @Override
    public Frame getFrame() {
        return this.frame;
    }
}
