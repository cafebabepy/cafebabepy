package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.runtime.object.java.PyFloatObject;

import static org.cafebabepy.util.ProtocolNames.*;

/**
 * Created by yotchang4s on 2018/08/11.
 */
@DefinePyType(name = "builtins.float")
public final class PyFloatType extends AbstractCafeBabePyType {

    public PyFloatType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __int__)
    public PyObject __int__(PyObject self) {
        PyObject result = check(__eq__, self);
        if (result != null) {
            return result;
        }

        PyFloatObject floatObject = (PyFloatObject) self;
        float value = floatObject.getValue();

        return this.runtime.number((int) Math.ceil(value));
    }

    @DefinePyFunction(name = __eq__)
    public PyObject __eq__(PyObject self, PyObject other) {
        PyObject result = check(__eq__, self, other);
        if (result != null) {
            return result;
        }

        return ((PyFloatObject) self).eq((PyFloatObject) other);
    }

    @DefinePyFunction(name = __hash__)
    public PyObject __hash__(PyObject self) {
        PyObject result = check(__hash__, self);
        if (result != null) {
            return result;
        }

        float value = ((PyFloatObject) self).getValue();

        return this.runtime.number(Float.hashCode(value));
    }

    @DefinePyFunction(name = __ne__)
    public PyObject __ne__(PyObject self, PyObject other) {
        PyObject result = check(__ne__, self, other);
        if (result != null) {
            return result;
        }

        return ((PyFloatObject) self).ne((PyFloatObject) other);
    }

    @DefinePyFunction(name = __add__)
    public PyObject __add__(PyObject self, PyObject other) {
        PyObject result = check(__add__, self, other);
        if (result != null) {
            return result;
        }

        return ((PyFloatObject) self).add((PyFloatObject) other);
    }

    @DefinePyFunction(name = __radd__)
    public PyObject __radd__(PyObject self, PyObject other) {
        PyObject result = check(__radd__, self, other);
        if (result != null) {
            return result;
        }

        return ((PyFloatObject) other).add((PyFloatObject) self);
    }

    @DefinePyFunction(name = __sub__)
    public PyObject __sub__(PyObject self, PyObject other) {
        PyObject result = check(__sub__, self, other);
        if (result != null) {
            return result;
        }

        return ((PyFloatObject) self).sub((PyFloatObject) other);
    }

    @DefinePyFunction(name = __rsub__)
    public PyObject __rsub__(PyObject self, PyObject other) {
        PyObject result = check(__rsub__, self, other);
        if (result != null) {
            return result;
        }

        return ((PyFloatObject) other).sub((PyFloatObject) self);
    }

    @DefinePyFunction(name = __mod__)
    public PyObject __mod__(PyObject self, PyObject other) {
        PyObject result = check(__mod__, self, other);
        if (result != null) {
            return result;
        }

        return ((PyFloatObject) self).mod((PyFloatObject) other);
    }

    @DefinePyFunction(name = __rmod__)
    public PyObject __rmod__(PyObject self, PyObject other) {
        PyObject result = check(__rmod__, self, other);
        if (result != null) {
            return result;
        }

        return ((PyFloatObject) other).mod((PyFloatObject) self);
    }

    @DefinePyFunction(name = __mul__)
    public PyObject __mul__(PyObject self, PyObject other) {
        PyObject result = check(__mul__, self, other);
        if (result != null) {
            return result;
        }

        return ((PyFloatObject) self).mul((PyFloatObject) other);
    }

    @DefinePyFunction(name = __rmul__)
    public PyObject __rmul__(PyObject self, PyObject other) {
        PyObject result = check(__rmul__, self, other);
        if (result != null) {
            return result;
        }

        return ((PyFloatObject) other).mul((PyFloatObject) self);
    }

    @DefinePyFunction(name = __lt__)
    public PyObject __lt__(PyObject self, PyObject other) {
        PyObject result = check(__lt__, self, other);
        if (result != null) {
            return result;
        }

        return ((PyFloatObject) self).lt(((PyFloatObject) other));
    }

    @DefinePyFunction(name = __rlt__)
    public PyObject __rlt__(PyObject self, PyObject other) {
        PyObject result = check(__rlt__, self, other);
        if (result != null) {
            return result;
        }

        return ((PyFloatObject) other).lt(((PyFloatObject) self));
    }

    @DefinePyFunction(name = __le__)
    public PyObject __le__(PyObject self, PyObject other) {
        PyObject result = check(__le__, self, other);
        if (result != null) {
            return result;
        }

        return ((PyFloatObject) self).le(((PyFloatObject) other));
    }

    @DefinePyFunction(name = __rle__)
    public PyObject __rle__(PyObject self, PyObject other) {
        PyObject result = check(__rle__, self, other);
        if (result != null) {
            return result;
        }

        return ((PyFloatObject) other).le(((PyFloatObject) self));
    }

    @DefinePyFunction(name = __gt__)
    public PyObject __gt__(PyObject self, PyObject other) {
        PyObject result = check(__gt__, self, other);
        if (result != null) {
            return result;
        }

        return ((PyFloatObject) self).gt(((PyFloatObject) other));
    }

    @DefinePyFunction(name = __rgt__)
    public PyObject __rgt__(PyObject self, PyObject other) {
        PyObject result = check(__rgt__, self, other);
        if (result != null) {
            return result;
        }

        return ((PyFloatObject) other).gt(((PyFloatObject) self));
    }

    @DefinePyFunction(name = __ge__)
    public PyObject __ge__(PyObject self, PyObject other) {
        PyObject result = check(__ge__, self, other);
        if (result != null) {
            return result;
        }

        return ((PyFloatObject) self).ge(((PyFloatObject) other));
    }

    @DefinePyFunction(name = __rge__)
    public PyObject __rge__(PyObject self, PyObject other) {
        PyObject result = check(__rge__, self, other);
        if (result != null) {
            return result;
        }

        return ((PyFloatObject) other).ge(((PyFloatObject) self));
    }

    @DefinePyFunction(name = __neg__)
    public PyObject __neg__(PyObject self) {
        PyObject result = check(__neg__, self);
        if (result != null) {
            return result;
        }

        return ((PyFloatObject) self).neg();
    }

    @DefinePyFunction(name = __pos__)
    public PyObject __pos__(PyObject self) {
        PyObject result = check(__pos__, self);
        if (result != null) {
            return result;
        }

        return ((PyFloatObject) self).pos();
    }

    @DefinePyFunction(name = __bool__)
    public PyObject __bool__(PyObject self) {
        PyObject result = check(__bool__, self);
        if (result != null) {
            return result;
        }

        return ((PyFloatObject) self).bool();
    }

    @DefinePyFunction(name = __str__)
    public PyObject __str__(PyObject self) {
        PyObject result = check(__str__, self);
        if (result != null) {
            return result;
        }
        return this.runtime.str(self.toJava(String.class));
    }

    private PyObject check(String functionName, PyObject o) {
        PyObject floatType = this.runtime.typeOrThrow("builtins.float");

        if (!this.runtime.isInstance(o, floatType)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '" + functionName + "' requires a 'float' object but received a '" + o.getFullName() + "'");

        } else if (!(o instanceof PyFloatObject)) {
            throw new CafeBabePyException(o + " is not PyFloatObject");
        }

        return null;
    }

    private PyObject check(String functionName, PyObject o1, PyObject o2) {
        PyObject floatType = this.runtime.typeOrThrow("builtins.float");

        if (!this.runtime.isInstance(o1, floatType)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '" + functionName + "' requires a 'float' object but received a '" + o1.getFullName() + "'");

        } else if (!this.runtime.isInstance(o2, floatType)) {
            return this.runtime.NotImplemented();

        } else if (!(o1 instanceof PyFloatObject) || !(o2 instanceof PyFloatObject)) {
            throw new CafeBabePyException(o1 + " or " + o2 + " is not PyFloatObject");
        }

        return null;
    }
}
