package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;

import static org.cafebabepy.util.ProtocolNames.__init__;

/**
 * Created by yotchang4s on 2017/08/09.
 */
@DefinePyType(name = "_ast.ClassDef", parent = {"_ast.stmt"})
public class PyClassDefType extends AbstractCafeBabePyType {

    public PyClassDefType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject... args) {
        if (args.length == 0) {
            return;
        }

        self.getScope().put(this.runtime.str("name"), args[0]);
        self.getScope().put(this.runtime.str("bases"), args[1]);
        self.getScope().put(this.runtime.str("keywords"), args[2]);
        self.getScope().put(this.runtime.str("body"), args[3]);
        self.getScope().put(this.runtime.str("decorator_list"), args[4]);
    }
}
