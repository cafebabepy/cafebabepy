package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;

import static org.cafebabepy.util.ProtocolNames.__init__;

/**
 * Created by yotchang4s on 2018/04/03.
 */
@DefinePyType(name = "_ast.Subscript", parent = {"_ast.expr"})
public class PySubscriptType extends AbstractAST {

    public PySubscriptType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject... args) {
        if (args.length == 0) {
            return;
        }

        self.getFrame().getLocals().put("value", args[0]);
        self.getFrame().getLocals().put("slice", args[1]);
        self.getFrame().getLocals().put("ctx", args[2]);
    }

    @Override
    String[] _fields() {
        return new String[]{"value", "slice", "ctx"};
    }
}
