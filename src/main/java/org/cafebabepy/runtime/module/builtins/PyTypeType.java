package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefinePyFunction;
import org.cafebabepy.annotation.DefinePyType;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

import java.util.Optional;

import static org.cafebabepy.util.ProtocolNames.*;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefinePyType(name = "builtins.type")
public final class PyTypeType extends AbstractCafeBabePyType {

    public PyTypeType(Python runtime) {
        super(runtime);
    }

    public PyObject __call__(PyObject name) {
        // FIXME 3 arguments version
        return name.getType();
    }

    @DefinePyFunction(name = __getattribute__)
    public PyObject __getattribute__(PyObject self, PyObject key) {
        PyObject getattribute = this.runtime.Object().getScope().getOrThrow(__getattribute__);
        PyObject v = getattribute.call(self, key);

        Optional<PyObject> getOpt = v.getScope().get(__get__);
        if (getOpt.isPresent()) {
            PyObject get = getOpt.get();
            return get.call(this.runtime.None(), self);

        } else {
            return v;
        }
    }
}
