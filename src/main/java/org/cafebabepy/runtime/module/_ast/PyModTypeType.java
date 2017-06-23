package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

/**
 * Created by yotchang4s on 2017/06/04.
 */
@DefineCafeBabePyType(name = "_ast.mod", parent = {"_ast.AST"})
public class PyModTypeType extends AbstractCafeBabePyType {

    public PyModTypeType(Python runtime) {
        super(runtime);
    }
}
