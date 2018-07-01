package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;

import static org.cafebabepy.util.ProtocolNames.__get__;
import static org.cafebabepy.util.ProtocolNames.__init__;

/**
 * Created by yotchang4s on 2018/06/26.
 */
@DefinePyType(name = "builtins.staticmethod")
public class PyStaticMethodType extends AbstractCafeBabePyType {

    public PyStaticMethodType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject f) {
        self.getScope().put(this.runtime.str("f"), f, false);
    }

    @DefinePyFunction(name = __get__)
    public PyObject __get__(PyObject self, PyObject obj, PyObject objType) {
        return self.getScope().get(this.runtime.str("f"), false).orElseThrow(() ->
                this.runtime.newRaiseException("builtins.RuntimeError", "uninitialized staticmethod object")
        );
    }
}
