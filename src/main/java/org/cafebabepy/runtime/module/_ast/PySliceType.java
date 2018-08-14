package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;

import static org.cafebabepy.util.ProtocolNames.__init__;

/**
 * Created by yotchang4s on 2018/04/21.
 */
@DefinePyType(name = "_ast.Slice", parent = {"_ast.slice"})
public class PySliceType extends AbstractAST {

    public PySliceType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject... args) {
        if (args.length == 0) {
            return;
        }

        self.getScope().put(this.runtime.str("lower"), args[0]);
        self.getScope().put(this.runtime.str("upper"), args[1]);
        self.getScope().put(this.runtime.str("step"), args[2]);
    }

    @Override
    String[] _fields() {
        return new String[]{"lower", "upper", "step"};
    }
}
