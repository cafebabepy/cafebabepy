package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;

import static org.cafebabepy.util.ProtocolNames.__init__;

/**
 * Created by yotchang4s on 2017/05/29.
 */
@DefinePyType(name = "_ast.For", parent = {"_ast.stmt"})
public class PyForType extends AbstractAST {

    public PyForType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject... args) {
        if (args.length == 0) {
            return;
        }

        self.getFrame().putToLocals("target", args[0]);
        self.getFrame().putToLocals("iter", args[1]);
        self.getFrame().putToLocals("body", args[2]);
        self.getFrame().putToLocals("orelse", args[3]);
    }

    @Override
    String[] _fields() {
        return new String[]{"target", "iter", "body", "orelse"};
    }
}
