package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.runtime.object.PyObjectObject;

import static org.cafebabepy.util.ProtocolNames.*;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefinePyType(name = "builtins.type")
public final class PyTypeType extends AbstractCafeBabePyType {

    public PyTypeType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __call__)
    public PyObject __call__(PyObject self, PyObject... args) {
        PyObject object = self.getScope().getOrThrow(__new__).call();
        object.getScope().getOrThrow(__init__).call(args);

        return object;
    }

    @DefinePyFunction(name = __new__)
    public PyObject __new__(PyObject cls) {
        if (!cls.isType()) {
            throw this.runtime.newRaiseTypeError(
                    "object.__new__(X): X is not a type object ("
                            + cls.getFullName()
                            + ")");
        }

        return new PyObjectObject(this.runtime, cls);
    }
}
