package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.annotation.DefineCafeBabePyFunction;
import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

import static org.cafebabepy.util.ProtocolNames.__init__;

/**
 * Created by yotchang4s on 2017/08/09.
 */
@DefineCafeBabePyType(name = "_ast.ClassDef", parent = {"_ast.stmt"})
public class PyClassDefType extends AbstractCafeBabePyType {

    public PyClassDefType(Python runtime) {
        super(runtime);
    }

    @DefineCafeBabePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject... args) {
        if (args.length == 0) {
            return;
        }

        self.getScope().put("name", args[0]);
        self.getScope().put("bases", args[1]);
        self.getScope().put("keywords", args[2]);
        self.getScope().put("body", args[3]);
        self.getScope().put("decorator_list", args[4]);
    }
}