package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;

import static org.cafebabepy.util.ProtocolNames.__init__;

/**
 * Created by yotchang4s on 2017/05/31.
 */
@DefinePyType(name = "builtins.StopIteration", parent = {"builtins.Exception"})
public class PyStopIterationType extends AbstractCafeBabePyType {

    public PyStopIterationType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject... args) {
        // FIXME super
        self.getFrame().putToLocals("args", this.runtime.tuple(args));

        PyObject value = this.runtime.None();
        if (args.length > 0) {
            value = args[0];
        }

        self.getFrame().putToLocals("value", value);
    }
}
