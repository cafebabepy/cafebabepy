package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;

import static org.cafebabepy.util.ProtocolNames.__get__;
import static org.cafebabepy.util.ProtocolNames.__init__;

/**
 * Created by yotchang4s on 2018/07/01.
 */
@DefinePyType(name = "builtins.classmethod")
public class PyClassMethodType extends AbstractCafeBabePyType {

    public PyClassMethodType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject f) {
        self.getFrame().putToNotAppearLocals("f", f);
    }

    @DefinePyFunction(name = __get__)
    public PyObject __get__(PyObject self, PyObject obj, PyObject klass) {
        PyObject f = self.getFrame().getFromNotAppearLocals("f").orElseThrow(() ->
                this.runtime.newRaiseException("builtins.RuntimeError", "uninitialized staticmethod object")
        );

        if (klass.isNone()) {
            klass = obj.getType();
        }

        this.runtime.pushNewContext();
        try {
            this.runtime.setattr(this.runtime.getCurrentContext(), "f", f);
            this.runtime.setattr(this.runtime.getCurrentContext(), "klass", klass);


            return this.runtime.eval("<classmethod>", ""
                    + "def newfunc(*args):\n"
                    + "  return f(klass, *args)\n"
                    + "newfunc");

        } finally {
            this.runtime.popContext();
        }
    }
}
