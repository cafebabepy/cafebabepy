package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

/**
 * Created by yotchang4s on 2017/05/29.
 */
@DefinePyType(name = "_ast.operator", parent = {"_ast.AST"})
public class PyOperatorType extends AbstractCafeBabePyType {

    public PyOperatorType(Python runtime) {
        super(runtime);
    }
}
