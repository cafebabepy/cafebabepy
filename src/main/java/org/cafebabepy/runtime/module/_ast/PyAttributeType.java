package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;

import static org.cafebabepy.util.ProtocolNames.__init__;

/**
 * Created by yotchang4s on 2017/05/29.
 */
@DefinePyType(name = "_ast.Attribute", parent = {"_ast.expr"})
public class PyAttributeType extends AbstractAST {

    public PyAttributeType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject... args) {
        if (args.length == 0) {
            return;
        }

        self.getScope().put(this.runtime.str("value"), args[0]);
        self.getScope().put(this.runtime.str("attr"), args[1]);
        self.getScope().put(this.runtime.str("ctx"), args[2]);
    }

    @Override
    String[] _fields() {
        return new String[]{"value", "attr", "ctx"};
    }
}