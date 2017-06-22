package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefineCafeBabePyFunction;
import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.object.PyIntObject;

import static org.cafebabepy.util.ProtocolNames.*;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefineCafeBabePyType(name = "builtins.int")
public final class PyIntType extends AbstractCafeBabePyType {

    public PyIntType(Python runtime) {
        super(runtime);
    }

    @DefineCafeBabePyFunction(name = __int__)
    public PyObject __int__(PyObject self) {
        PyObject intType = this.runtime.typeOrThrow("builtins.int");

        PyObject builtins = this.runtime.getBuiltinsModule();
        PyObject isinstance = builtins.getObjectOrThrow("isinstance");
        if (isinstance.call(builtins, self, intType).isFalse()) {
            throw this.runtime.newRaiseTypeError("descriptor '__int__' requires a 'int' object but received a '" + self.getFullName() + "'");
        }

        return self;
    }

    @DefineCafeBabePyFunction(name = __add__)
    public PyObject __add__(PyObject self, PyObject other) {
        PyObject result = check(self, other);
        if (result != null) {
            return result;
        }

        return ((PyIntObject) self).add((PyIntObject) other);
    }

    @DefineCafeBabePyFunction(name = __radd__)
    public PyObject __radd__(PyObject self, PyObject other) {
        PyObject result = check(other, self);
        if (result != null) {
            return result;
        }

        return ((PyIntObject) other).add((PyIntObject) self);
    }

    @DefineCafeBabePyFunction(name = __sub__)
    public PyObject __sub__(PyObject self, PyObject other) {
        PyObject result = check(self, other);
        if (result != null) {
            return result;
        }

        return ((PyIntObject) self).sub((PyIntObject) other);
    }

    @DefineCafeBabePyFunction(name = __rsub__)
    public PyObject __rsub__(PyObject self, PyObject other) {
        PyObject result = check(other, self);
        if (result != null) {
            return result;
        }

        return ((PyIntObject) other).sub((PyIntObject) self);
    }

    private PyObject check(PyObject o1, PyObject o2) {
        PyObject intType = this.runtime.typeOrThrow("builtins.int");

        PyObject builtins = this.runtime.getBuiltinsModule();
        PyObject isinstance = builtins.getObjectOrThrow("isinstance");
        if (isinstance.call(builtins, o1, intType).isFalse()) {
            throw this.runtime.newRaiseTypeError("descriptor '__int__' requires a 'int' object but received a '" + o1.getFullName() + "'");

        } else if (isinstance.call(builtins, o1, intType).isFalse()) {
            return this.runtime.NotImplementedType();

        } else if (!(o1 instanceof PyIntObject) || !(o2 instanceof PyIntObject)) {
            throw new CafeBabePyException("int " + o2.getFullName() + " object is not PyIntObject");
        }

        return null;
    }

    public static PyObject newInt(Python runtime, int value) {
        PyObject result = new PyIntObject(runtime, value);
        result.preInitialize();
        result.postInitialize();

        return result;
    }
}
