package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefineCafeBabePyFunction;
import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.object.PyIntObject;
import org.cafebabepy.runtime.object.PyTupleIteratorObject;
import org.cafebabepy.runtime.object.PyTupleObject;

import static org.cafebabepy.util.ProtocolNames.*;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefineCafeBabePyType(name = "builtins.tuple")
public class PyTupleType extends AbstractCafeBabePyType {

    public PyTupleType(Python runtime) {
        super(runtime);
    }

    @DefineCafeBabePyFunction(name = __getitem__)
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

        return tuple.get(index);
    }

    @DefineCafeBabePyFunction(name = __len__)
    public PyObject __len__(PyObject self) {
        if (!(self instanceof PyTupleObject)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__getitem__' requires a 'tuple' object but received a '"
                            + self.getType().getFullName()
                            + "'");
        }

        return ((PyTupleObject) self).getLen();
    }

    @DefineCafeBabePyFunction(name = __iter__)
    public PyObject __iter__(PyObject self) {
        if (!(self instanceof PyTupleObject)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__iter__' requires a 'tuple' object but received a '"
                            + self.getType().getFullName()
                            + "'");
        }

        return new PyTupleIteratorObject(this.runtime, (PyTupleObject) self);
    }
}
