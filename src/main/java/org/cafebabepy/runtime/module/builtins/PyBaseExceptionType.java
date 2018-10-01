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
@DefinePyType(name = "builtins.BaseException")
public class PyBaseExceptionType extends AbstractCafeBabePyType {

    public PyBaseExceptionType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject... args) {
        // FIXME 暫定対処
        if (args.length > 0) {
            this.runtime.setattr(this, "args", args[0]);
        }
    }
}
