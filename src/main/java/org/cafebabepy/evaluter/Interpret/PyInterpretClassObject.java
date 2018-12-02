package org.cafebabepy.evaluter.Interpret;

import org.cafebabepy.runtime.AbstractPyObject;
import org.cafebabepy.runtime.Frame;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import static org.cafebabepy.util.ProtocolNames.__call__;

public class PyInterpretClassObject extends AbstractPyObject {

    private final String name;
    private final List<PyObject> bases;
    private PyObject context;
    private volatile Frame frame;

    public PyInterpretClassObject(Python runtime, String name, List<PyObject> bases) {
        super(runtime);

        this.context = this.runtime.None();
        this.name = name;

        List<PyObject> mutableBases = new ArrayList<>(bases);
        if (mutableBases.isEmpty()) {
            mutableBases.add(runtime.typeOrThrow("builtins.object"));
        }
        this.bases = Collections.unmodifiableList(mutableBases);
    }

    public PyObject getContext() {
        return this.context;
    }

    public void setContext(PyObject context) {
        this.frame = null;
        this.context = context;
    }

    @Override
    public Frame getFrame() {
        if (this.frame == null) {
            synchronized (this) {
                if (this.frame == null) {
                    this.frame = new Frame(this.context.getFrame());
                }
            }
        }

        return this.frame;
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("builtins.type");
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
    public PyObject call(PyObject... args) {
        return call(args, new LinkedHashMap<>());
    }

    @Override
    public PyObject call(PyObject[] args, LinkedHashMap<String, PyObject> keywords) {
        if (this.runtime.isSubClass(this, "builtins.type")) {
            PyObject[] newArgs = new PyObject[args.length + 1];
            newArgs[0] = this;
            System.arraycopy(args, 0, newArgs, 1, args.length);

            return this.runtime.getattr(this, __call__).call(newArgs, keywords);

        } else {
            return this.runtime.getattr(this, __call__).call(args, keywords);
        }
    }
}
