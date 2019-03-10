package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.runtime.object.proxy.PyMethodObject;

import static org.cafebabepy.util.ProtocolNames.__init__;
import static org.cafebabepy.util.ProtocolNames.__str__;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefinePyType(name = "builtins.method", appear = false)
public class PyMethodType extends AbstractCafeBabePyType {

    public PyMethodType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject func, PyObject instance) {
        self.getFrame().getNotAppearLocals().put("func", func);
        self.getFrame().getNotAppearLocals().put("instance", instance);
    }

    @DefinePyFunction(name = __str__)
    public PyObject __str__(PyObject self) {
        if (!this.equals(self.getType())) {
            return this.runtime.str(self);
        }

        if (!(self instanceof PyMethodObject)) {
            throw new CafeBabePyException(self + " is not method");
        }

        PyMethodObject object = (PyMethodObject) self;

        return this.runtime.str("<bound method "
                + object.getSource().getName() + "." + object.getFunction().getFullName()
                + " of " + this.runtime.str(object.getSource()) + ">");
    }
}
