package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;

import static org.cafebabepy.util.ProtocolNames.__init__;

/**
 * Created by yotchang4s on 2017/08/05.
 */
@DefinePyType(name = "_ast.FormattedValue", parent = {"_ast.expr"})
public class PyFormattedValueType extends AbstractAST {

    public PyFormattedValueType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject... args) {
        if (args.length == 0) {
            return;
        }

        self.getFrame().putToLocals("value", args[0]);
        self.getFrame().putToLocals("conversion", args[1]);
        self.getFrame().putToLocals("format_spec", args[2]);
    }

    @Override
    String[] _fields() {
        return new String[]{"value", "conversion", "format_spec"};
    }
}
