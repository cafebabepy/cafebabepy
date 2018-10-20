package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.runtime.object.iterator.PyStrIteratorObject;
import org.cafebabepy.runtime.object.java.PyBytesObject;
import org.cafebabepy.runtime.object.java.PyStrObject;

import java.util.Arrays;

import static org.cafebabepy.util.ProtocolNames.*;

/**
 * Created by yotchang4s on 2018/10/08.
 */
@DefinePyType(name = "builtins.bytes")
public final class PyBytesType extends AbstractCafeBabePyType {

    public PyBytesType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __str__)
    public PyObject __str__(PyObject self) {
        PyObject strType = this.runtime.typeOrThrow("builtins.str");

        if (!this.runtime.isInstance(self, strType)) {
            throw this.runtime.newRaiseTypeError("descriptor '__str__' requires a 'str' object but received a '" + self.getFullName() + "'");
        }

        return self;
    }

    @DefinePyFunction(name = __add__)
    public PyObject __add__(PyObject self, PyObject other) {
        PyObject strType = this.runtime.typeOrThrow("builtins.str");

        if (!(self instanceof PyStrObject)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__add__' requires a 'str' object but received a '" + self.getFullName() + "'");

        } else if (!(other instanceof PyStrObject)) {
            throw this.runtime.newRaiseTypeError("must be str, not " + other);
        }

        return ((PyStrObject) self).add((PyStrObject) other);
    }

    @DefinePyFunction(name = __iter__)
    public PyObject __iter__(PyObject self) {
        if (!(self instanceof PyStrObject)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__iter__' requires a 'str' object but received a '"
                            + self.getType().getFullName()
                            + "'");
        }

        return new PyStrIteratorObject(this.runtime, (PyStrObject) self);
    }

    @DefinePyFunction(name = __eq__)
    public PyObject __eq__(PyObject self, PyObject other) {
        if (this.runtime.isInstance(self, "bytes")) {
            if (!(self instanceof PyBytesObject)) {
                throw new CafeBabePyException("self is not PyBytesObject " + self);

            } else if (this.runtime.isInstance(other, "bytes")) {
                if (!(other instanceof PyBytesObject)) {
                    throw new CafeBabePyException("other is not PyBytesObject " + other);
                }
                int[] jself = ((PyBytesObject) self).getValue();
                int[] jother = ((PyBytesObject) other).getValue();

                return Arrays.equals(jself, jother) ? this.runtime.True() : this.runtime.False();

            } else {
                return this.runtime.NotImplemented();
            }

        } else {
            throw this.runtime.newRaiseTypeError("TypeError: descriptor '__eq__' requires a 'str' object but received a '" + self.getName() + "'");
        }
    }

    @DefinePyFunction(name = __hash__)
    public PyObject __hash__(PyObject self) {
        if (this.runtime.isInstance(self, "bytes")) {
            if (!(self instanceof PyBytesObject)) {
                throw new CafeBabePyException("self is not PyStrObject " + self);
            }

            int hashCode = Arrays.hashCode(((PyBytesObject) self).getValue());

            return this.runtime.number(hashCode);

        } else {
            throw this.runtime.newRaiseTypeError("TypeError: descriptor '__hash__' requires a 'str' object but received a '" + self.getName() + "'");
        }
    }
}
