package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.annotation.DefinePyFunction;
import org.cafebabepy.annotation.DefinePyType;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

import static org.cafebabepy.util.ProtocolNames.__init__;

/**
 * Created by yotchang4s on 2017/05/29.
 */
@DefinePyType(name = "_ast.IfExp", parent = {"_ast.expr"})
public class PyIfExpType extends AbstractCafeBabePyType {

    public PyIfExpType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject... args) {
        if (args.length == 0) {
            return;
        }

        self.getScope().put("test", args[0]);
        self.getScope().put("body", args[1]);
        self.getScope().put("orelse", args[2]);
    }
}
