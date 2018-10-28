package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.runtime.object.iterator.PyDictItemIteratorObject;
import org.cafebabepy.runtime.object.java.PyDictItemsObject;

import static org.cafebabepy.util.ProtocolNames.__iter__;

/**
 * Created by yotchang4s on 2018/10/28.
 */
@DefinePyType(name = "builtins.dict_items")
public final class PyDictItemsType extends AbstractCafeBabePyType {

    public PyDictItemsType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __iter__)
    public PyObject __iter__(PyObject self) {
        if (!(self instanceof PyDictItemsObject)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__iter__' requires a 'dict_values' object but received a '"
                            + self.getType().getFullName()
                            + "'");
        }

        PyDictItemsObject object = (PyDictItemsObject) self;

        return new PyDictItemIteratorObject(this.runtime, object);
    }
}
