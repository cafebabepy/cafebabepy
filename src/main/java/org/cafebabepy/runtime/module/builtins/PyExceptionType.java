package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;

import java.util.Map;

import static org.cafebabepy.util.ProtocolNames.__init__;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefinePyType(name = "builtins.Exception", parent = {"builtins.BaseException"})
public class PyExceptionType extends AbstractCafeBabePyType {

    public PyExceptionType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject[] args, Map<String, PyObject> kwargs) {
        if (!kwargs.isEmpty()) {
            throw this.runtime.newRaiseTypeError("Exception does not take keyword arguments");
        }

        this.runtime.setattr(self, "args", this.runtime.tuple(args));
    }
}
