package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.DefinePyType;

/**
 * Created by yotchang4s on 2017/05/29.
 */
@DefinePyType(name = "_ast.Add", parent = {"_ast.operator"})
public class PyAddType extends AbstractAST {

    public PyAddType(Python runtime) {
        super(runtime);
    }
}
