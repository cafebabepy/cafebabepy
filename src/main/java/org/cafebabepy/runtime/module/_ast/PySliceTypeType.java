package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.DefinePyType;

/**
 * Created by yotchang4s on 2018/04/03.
 */
@DefinePyType(name = "_ast.slice", parent = {"_ast.AST"})
public class PySliceTypeType extends AbstractAST {

    public PySliceTypeType(Python runtime) {
        super(runtime);
    }
}
