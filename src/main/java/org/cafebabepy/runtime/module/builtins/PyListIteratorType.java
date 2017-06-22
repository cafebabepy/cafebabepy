package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefineCafeBabePyFunction;
import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.object.PyListIteratorObject;
import org.cafebabepy.runtime.object.PyListObject;

import static org.cafebabepy.util.ProtocolNames.__iter__;
import static org.cafebabepy.util.ProtocolNames.__next__;

/**
 * Created by yotchang4s on 2017/06/14.
 */
@DefineCafeBabePyType(name = "builtins.list_iterator", appear = false)
public class PyListIteratorType extends AbstractCafeBabePyType {

    public PyListIteratorType(Python runtime) {
        super(runtime);
    }

    @DefineCafeBabePyFunction(name = __next__)
    public PyObject __next__(PyObject self) {
        if (!(self instanceof PyListIteratorObject)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__next__' requires a 'list_iterator' object but received a '"
                            + self.getType().getFullName()
                            + "'");
        }

        return ((PyListIteratorObject) self).next();
    }

    @DefineCafeBabePyFunction(name = __iter__)
    public PyObject __iter__(PyObject self) {
        if (!(self instanceof PyListObject)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__iter__' requires a 'list' object but received a '"
                            + self.getType().getFullName()
                            + "'");
        }

        return new PyListIteratorObject(self.getRuntime(), (PyListObject) self);
    }
}
