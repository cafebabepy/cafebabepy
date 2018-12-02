package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;

import static org.cafebabepy.util.ProtocolNames.__init__;

/**
 * Created by yotchang4s on 2017/08/09.
 */
@DefinePyType(name = "_ast.ClassDef", parent = {"_ast.stmt"})
public class PyClassDefType extends AbstractAST {

    public PyClassDefType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject... args) {
        if (args.length == 0) {
            return;
        }

        self.getFrame().getLocals().put("name", args[0]);
        self.getFrame().getLocals().put("bases", args[1]);
        self.getFrame().getLocals().put("keywords", args[2]);
        self.getFrame().getLocals().put("body", args[3]);
        self.getFrame().getLocals().put("decorator_list", args[4]);
    }

    @Override
    String[] _fields() {
        return new String[]{"name", "bases", "keywords", "body", "decorator_list"};
    }
}
