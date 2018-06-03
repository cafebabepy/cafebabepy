package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.runtime.object.iterator.PyTupleIteratorObject;
import org.cafebabepy.runtime.object.java.PyIntObject;
import org.cafebabepy.runtime.object.java.PyTupleObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.cafebabepy.util.ProtocolNames.*;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefinePyType(name = "builtins.tuple")
public class PyTupleType extends AbstractCafeBabePyType {

    public PyTupleType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __init__)
    public PyObject __init__(PyObject self, PyObject iterable) {
        if (!(self instanceof PyTupleObject)) {
            return this.runtime.getattr(self, __init__).call(iterable);
        }

        PyTupleObject tuple = (PyTupleObject) self;

        // TODO 直接tuple.__init__を呼び出すとミュータブルになるが大丈夫か？
        List<PyObject> list = new ArrayList<>();
        this.runtime.iter(iterable, list::add);

        tuple.getRawList().clear();
        tuple.getRawList().addAll(list);

        return this.runtime.None();
    }

    @DefinePyFunction(name = __getitem__)
    public PyObject __getitem__(PyObject self, PyObject key) {
        if (!(self instanceof PyTupleObject)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__getitem__' requires a 'tuple' object but received a '"
                            + self.getType().getFullName()
                            + "'");
        }
        if (!(key instanceof PyIntObject)) {
            throw this.runtime.newRaiseTypeError(
                    "tuple indices must be integers or slices, not " + key.getType().getFullName());
        }

        PyTupleObject tuple = (PyTupleObject) self;
        PyIntObject index = (PyIntObject) key;

        return tuple.getRawList().get(index.getIntValue());
    }

    @DefinePyFunction(name = __len__)
    public PyObject __len__(PyObject self) {
        if (!(self instanceof PyTupleObject)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__getitem__' requires a 'tuple' object but received a '"
                            + self.getType().getFullName()
                            + "'");
        }

        return ((PyTupleObject) self).getLen();
    }

    @DefinePyFunction(name = __iter__)
    public PyObject __iter__(PyObject self) {
        if (!(self instanceof PyTupleObject)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__iter__' requires a 'tuple' object but received a '"
                            + self.getType().getFullName()
                            + "'");
        }

        return new PyTupleIteratorObject(this.runtime, (PyTupleObject) self);
    }

    @DefinePyFunction(name = __str__)
    public PyObject __str__(PyObject self) {
        List<PyObject> jlist = new ArrayList<>();
        this.runtime.iter(self, jlist::add);

        if (jlist.size() == 1) {
            return this.runtime.str("(" + jlist.get(0) + ",)");

        } else {
            String jstr = jlist.stream().map(Object::toString).collect(Collectors.joining(", ", "(", ")"));

            return this.runtime.str(jstr);
        }
    }

    @DefinePyFunction(name = __eq__)
    public PyObject __eq__(PyObject self, PyObject other) {
        if (!(self instanceof PyTupleObject)) {
            throw this.runtime.newRaiseTypeError("descriptor '__eq__' requires a 'list' object but received a '" + self.getType() + "'");
        }

        if (!(other instanceof PyTupleObject)) {
            return this.runtime.NotImplemented();
        }

        PyTupleObject v1 = (PyTupleObject) self;
        PyTupleObject v2 = (PyTupleObject) other;

        return this.runtime.bool(v1.getRawList().equals(v2.getRawList()));
    }

    @DefinePyFunction(name = __hash__)
    public PyObject __hash__(PyObject self) {
        if (!(self instanceof PyTupleObject)) {
            throw this.runtime.newRaiseTypeError("descriptor '__hash__' requires a 'list' object but received a '" + self.getType() + "'");
        }

        PyTupleObject v = (PyTupleObject) self;

        return this.runtime.number(v.getRawList().hashCode());
    }
}
