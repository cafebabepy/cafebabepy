package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.DefinePyType;

/**
 * Created by yotchang4s on 2017/05/29.
 */
@DefinePyType(name = "_ast.USub", parent = {"_ast.unaryop"})
public class PyUSubType extends AbstractAST {

    public PyUSubType(Python runtime) {
        super(runtime);
    }
}
