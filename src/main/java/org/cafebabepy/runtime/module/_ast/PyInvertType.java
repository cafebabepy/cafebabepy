package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

/**
 * Created by yotchang4s on 2017/05/29.
 */
@DefineCafeBabePyType(name = "_ast.Invert", parent = {"_ast.unaryop"})
public class PyInvertType extends AbstractCafeBabePyType {

    public PyInvertType(Python runtime) {
        super(runtime);
    }
}
