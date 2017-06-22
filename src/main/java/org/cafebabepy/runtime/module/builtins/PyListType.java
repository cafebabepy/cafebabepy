package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefineCafeBabePyFunction;
import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.object.PyListIteratorObject;
import org.cafebabepy.runtime.object.PyListObject;

import static org.cafebabepy.util.ProtocolNames.__iter__;

/**
 * Created by yotchang4s on 2017/06/03.
 */
@DefineCafeBabePyType(name = "builtins.list")
public class PyListType extends AbstractCafeBabePyType {

    public PyListType(Python runtime) {
        super(runtime);
    }

    @DefineCafeBabePyFunction(name = __iter__)
    public PyObject __iter__(PyObject self) {
        if (!(self instanceof PyListObject)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__iter__' requires a 'list' object but received a '"
                            + self.getType().getFullName()
                            + "'");
        }

        return new PyListIteratorObject(this.runtime, (PyListObject) self);
    }
}
