package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyFunctionDefaultValue;
import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.runtime.object.java.PyMethodWrapperObject;

import java.util.List;
import java.util.Optional;

import static org.cafebabepy.util.ProtocolNames.*;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefinePyType(name = "builtins.super")
public class PySuperType extends AbstractCafeBabePyType {

    public PySuperType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject type, PyObject objectOrType) {
        if (!type.isType()) {
            throw this.runtime.newRaiseTypeError("super() argument 1 must be type, not " + type.getFullName());
        }

        if (!objectOrType.isNone()) {
            if (objectOrType.isType()) {
                if (!this.runtime.isSubClass(objectOrType, type)) {
                    throw this.runtime.newRaiseTypeError("super(type, obj): obj must be an instance or subtype of type");
                }

            } else {
                if (!this.runtime.isInstance(objectOrType, type)) {
                    throw this.runtime.newRaiseTypeError("super(type, obj): obj must be an instance or subtype of type");
                }
            }
        }

        self.getFrame().getNotAppearLocals().put("type", type);
        self.getFrame().getNotAppearLocals().put("objectOrType", objectOrType);
    }

    @DefinePyFunctionDefaultValue(methodName = __init__, parameterName = "objectOrType")
    public PyObject __init__objectOrType() {
        return this.runtime.None();
    }

    @DefinePyFunction(name = __getattribute__)
    public PyObject __getattribute__(PyObject self, PyObject name) {
        PyObject strType = this.runtime.typeOrThrow("builtins.str");

        if (!this.runtime.isInstance(name, strType)) {
            throw this.runtime.newRaiseTypeError(
                    "attribute name must be string, not '" + name.getFullName() + "'"
            );
        }

        String javaName = name.toJava(String.class);

        PyObject type = self.getFrame().getNotAppearLocals().get("type");
        PyObject objectOrType = self.getFrame().getNotAppearLocals().get("objectOrType");
        if (type == null || objectOrType == null) {
            throw new CafeBabePyException("type or objectOrType is not found");
        }

        if (__get__.equals(javaName)) {
            PyObject get = self.getType().getFrame().getLocals().get(__get__);
            if (get != null) {
                return get;
            }
        }

        PyObject startType;
        if (this.runtime.isInstance(objectOrType, type)) {
            startType = objectOrType.getType();

        } else {
            startType = objectOrType;
        }

        List<PyObject> types = type.getTypes();
        int index = 0;
        for (; index < types.size(); index++) {
            if (types.get(index).equals(startType)) {
                index++;
                break;
            }
        }

        for (; index < types.size(); index++) {
            PyObject t = types.get(index);
            PyObject x = t.getFrame().getLocals().get(javaName);
            if (x != null) {
                Optional<PyObject> xgetOpt = this.runtime.getattrOptional(x, __get__);
                if (xgetOpt.isPresent()) {
                    return xgetOpt.get().call(objectOrType, t);

                } else {
                    return x;
                }
            }
        }

        throw this.runtime.newRaiseException("builtins.AttributeError",
                "'super' object has no attribute '" + name.toJava(String.class) + "'");
    }

    @DefinePyFunction(name = __get__)
    public PyObject __get__(PyObject self, PyObject obj, PyObject type) {
        PyObject t = self.getFrame().getNotAppearLocals().get("type");
        PyObject oot = self.getFrame().getNotAppearLocals().get("objectOrType");
        if (oot.isNone() && !obj.isNone()) {
            return this.call(t, obj);

        } else {
            return self;
        }
    }

    @DefinePyFunctionDefaultValue(methodName = __get__, parameterName = "type")
    public PyObject __get__type() {
        return this.runtime.None();
    }
}