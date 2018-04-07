package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;

import static org.cafebabepy.util.ProtocolNames.__init__;

/**
 * Created by yotchang4s on 2018/04/03.
 */
@DefinePyType(name = "_ast.Subscript", parent = {"_ast.expr"})
public class PySubscriptType extends AbstractCafeBabePyType {

    public PySubscriptType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject... args) {
        if (args.length == 0) {
            return;
        }

        self.getScope().put("value", args[0]);
        self.getScope().put("slice", args[1]);
        self.getScope().put("ctx", args[2]);
    }
}
