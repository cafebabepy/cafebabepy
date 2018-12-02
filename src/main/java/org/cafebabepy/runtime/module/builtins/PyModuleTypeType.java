package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;

import static org.cafebabepy.util.ProtocolNames.__setattr__;

/**
 * Created by yotchang4s on 2017/05/12.
 */
@DefinePyType(name = "builtins.ModuleType", appear = false)
public class PyModuleTypeType extends AbstractCafeBabePyType {

    public PyModuleTypeType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __setattr__)
    public void __setattr__(PyObject self, PyObject name, PyObject value) {
        PyObject strType = this.runtime.typeOrThrow("builtins.str");

        if (!this.runtime.isInstance(name, strType)) {
            throw this.runtime.newRaiseTypeError(
                    "attribute name must be string, not '" + name.getFullName() + "'"
            );
        }

        self.getFrame().getLocals().put(name.toJava(String.class), value);
    }
}
