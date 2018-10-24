package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.DefinePyType;

/**
 * Created by yotchang4s on 2018/10/24.
 */
@DefinePyType(name = "_ast.FloorDiv", parent = {"_ast.operator"})
public class PyFloorDivType extends AbstractAST {

    public PyFloorDivType(Python runtime) {
        super(runtime);
    }
}
