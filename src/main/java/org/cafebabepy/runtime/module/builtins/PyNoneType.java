package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefineCafeBabePyFunction;
import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

import static org.cafebabepy.util.ProtocolNames.__bool__;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefineCafeBabePyType(name = "builtins.None", appear = false)
public class PyNoneType extends AbstractCafeBabePyType {

    public PyNoneType(Python runtime) {
        super(runtime);
    }

    @DefineCafeBabePyFunction(name = __bool__)
    public PyObject __bool__(PyObject self) {
        PyObject noneType = this.runtime.typeOrThrow("builtins.None", false);

        if (this.runtime.callFunction("builtins.isinstance", self, noneType).isFalse()) {
            throw this.runtime.newRaiseTypeError(
                    "'__eq__' requires a 'int' object but received a '" + self.getFullName() + "'");
        }

        return this.runtime.False();
    }
}
