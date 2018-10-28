package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.runtime.object.iterator.PyDictKeyIteratorObject;
import org.cafebabepy.runtime.object.java.PyDictKeysObject;

import static org.cafebabepy.util.ProtocolNames.__iter__;

/**
 * Created by yotchang4s on 2018/10/28.
 */
@DefinePyType(name = "builtins.dict_keys")
public final class PyDictKeysType extends AbstractCafeBabePyType {

    public PyDictKeysType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __iter__)
    public PyObject __iter__(PyObject self) {
        if (!(self instanceof PyDictKeysObject)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__iter__' requires a 'dict_keys' object but received a '"
                            + self.getType().getFullName()
                            + "'");
        }

        PyDictKeysObject object = (PyDictKeysObject) self;

        return new PyDictKeyIteratorObject(this.runtime, object);
    }
}
