package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;

import static org.cafebabepy.util.ProtocolNames.__init__;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefinePyType(name = "builtins.MethodType")
public class PyMethodTypeType extends AbstractCafeBabePyType {

    public PyMethodTypeType(Python runtime) {
        super(runtime);
    }

    @Override
    public String getName() {
        return "method";
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject func, PyObject instance) {
        self.getScope().put(this.runtime.str("func"), func, false);
    }
}

