package org.cafebabepy.runtime.module.sys;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;

import static org.cafebabepy.util.ProtocolNames.*;

/**
 * Created by yotchang4s on 2018/06/03.
 */
@DefinePyType(name = "sys.version_info", appear = false)
public final class Pyversion_infoType extends AbstractCafeBabePyType {

    public Pyversion_infoType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __call__)
    public PyObject __call__(PyObject self, PyObject... args) {
        System.out.println(__call__);
        return this.runtime.None();
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self) {
        self.getFrame().putToLocals("major", this.runtime.number(Python.MAJOR));
        self.getFrame().putToLocals("minor", this.runtime.number(Python.MINOR));
        self.getFrame().putToLocals("micro", this.runtime.number(Python.MICRO));
        self.getFrame().putToLocals("releaselevel", this.runtime.str(Python.RELEASE_LEVEL));
        self.getFrame().putToLocals("serial", this.runtime.number(Python.SERIAL));
    }

    @DefinePyFunction(name = __str__)
    public PyObject __str__(PyObject self) {
        if (!this.runtime.isInstance(self, "sys.version_info", false)) {
            throw this.runtime.newRaiseTypeError("descriptor '__str__' requires a 'sys.version_info' object but received a '" + self.getFullName() + "'");
        }

        String builder = "sys.version_info(" +
                "major=" + this.runtime.getattr(self, "major") +
                ", minor=" + this.runtime.getattr(self, "minor") +
                ", micro=" + this.runtime.getattr(self, "micro") +
                ", releaselevel=" + this.runtime.getattr(self, "releaselevel") +
                ", serial=" + this.runtime.getattr(self, "serial") +
                ")";

        return this.runtime.str(builder);
    }
}
