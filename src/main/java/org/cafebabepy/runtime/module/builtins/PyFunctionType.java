package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.runtime.object.proxy.PyMethodTypeObject;

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

        getScope().put(this.runtime.str(__code__), this.runtime.typeOrThrow("builtins.code", false));

        // FIXME stub
        getScope().put(this.runtime.str(__globals__), this.runtime.dict());
    }

    @DefinePyFunction(name = __get__)
    public PyObject __get__(PyObject self, PyObject... args) {
        if (args.length == 0) {
            throw this.runtime.newRaiseTypeError("expected at least 1 arguments, got 0");

        } else if (args.length > 2) {
            throw this.runtime.newRaiseTypeError("expected at most 2 arguments, got " + args.length);
        }

        if (args[0].isNone() && !this.runtime.isSubClass(args[1], "builtins.NoneType", false)) {
            return self;
        }

        //return this.runtime.newPyObject("method", self, args[0]);
        return new PyMethodTypeObject(this.runtime, self, args[0]);
    }
}
