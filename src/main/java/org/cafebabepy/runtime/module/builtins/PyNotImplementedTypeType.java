package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefinePyFunction;
import org.cafebabepy.annotation.DefinePyType;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

import static org.cafebabepy.util.ProtocolNames.__str__;

/**
 * Created by yotchang4s on 2017/05/13.
 */
// TODO builtins????
@DefinePyType(name = "builtins.NotImplementedType", appear = false)
public class PyNotImplementedTypeType extends AbstractCafeBabePyType {

    public PyNotImplementedTypeType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __str__)
    public PyObject __str__(PyObject self) {
        if (this.runtime.isInstance(self, "builtins.NotImplementedType")) {
            return this.runtime.str("NotImplemented");

        } else {
            return self.getScope().getOrThrow(__str__).call();
        }
    }
}
