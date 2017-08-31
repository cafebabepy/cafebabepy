package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.annotation.DefinePyFunction;
import org.cafebabepy.annotation.DefinePyType;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

import static org.cafebabepy.util.ProtocolNames.__init__;

/**
 * Created by yotchang4s on 2017/05/29.
 */
@DefinePyType(name = "_ast.arguments", parent = {"_ast.AST"})
public class PyArgumentsType extends AbstractCafeBabePyType {

    public PyArgumentsType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject... args) {
        if (args.length == 0) {
            return;
        }

        self.getScope().put("args", args[0]);
        self.getScope().put("vararg", args[1]);
        self.getScope().put("kwonlyargs", args[2]);
        self.getScope().put("kw_defaults", args[3]);
        self.getScope().put("kwarg", args[4]);
        self.getScope().put("defaults", args[5]);
    }
}