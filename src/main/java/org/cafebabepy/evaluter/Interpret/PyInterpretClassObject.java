package org.cafebabepy.evaluter.Interpret;

import org.cafebabepy.runtime.AbstractPyObject;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.PyObjectScope;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.proxy.PyLexicalScopeProxyObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.cafebabepy.util.ProtocolNames.__init__;
import static org.cafebabepy.util.ProtocolNames.__new__;

class PyInterpretClassObject extends AbstractPyObject {

    private final PyObject context;

    private final String name;

    private final List<PyObject> bases;

    PyInterpretClassObject(Python runtime, PyObject context, String name, List<PyObject> bases) {
        super(runtime, true);

        this.context = new PyLexicalScopeProxyObject(context);
        this.name = name;

        List<PyObject> mutableBases = new ArrayList<>(bases);
        if (mutableBases.isEmpty()) {
            mutableBases.add(runtime.typeOrThrow("builtins.object"));
        }
        this.bases = Collections.unmodifiableList(mutableBases);
    }

    @Override
    public PyObjectScope getScope() {
        return this.context.getScope();
    }

    @Override
    public PyObject getType() {
        return this;
    }

    @Override
    public List<PyObject> getBases() {
        return this.bases;
    }

    @Override
    public PyObject getModule() {
        return this.context.getModule();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isType() {
        return true;
    }

    @Override
    public boolean isModule() {
        return false;
    }

    @Override
    public boolean isNone() {
        return false;
    }

    @Override
    public boolean isNotImplemented() {
        return false;
    }

    @Override
    public boolean isEllipsis() {
        return false;
    }

    @Override
    public String asJavaString() {
        return "<class '" + getFullName() + "'>";
    }

    @Override
    public PyObject call(PyObject... args) {
        PyObject object = getScope().getOrThrow(__new__).call(this);
        object.getScope().getOrThrow(__init__).call(args);

        return object;
    }
}
