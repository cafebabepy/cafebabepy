package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;

import static org.cafebabepy.util.ProtocolNames.__init__;

/**
 * Created by yotchang4s on 2018/06/24.
 */
@DefinePyType(name = "builtins.method-wrapper", appear = false)
public class PyMethodWrapperType extends AbstractCafeBabePyType {

    public PyMethodWrapperType(Python runtime) {
        super(runtime);
    }

    @Override
    public String getName() {
        return "method-wrapper";
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject func, PyObject instance) {
    }
}
