package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.DefinePyType;

/**
 * Created by yotchang4s on 2017/05/29.
 */
@DefinePyType(name = "_ast.expr", parent = "_ast.AST")
public class PyExprTypeType extends AbstractAST {

    public PyExprTypeType(Python runtime) {
        super(runtime);
    }
}
