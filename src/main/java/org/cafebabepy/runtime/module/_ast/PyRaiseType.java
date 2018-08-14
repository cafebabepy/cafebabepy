package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;

import static org.cafebabepy.util.ProtocolNames.__init__;

/**
 * Created by yotchang4s on 2018/06/03.
 */
@DefinePyType(name = "_ast.Raise", parent = "_ast.stmt")
public class PyRaiseType extends AbstractAST {

    public PyRaiseType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject... args) {
        if (args.length == 0) {
            return;
        }

        self.getScope().put(this.runtime.str("exc"), args[0]);
        self.getScope().put(this.runtime.str("cause"), args[1]);
    }

    @Override
    String[] _fields() {
        return new String[]{"exc", "cause"};
    }
}
