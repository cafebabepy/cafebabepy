package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.DefinePyType;

/**
 * Created by yotchang4s on 2018/06/03.
 */
@DefinePyType(name = "_ast.excepthandler", parent = "_ast.AST")
public class PyexcepthandlerTypeType extends AbstractAST {

    public PyexcepthandlerTypeType(Python runtime) {
        super(runtime);
    }
}
