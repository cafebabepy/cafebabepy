package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.DefinePyType;

/**
 * Created by yotchang4s on 2018/05/03.
 */
@DefinePyType(name = "_ast.NotIn", parent = {"_ast.cmpop"})
public class PyNotInType extends AbstractAST {

    public PyNotInType(Python runtime) {
        super(runtime);
    }
}
