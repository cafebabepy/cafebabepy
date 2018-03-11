package org.cafebabepy.runtime.module.types;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;

import static org.cafebabepy.util.ProtocolNames.__get__;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefinePyType(name = "types.FunctionType")
public class PyFunctionTypeType extends AbstractCafeBabePyType {

    public PyFunctionTypeType(Python runtime) {
        super(runtime);
    }

    @Override
    public String getName() {
        return "function";
    }

    @DefinePyFunction(name = __get__)
    public PyObject __get__(PyObject self, PyObject... args) {
        if (args.length == 0) {
            throw this.runtime.newRaiseTypeError("expected at least 1 arguments, got 0");
        } else if (args.length > 2) {
            throw this.runtime.newRaiseTypeError("expected at most 2 arguments, got " + args.length);
        }

        return this.runtime.newPyObject("types.MethodType", self, args[0]);
    }
}
