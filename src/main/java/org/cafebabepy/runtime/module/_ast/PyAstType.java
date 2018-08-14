package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.DefinePyType;

/**
 * Created by yotchang4s on 2017/05/29.
 */
@DefinePyType(name = "_ast.AST")
public class PyAstType extends AbstractAST {

    public PyAstType(Python runtime) {
        super(runtime);
    }
}
