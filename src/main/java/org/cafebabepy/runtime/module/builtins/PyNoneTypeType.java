package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;

import static org.cafebabepy.util.ProtocolNames.__bool__;
import static org.cafebabepy.util.ProtocolNames.__str__;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefinePyType(name = "builtins.NoneType", appear = false)
public class PyNoneTypeType extends AbstractCafeBabePyType {

    public PyNoneTypeType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __bool__)
    public PyObject __bool__(PyObject self) {
        PyObject noneType = this.runtime.typeOrThrow("builtins.NoneType", false);

        if (this.runtime.isInstance(self, noneType)) {
            throw this.runtime.newRaiseTypeError(
                    "'__bool__' requires a 'NoneType' object but received a '" + self.getFullName() + "'");
        }

        return this.runtime.False();
    }

    @DefinePyFunction(name = __str__)
    public PyObject __str__(PyObject self) {
        if (self.getType() instanceof PyNoneTypeType) {
            return this.runtime.str("None");

        } else {
            return this.runtime.getattr(self, __str__).call();
        }
    }
}
