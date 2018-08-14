package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.DefinePyType;

/**
 * Created by yotchang4s on 2017/05/29.
 */
@DefinePyType(name = "_ast.stmt", parent = "_ast.AST")
public class PyStmtTypeType extends AbstractAST {

    public PyStmtTypeType(Python runtime) {
        super(runtime);
    }
}
