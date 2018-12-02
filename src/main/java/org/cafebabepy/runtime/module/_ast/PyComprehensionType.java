package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;

import static org.cafebabepy.util.ProtocolNames.__init__;

/**
 * Created by yotchang4s on 2017/05/29.
 */
@DefinePyType(name = "_ast.comprehension", parent = {"_ast.AST"})
public class PyComprehensionType extends AbstractAST {

    public PyComprehensionType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject... args) {
        if (args.length == 0) {
            return;
        }

        self.getFrame().putToLocals("target", args[0]);
        self.getFrame().putToLocals("iter", args[1]);
        self.getFrame().putToLocals("ifs", args[2]);
        self.getFrame().putToLocals("is_async", args[3]);
    }

    @Override
    String[] _fields() {
        return new String[]{"target", "iter", "ifs", "is_async"};
    }
}
