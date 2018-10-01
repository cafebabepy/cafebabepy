package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyFunctionDefaultValue;
import org.cafebabepy.runtime.module.DefinePyType;

import static org.cafebabepy.util.ProtocolNames.*;

/**
 * Created by yotchang4s on 2018/09/03.
 */
@DefinePyType(name = "builtins.property")
public class PyPropertyType extends AbstractCafeBabePyType {
    public PyPropertyType(Python runtime) {
        super(runtime, false);
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject fget, PyObject fset, PyObject fdel, PyObject doc) {
        this.runtime.setattr(self, "fget", fget);
        this.runtime.setattr(self, "fset", fset);
        this.runtime.setattr(self, "fdel", fdel);

        if (doc.isNone() && !fget.isNone()) {
            // FIXME __doc__
            //doc = this.runtime.getattr(fget, __doc__);
        }

        this.runtime.setattr(self, "doc", doc);
    }

    @DefinePyFunctionDefaultValue(methodName = __init__, parameterName = "fget")
    public PyObject __init__fget() {
        return this.runtime.None();
    }

    @DefinePyFunctionDefaultValue(methodName = __init__, parameterName = "fset")
    public PyObject __init__fset() {
        return this.runtime.None();
    }

    @DefinePyFunctionDefaultValue(methodName = __init__, parameterName = "fdel")
    public PyObject __init__fdel() {
        return this.runtime.None();
    }

    @DefinePyFunctionDefaultValue(methodName = __init__, parameterName = "doc")
    public PyObject __init__doc() {
        return this.runtime.None();
    }

    @DefinePyFunction(name = __get__)
    public PyObject __get__(PyObject self, PyObject obj, PyObject objtype) {
        if (obj.isNone()) {
            return self;
        }

        PyObject fget = this.runtime.getattr(self, "fget");

        if (fget.isNone()) {
            throw this.runtime.newRaiseException("AttributeError", "unreadable attribute");
        }

        return fget.call(obj);
    }

    @DefinePyFunctionDefaultValue(methodName = __get__, parameterName = "objtype")
    public PyObject __get___objtype() {
        return this.runtime.None();
    }

    @DefinePyFunction(name = __set__)
    public void __set__(PyObject self, PyObject obj, PyObject value) {
        PyObject fset = this.runtime.getattr(self, "fset");

        if (fset.isNone()) {
            throw this.runtime.newRaiseException("AttributeError", "can't set attribute");
        }

        fset.call(obj, value);
    }

    @DefinePyFunction(name = __delete__)
    public void __delete__(PyObject self, PyObject obj) {
        PyObject fdel = this.runtime.getattr(self, "fdel");

        if (fdel.isNone()) {
            throw this.runtime.newRaiseException("AttributeError", "can't delete attribute");
        }

        fdel.call(obj);
    }

    @DefinePyFunction(name = "getter")
    public PyObject getter(PyObject self, PyObject fget) {
        PyObject fset = this.runtime.getattr(self, "fset");
        PyObject fdel = this.runtime.getattr(self, "fdel");
        PyObject doc = this.runtime.getattr(self, __doc__);

        return self.getType().call(fget, fset, fdel, doc);
    }

    @DefinePyFunction(name = "setter")
    public PyObject setter(PyObject self, PyObject fset) {
        PyObject fget = this.runtime.getattr(self, "fget");
        PyObject fdel = this.runtime.getattr(self, "fdel");

        //PyObject doc = this.runtime.getattr(self, __doc__);
        PyObject doc = this.runtime.None();

        return self.getType().call(fget, fset, fdel, doc);
    }

    @DefinePyFunction(name = "deleter")
    public PyObject deleter(PyObject self, PyObject fdel) {
        PyObject fget = this.runtime.getattr(self, "fget");
        PyObject fset = this.runtime.getattr(self, "fset");

        //PyObject doc = this.runtime.getattr(self, __doc__);
        PyObject doc = this.runtime.None();

        return self.getType().call(fget, fset, fdel, doc);
    }
}
