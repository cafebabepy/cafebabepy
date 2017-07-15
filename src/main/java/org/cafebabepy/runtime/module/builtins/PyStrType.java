package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefineCafeBabePyFunction;
import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.object.PyStrObject;

import static org.cafebabepy.util.ProtocolNames.__add__;
import static org.cafebabepy.util.ProtocolNames.__str__;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefineCafeBabePyType(name = "builtins.str")
public final class PyStrType extends AbstractCafeBabePyType {

    public PyStrType(Python runtime) {
        super(runtime);
    }

    @DefineCafeBabePyFunction(name = __str__)
    public PyObject __str__(PyObject self) {
        PyObject strType = this.runtime.typeOrThrow("builtins.str");

        if (!this.runtime.isInstance(self, strType)) {
            throw this.runtime.newRaiseTypeError("descriptor '__str__' requires a 'str' object but received a '" + self.getFullName() + "'");
        }

        return self;
    }

    @DefineCafeBabePyFunction(name = __add__)
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
