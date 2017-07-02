package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.Python;
import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

/**
 * Created by yotchang4s on 2017/05/29.
 */
@DefineCafeBabePyType(name = "_ast.expr", parent = "_ast.AST")
public class PyExprTypeType extends AbstractCafeBabePyType {

    public PyExprTypeType(Python runtime) {
        super(runtime);
    }
}