package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.DefinePyType;

/**
 * Created by yotchang4s on 2018/05/03.
 */
@DefinePyType(name = "_ast.Is", parent = {"_ast.cmpop"})
public class PyIsType extends AbstractAST {

    public PyIsType(Python runtime) {
        super(runtime);
    }
}
