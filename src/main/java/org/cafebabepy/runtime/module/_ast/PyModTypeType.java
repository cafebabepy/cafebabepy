package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.DefinePyType;

/**
 * Created by yotchang4s on 2017/06/04.
 */
@DefinePyType(name = "_ast.mod", parent = {"_ast.AST"})
public class PyModTypeType extends AbstractAST {

    public PyModTypeType(Python runtime) {
        super(runtime);
    }
}
