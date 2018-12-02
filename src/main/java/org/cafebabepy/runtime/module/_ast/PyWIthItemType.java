package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;

import static org.cafebabepy.util.ProtocolNames.__init__;

/**
 * Created by yotchang4s on 2018/07/13.
 */
@DefinePyType(name = "_ast.withitem", parent = {"_ast.AST"})
public class PyWIthItemType extends AbstractAST {

    public PyWIthItemType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject... args) {
        if (args.length == 0) {
            return;
        }

        self.getFrame().getLocals().put("context_expr", args[0]);
        self.getFrame().getLocals().put("optional_vars", args[1]);
    }

    @Override
    String[] _fields() {
        return new String[]{"context_expr", "optional_vars"};
    }
}
