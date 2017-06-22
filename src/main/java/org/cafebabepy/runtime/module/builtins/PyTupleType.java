package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefineCafeBabePyFunction;
import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.object.PyTupleIteratorObject;
import org.cafebabepy.runtime.object.PyTupleObject;

import java.util.Collection;

import static org.cafebabepy.util.ProtocolNames.__iter__;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefineCafeBabePyType(name = "builtins.tuple")
public class PyTupleType extends AbstractCafeBabePyType {

    public PyTupleType(Python runtime) {
        super(runtime);
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
