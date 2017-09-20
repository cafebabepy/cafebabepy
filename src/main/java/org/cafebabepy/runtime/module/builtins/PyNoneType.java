package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefinePyFunction;
import org.cafebabepy.annotation.DefinePyType;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

import static org.cafebabepy.util.ProtocolNames.__bool__;
import static org.cafebabepy.util.ProtocolNames.__str__;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefinePyType(name = "builtins.None", appear = false)
public class PyNoneType extends AbstractCafeBabePyType {

    public PyNoneType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __bool__)
    public PyObject __bool__(PyObject self) {
        PyObject noneType = this.runtime.typeOrThrow("builtins.None", false);

        if (this.runtime.callFunction("builtins.isinstance", self, noneType).isFalse()) {
            throw this.runtime.newRaiseTypeError(
                    "'__bool__' requires a 'None' object but received a '" + self.getFullName() + "'");
        }

        return this.runtime.False();
    }

    @DefinePyFunction(name = __str__)
    public PyObject __str__(PyObject self) {
        if (self.getType() instanceof PyNoneType) {
            return this.runtime.str("None");

        } else {
            return self.getScope().getOrThrow(__str__).call();
        }
    }
}
