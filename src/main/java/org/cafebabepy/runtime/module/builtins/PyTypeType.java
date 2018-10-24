package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.runtime.object.PyObjectObject;
import org.cafebabepy.runtime.object.java.PyTupleObject;

import java.util.LinkedHashMap;
import java.util.Optional;

import static org.cafebabepy.util.ProtocolNames.*;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefinePyType(name = "builtins.type")
public final class PyTypeType extends AbstractCafeBabePyType {

    public PyTypeType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __call__)
    public PyObject __call__(PyObject self, PyObject[] args, LinkedHashMap<String, PyObject> kwargs) {
        if (self == this) {
            if (args.length == 1) {
                return args[0].getType();

            } else if (args.length == 3) {
                // FIXME ?
                throw new CafeBabePyException("Not implement");

            } else {
                throw this.runtime.newRaiseTypeError("type() takes 1 or 3 arguments");
            }
        }

        PyObject object = this.runtime.getattr(self, __new__).call(self);
        this.runtime.getattr(object, __init__).call(args, kwargs);

        return object;
    }

    @DefinePyFunction(name = __new__)
    public PyObject __new__(PyObject self, PyObject cls) {
        if (!cls.isType()) {
            throw this.runtime.newRaiseTypeError(
                    "object.__new__(X): X is not a type object ("
                            + cls.getFullName()
                            + ")");
        }

        if (cls.getClass() == PyTupleType.class) {
            return new PyTupleObject(this.runtime);
        }

        return new PyObjectObject(this.runtime, cls);
    }

    @DefinePyFunction(name = __getattribute__)
    public PyObject __getattribute__(PyObject cls, PyObject key) {
        return this.runtime.builtins_type__getattribute__(cls, key).orElseGet(() -> {
            Optional<PyObject> specialVar;
            if (__name__.equals(key.toJava(String.class))) {
                specialVar = cls.getScope().get(key, false);

            } else {
                specialVar = Optional.empty();
            }

            if (specialVar.isPresent()) {
                return specialVar.get();
            }

            throw this.runtime.newRaiseException("AttributeError",
                    "type object '" + cls.getName() + "' object has no attribute '" + key.toJava(String.class) + "'");
        });
    }
}
