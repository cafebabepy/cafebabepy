package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;

import static org.cafebabepy.util.ProtocolNames.__init__;

/**
 * Created by yotchang4s on 2017/05/29.
 */
@DefinePyType(name = "_ast.Compare", parent = {"_ast.expr"})
public class PyCompareType extends AbstractAST {

    public PyCompareType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject... args) {
        if (args.length == 0) {
            return;
        }

        self.getFrame().getLocals().put("left", args[0]);
        self.getFrame().getLocals().put("ops", args[1]);
        self.getFrame().getLocals().put("comparators", args[2]);
    }

    @Override
    String[] _fields() {
        return new String[]{"left", "ops", "comparators"};
    }
}
