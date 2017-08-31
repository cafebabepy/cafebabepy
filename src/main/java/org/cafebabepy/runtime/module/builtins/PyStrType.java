package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefinePyFunction;
import org.cafebabepy.annotation.DefinePyType;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.object.java.PyStrObject;

import static org.cafebabepy.util.ProtocolNames.__add__;
import static org.cafebabepy.util.ProtocolNames.__str__;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefinePyType(name = "builtins.str")
public final class PyStrType extends AbstractCafeBabePyType {

    public PyStrType(Python runtime) {
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
}
