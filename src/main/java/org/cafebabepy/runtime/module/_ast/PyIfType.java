package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;

import static org.cafebabepy.util.ProtocolNames.__init__;

/**
 * Created by yotchang4s on 2017/05/29.
 */
@DefinePyType(name = "_ast.If", parent = {"_ast.stmt"})
public class PyIfType extends AbstractAST {

    public PyIfType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject... args) {
        if (args.length == 0) {
            return;
        }

        self.getFrame().getLocals().put("test", args[0]);
        self.getFrame().getLocals().put("body", args[1]);
        self.getFrame().getLocals().put("orelse", args[2]);
    }

    @Override
    String[] _fields() {
        return new String[]{"test", "body", "orelse"};
    }
}
