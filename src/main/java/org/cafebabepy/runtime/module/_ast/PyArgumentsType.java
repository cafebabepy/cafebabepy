package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;

import static org.cafebabepy.util.ProtocolNames.__init__;

/**
 * Created by yotchang4s on 2017/05/29.
 */
@DefinePyType(name = "_ast.arguments", parent = {"_ast.AST"})
public class PyArgumentsType extends AbstractAST {

    public PyArgumentsType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject... args) {
        if (args.length == 0) {
            return;
        }

        self.getFrame().getLocals().put("args", args[0]);
        self.getFrame().getLocals().put("vararg", args[1]);
        self.getFrame().getLocals().put("kwonlyargs", args[2]);
        self.getFrame().getLocals().put("kw_defaults", args[3]);
        self.getFrame().getLocals().put("kwarg", args[4]);
        self.getFrame().getLocals().put("defaults", args[5]);
    }

    @Override
    String[] _fields() {
        return new String[]{"args", "vararg", "kwonlyargs", "kw_defaults", "kwarg", "defaults"};
    }
}