package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.PyFunctionObject;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyFunctionDefaultValue;
import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.runtime.object.proxy.PyMethodObject;

import static org.cafebabepy.util.ProtocolNames.*;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefinePyType(name = "builtins.function", appear = false)
public class PyFunctionType extends AbstractCafeBabePyType {

    public PyFunctionType(Python runtime) {
        super(runtime);
    }

    @Override
    public void initialize() {
        super.initialize();

        getFrame().getLocals().put(__code__, this.runtime.typeOrThrow("builtins.code", false));
    }

    @DefinePyFunction(name = __get__)
    public PyObject __get__(PyObject self, PyObject obj, PyObject type) {
        if (!this.runtime.isInstance(self, "builtins.function", false)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__get__' requires a 'function' object but received a '" + self.getFullName() + "'");
        }

        if (obj.isNone() && !this.runtime.isSubClass(type, "builtins.NoneType", false)) {
            return self;
        }

        if (!this.runtime.isInstance(self, "builtins.function", false)) {
            return self;
        }

        //return this.runtime.newPyObject("method", self, args[0]);
        return new PyMethodObject(this.runtime, (PyFunctionObject) self, obj);
    }

    @DefinePyFunctionDefaultValue(methodName = __get__, parameterName = "type")
    public PyObject __get__type() {
        return this.runtime.None();
    }

    @DefinePyFunction(name = __str__)
    public PyObject __str__(PyObject self) {
        if (!this.equals(self.getType())) {
            return this.runtime.str(self);
        }

        String hashCode = Integer.toHexString(System.identityHashCode(self));

        return this.runtime.str("<function '" + self.getFullName() + "' at 0x" + hashCode + ">");
    }
}
