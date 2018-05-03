package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.runtime.object.iterator.PyListIteratorObject;
import org.cafebabepy.runtime.object.iterator.PyStrIteratorObject;
import org.cafebabepy.runtime.object.java.PyListObject;
import org.cafebabepy.runtime.object.java.PyStrObject;

import static org.cafebabepy.util.ProtocolNames.*;

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
        if (this.runtime.isInstance(self, "builtins.str")) {
            if (!(self instanceof PyStrObject)) {
                throw new CafeBabePyException("self is not PyStrObject " + self);

            } else if (this.runtime.isInstance(other, "builtins.str")) {
                if (!(other instanceof PyStrObject)) {
                    throw new CafeBabePyException("other is not PyStrObject " + other);
                }
                String jself = ((PyStrObject) self).getValue();
                String jother = ((PyStrObject) other).getValue();

                return jself.equals(jother) ? this.runtime.True() : this.runtime.False();

            } else {
                return this.runtime.NotImplemented();
            }

        } else {
            throw this.runtime.newRaiseTypeError("TypeError: descriptor '__eq__' requires a 'str' object but received a '" + self.getName() + "'");
        }
    }

    @DefinePyFunction(name = __hash__)
    public PyObject __hash__(PyObject self) {
        if (this.runtime.isInstance(self, "builtins.str")) {
            if (!(self instanceof PyStrObject)) {
                throw new CafeBabePyException("self is not PyStrObject " + self);
            }

            return this.runtime.number(((PyStrObject) self).getValue().hashCode());

        } else {
            throw this.runtime.newRaiseTypeError("TypeError: descriptor '__hash__' requires a 'str' object but received a '" + self.getName() + "'");
        }
    }

    @DefinePyFunction(name = "join")
    public PyObject join(PyObject self, PyObject iterable) {
        if (!this.runtime.isInstance(self, "builtins.str")) {
            throw this.runtime.newRaiseTypeError("TypeError: descriptor '__hash__' requires a 'str' object but received a '" + self.getName() + "'");
        }
        if (!(self instanceof PyStrObject)) {
            throw new CafeBabePyException("self is not PyStrObject " + self);
        }

        if (!this.runtime.isIterable(iterable)) {
            throw this.runtime.newRaiseTypeError("can only join an iterable");
        }

        String selfValue = ((PyStrObject) self).getValue();

        StringBuilder builder = new StringBuilder();
        this.runtime.iterIndex(iterable, (v, i) -> {
            if (i > 0) {
                builder.append(selfValue);
            }
            builder.append(v.toJava(String.class));
        });

        return this.runtime.str(builder.toString());
    }
}
