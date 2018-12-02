package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;

import static org.cafebabepy.util.ProtocolNames.__init__;

/**
 * Created by yotchang4s on 2018/04/01.
 */
@DefinePyType(name = "_ast.Tuple", parent = {"_ast.expr"})
public class PyTupleType extends AbstractAST {

    public PyTupleType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject... args) {
        if (args.length == 0) {
            return;
        }

        self.getFrame().putToLocals("elts", args[0]);
        self.getFrame().putToLocals("ctx", args[1]);
    }

    @Override
    String[] _fields() {
        return new String[]{"elts", "ctx"};
    }
}
