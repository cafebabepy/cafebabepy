package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

import static org.cafebabepy.util.ProtocolNames.__bool__;
import static org.cafebabepy.util.ProtocolNames.__str__;

/**
 * Created by yotchang4s on 2017/06/07.
 */
@DefinePyType(name = "builtins.bool", parent = {"builtins.int"})
public class PyBoolType extends AbstractCafeBabePyType {

    public PyBoolType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __bool__)
    public PyObject __bool__(PyObject self) {
        if (self.isFalse()) {
            return this.runtime.False();

        } else {
            return this.runtime.True();
        }
    }

    @DefinePyFunction(name = __str__)
    public PyObject __str__(PyObject self) {
        if (self.getType() instanceof PyBoolType) {
            if (self.isTrue()) {
                return this.runtime.str("True");
            } else {

                return this.runtime.str("False");
            }

        } else {
            return self.getScope().getOrThrow(__str__).call();
        }
    }
}
