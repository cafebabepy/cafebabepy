package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefinePyFunction;
import org.cafebabepy.annotation.DefinePyType;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.object.PyObjectObject;

import java.util.Optional;

import static org.cafebabepy.util.ProtocolNames.*;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefinePyType(name = "builtins.object")
public final class PyObjectType extends AbstractCafeBabePyType {

    public PyObjectType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __new__)
    public PyObject __new__(PyObject cls) {
        if (!cls.isType()) {
            throw this.runtime.newRaiseTypeError("object.__new__(X): X is not a type object (" + cls.getFullName() + ")");
        }
        return new PyObjectObject(this.runtime, cls);
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject... args) {
    }

    @DefinePyFunction(name = __getattribute__)
    public PyObject __getattribute__(PyObject self, PyObject name) {
        PyObject v = self.getScope().getOrThrow(name.asJavaString());

        Optional<PyObject> getOpt = v.getScope().get(__get__);
        if (getOpt.isPresent()) {
            PyObject get = getOpt.get();
            return get.call(v, v.getType());

        } else {
            return v;
        }
    }

    @DefinePyFunction(name = __setattr__)
    public void __setattr__(PyObject self, PyObject name, PyObject value) {
        PyObject strType = this.runtime.typeOrThrow("builtins.str");

        if (this.runtime.callFunction("builtins.isinstance", self, strType).isFalse()) {
            throw this.runtime.newRaiseTypeError(
                    "attribute name must be string, not '" + name.getFullName() + "'"
            );
        }

        self.getScope().put(name.asJavaString(), value);
    }

    @DefinePyFunction(name = __str__)
    public PyObject __str__(PyObject self) {
        return this.runtime.str(self.asJavaString());
    }

    @DefinePyFunction(name = __eq__)
    public PyObject __eq__(PyObject self, PyObject other) {
        return this.runtime.NotImplementedType();
    }

    @DefinePyFunction(name = __ne__)
    public PyObject __ne__(PyObject self, PyObject other) {
        return this.runtime.NotImplementedType();
    }
}
