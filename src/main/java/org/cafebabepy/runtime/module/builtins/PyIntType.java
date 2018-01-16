package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.object.java.PyIntObject;

import static org.cafebabepy.util.ProtocolNames.*;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefinePyType(name = "builtins.int")
public final class PyIntType extends AbstractCafeBabePyType {

    public PyIntType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __int__)
    public PyObject __int__(PyObject self) {
        PyObject intType = this.runtime.typeOrThrow("builtins.int");

        if (this.runtime.isInstance(self, intType)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__int__' requires a 'int' object but received a '" + self.getFullName() + "'");
        }

        return self;
    }

    @DefinePyFunction(name = __eq__)
    public PyObject __eq__(PyObject self, PyObject other) {
        PyObject result = check(self, other);
        if (result != null) {
            return result;
        }

        return ((PyIntObject) self).eq((PyIntObject) other);
    }

    @DefinePyFunction(name = __add__)
    public PyObject __add__(PyObject self, PyObject other) {
        PyObject result = check(self, other);
        if (result != null) {
            return result;
        }

        return ((PyIntObject) self).add((PyIntObject) other);
    }

    @DefinePyFunction(name = __radd__)
    public PyObject __radd__(PyObject self, PyObject other) {
        PyObject result = check(self, other);
        if (result != null) {
            return result;
        }

        return ((PyIntObject) other).add((PyIntObject) self);
    }

    @DefinePyFunction(name = __sub__)
    public PyObject __sub__(PyObject self, PyObject other) {
        PyObject result = check(self, other);
        if (result != null) {
            return result;
        }

        return ((PyIntObject) self).sub((PyIntObject) other);
    }

    @DefinePyFunction(name = __rsub__)
    public PyObject __rsub__(PyObject self, PyObject other) {
        PyObject result = check(self, other);
        if (result != null) {
            return result;
        }

        return ((PyIntObject) other).sub((PyIntObject) self);
    }

    @DefinePyFunction(name = __mod__)
    public PyObject __mod__(PyObject self, PyObject other) {
        PyObject result = check(self, other);
        if (result != null) {
            return result;
        }

        return ((PyIntObject) self).mod((PyIntObject) other);
    }

    @DefinePyFunction(name = __rmod__)
    public PyObject __rmod__(PyObject self, PyObject other) {
        PyObject result = check(self, other);
        if (result != null) {
            return result;
        }

        return ((PyIntObject) other).mod((PyIntObject) self);
    }

    @DefinePyFunction(name = __mul__)
    public PyObject __mul__(PyObject self, PyObject other) {
        PyObject result = check(self, other);
        if (result != null) {
            return result;
        }

        return ((PyIntObject) self).mul((PyIntObject) other);
    }

    @DefinePyFunction(name = __rmul__)
    public PyObject __rmul__(PyObject self, PyObject other) {
        PyObject result = check(self, other);
        if (result != null) {
            return result;
        }

        return ((PyIntObject) other).mul((PyIntObject) self);
    }

    @DefinePyFunction(name = __lt__)
    public PyObject __lt__(PyObject self, PyObject other) {
        PyObject result = check(self, other);
        if (result != null) {
            return result;
        }

        return ((PyIntObject) self).lt(((PyIntObject) other));
    }

    @DefinePyFunction(name = __rlt__)
    public PyObject __rlt__(PyObject self, PyObject other) {
        PyObject result = check(self, other);
        if (result != null) {
            return result;
        }

        return ((PyIntObject) other).lt(((PyIntObject) self));
    }

    @DefinePyFunction(name = __le__)
    public PyObject __le__(PyObject self, PyObject other) {
        PyObject result = check(self, other);
        if (result != null) {
            return result;
        }

        return ((PyIntObject) self).le(((PyIntObject) other));
    }

    @DefinePyFunction(name = __rle__)
    public PyObject __rle__(PyObject self, PyObject other) {
        PyObject result = check(self, other);
        if (result != null) {
            return result;
        }

        return ((PyIntObject) other).le(((PyIntObject) self));
    }

    @DefinePyFunction(name = __gt__)
    public PyObject __gt__(PyObject self, PyObject other) {
        PyObject result = check(self, other);
        if (result != null) {
            return result;
        }

        return ((PyIntObject) self).gt(((PyIntObject) other));
    }

    @DefinePyFunction(name = __rgt__)
    public PyObject __rgt__(PyObject self, PyObject other) {
        PyObject result = check(self, other);
        if (result != null) {
            return result;
        }

        return ((PyIntObject) other).gt(((PyIntObject) self));
    }

    @DefinePyFunction(name = __ge__)
    public PyObject __ge__(PyObject self, PyObject other) {
        PyObject result = check(self, other);
        if (result != null) {
            return result;
        }

        return ((PyIntObject) self).ge(((PyIntObject) other));
    }

    @DefinePyFunction(name = __rge__)
    public PyObject __rge__(PyObject self, PyObject other) {
        PyObject result = check(self, other);
        if (result != null) {
            return result;
        }

        return ((PyIntObject) other).ge(((PyIntObject) self));
    }

    @DefinePyFunction(name = __neg__)
    public PyObject __neg__(PyObject self) {
        PyObject result = check(self);
        if (result != null) {
            return result;
        }

        return ((PyIntObject) self).neg();
    }

    @DefinePyFunction(name = __pos__)
    public PyObject __pos__(PyObject self) {
        PyObject result = check(self);
        if (result != null) {
            return result;
        }

        return ((PyIntObject) self).pos();
    }

    @DefinePyFunction(name = __invert__)
    public PyObject __invert__(PyObject self) {
        PyObject result = check(self);
        if (result != null) {
            return result;
        }

        return ((PyIntObject) self).invert();
    }

    @DefinePyFunction(name = __bool__)
    public PyObject __bool__(PyObject self) {
        PyObject result = check(self);
        if (result != null) {
            return result;
        }

        return ((PyIntObject) self).bool();
    }

    @DefinePyFunction(name = __str__)
    public PyObject __str__(PyObject self) {
        check(self);
        return this.runtime.str(self.toJava(String.class));
    }

    private PyObject check(PyObject o) {
        PyObject intType = this.runtime.typeOrThrow("builtins.int");

        if (!this.runtime.isInstance(o, intType)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__int__' requires a 'int' object but received a '" + o.getFullName() + "'");

        } else if (!(o instanceof PyIntObject)) {
            throw new CafeBabePyException("int '" + o.getFullName() + "' object is not PyIntObject");
        }

        return null;
    }

    private PyObject check(PyObject o1, PyObject o2) {
        PyObject intType = this.runtime.typeOrThrow("builtins.int");

        if (!this.runtime.isInstance(o1, intType)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__int__' requires a 'int' object but received a '" + o1.getFullName() + "'");

        } else if (!this.runtime.isInstance(o2, intType)) {
            return this.runtime.NotImplemented();

        } else if (!(o1 instanceof PyIntObject) || !(o2 instanceof PyIntObject)) {
            throw new CafeBabePyException("int '" + o2.getFullName() + "' object is not PyIntObject");
        }

        return null;
    }
}
