package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;

import static org.cafebabepy.util.ProtocolNames.__init__;

/**
 * Created by yotchang4s on 2018/06/03.
 */
@DefinePyType(name = "_ast.Try", parent = "_ast.stmt")
public class PyTryType extends AbstractAST {

    public PyTryType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject... args) {
        if (args.length == 0) {
            return;
        }

        self.getFrame().getLocals().put("body", args[0]);
        self.getFrame().getLocals().put("handlers", args[1]);
        self.getFrame().getLocals().put("orelse", args[2]);
        self.getFrame().getLocals().put("finalbody", args[3]);
    }

    @Override
    String[] _fields() {
        return new String[]{"body", "handlers", "orelse", "finalbody"};
    }
}
