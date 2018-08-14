package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.DefinePyType;

/**
 * Created by yotchang4s on 2018/05/03.
 */
@DefinePyType(name = "_ast.In", parent = {"_ast.cmpop"})
public class PyInType extends AbstractAST {

    public PyInType(Python runtime) {
        super(runtime);
    }
}
