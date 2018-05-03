package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.runtime.object.iterator.PyStrIteratorObject;

import static org.cafebabepy.util.ProtocolNames.__iter__;
import static org.cafebabepy.util.ProtocolNames.__next__;

/**
 * Created by yotchang4s on 2018/05/03.
 */
@DefinePyType(name = "builtins.str_iterator", appear = false)
public class PyStrIteratorType extends AbstractCafeBabePyType {

    public PyStrIteratorType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __next__)
    public PyObject __next__(PyObject self) {
        if (!(self instanceof PyStrIteratorObject)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__next__' requires a 'str_iterator' object but received a '"
                            + self.getType().getFullName()
                            + "'");
        }

        return ((PyStrIteratorObject) self).next();
    }

    @DefinePyFunction(name = __iter__)
    public PyObject __iter__(PyObject self) {
        if (!(self instanceof PyStrIteratorObject)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__iter__' requires a 'str_iterator' object but received a '"
                            + self.getType().getFullName()
                            + "'");
        }

        return this;
    }
}
