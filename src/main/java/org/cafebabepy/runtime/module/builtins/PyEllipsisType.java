package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

import static org.cafebabepy.util.ProtocolNames.__str__;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefinePyType(name = "builtins.Ellipsis")
public class PyEllipsisType extends AbstractCafeBabePyType {

    public PyEllipsisType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __str__)
    public PyObject __str__(PyObject self) {
        if (self.getType() instanceof PyEllipsisType) {
            return this.runtime.str("Ellipsis");

        } else {
            return self.getScope().getOrThrow(__str__).call();
        }
    }
}
