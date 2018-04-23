package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;

import static org.cafebabepy.util.ProtocolNames.__init__;
import static org.cafebabepy.util.ProtocolNames.__str__;

/**
 * Created by yotchang4s on 2018/04/22.
 */
@DefinePyType(name = "builtins.slice")
public class PySliceType extends AbstractCafeBabePyType {

    public PySliceType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject... args) {
        if (args.length == 0) {
            throw this.runtime.newRaiseTypeError("slice expected 1 arguments, got 0");

        } else if (3 < args.length) {
            throw this.runtime.newRaiseTypeError("slice expected at most 3 arguments, got " + args.length);
        }

        PyObject start;
        PyObject stop;
        PyObject step;

        if (args.length == 1) {
            start = this.runtime.None();
            stop = args[0];
            step = this.runtime.None();

        } else if (args.length == 2) {
            start = args[0];
            stop = args[1];
            step = this.runtime.None();

        } else {
            start = args[0];
            stop = args[1];
            step = args[2];
        }

        self.getScope().put("start", start);
        self.getScope().put("stop", stop);
        self.getScope().put("step", step);
    }

    @DefinePyFunction(name = __str__)
    public PyObject __str__(PyObject self) {
        if (!this.runtime.isInstance(self, this)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__str__' requires a 'range' object but received a '"
                            + self.getType().getFullName()
                            + "'");
        }

        PyObject start = this.runtime.getattr(self, "start");
        PyObject stop = this.runtime.getattr(self, "stop");
        PyObject step = this.runtime.getattr(self, "step");

        return this.runtime.str("slice(" + start + ", " + stop + ", " + step + ")");
    }
}
