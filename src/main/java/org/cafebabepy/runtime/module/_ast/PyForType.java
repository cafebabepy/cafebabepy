package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;

import static org.cafebabepy.util.ProtocolNames.__init__;

/**
 * Created by yotchang4s on 2017/05/29.
 */
@DefinePyType(name = "_ast.For", parent = {"_ast.stmt"})
public class PyForType extends AbstractCafeBabePyType {

    public PyForType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject... args) {
        if (args.length == 0) {
            return;
        }

        self.getScope().put(this.runtime.str("target"), args[0]);
        self.getScope().put(this.runtime.str("iter"), args[1]);
        self.getScope().put(this.runtime.str("body"), args[2]);
        self.getScope().put(this.runtime.str("orelse"), args[3]);
    }
}
