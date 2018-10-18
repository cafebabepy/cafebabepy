package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.runtime.object.iterator.PyBytesIteratorObject;

import static org.cafebabepy.util.ProtocolNames.__iter__;
import static org.cafebabepy.util.ProtocolNames.__next__;

/**
 * Created by yotchang4s on 2018/10/17.
 */
@DefinePyType(name = "builtins.bytes_iterator", appear = false)
public class PyBytesIteratorType extends AbstractCafeBabePyType {

    public PyBytesIteratorType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __next__)
    public PyObject __next__(PyObject self) {
        if (!(self instanceof PyBytesIteratorObject)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__next__' requires a 'bytes_iterator' object but received a '"
                            + self.getType().getFullName()
                            + "'");
        }

        return ((PyBytesIteratorObject) self).next();
    }

    @DefinePyFunction(name = __iter__)
    public PyObject __iter__(PyObject self) {
        if (!(self instanceof PyBytesIteratorObject)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__iter__' requires a 'bytes_iterator' object but received a '"
                            + self.getType().getFullName()
                            + "'");
        }

        return this;
    }
}
