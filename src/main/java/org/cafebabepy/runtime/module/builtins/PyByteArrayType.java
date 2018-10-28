package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.runtime.object.iterator.PyByteArrayIteratorObject;
import org.cafebabepy.runtime.object.iterator.PyBytesIteratorObject;
import org.cafebabepy.runtime.object.java.PyBytesObject;

import java.util.Arrays;

import static org.cafebabepy.util.ProtocolNames.*;

/**
 * Created by yotchang4s on 2018/10/28.
 */
@DefinePyType(name = "builtins.bytearray")
public final class PyByteArrayType extends AbstractCafeBabePyType {

    public PyByteArrayType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __iter__)
    public PyObject __iter__(PyObject self) {
        if (!(self instanceof PyBytesObject)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__iter__' requires a 'bytes' object but received a '"
                            + self.getType().getFullName()
                            + "'");
        }

        return new PyByteArrayIteratorObject(this.runtime);
    }
}
