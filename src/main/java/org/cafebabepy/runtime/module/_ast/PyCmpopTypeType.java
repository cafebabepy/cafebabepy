package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

/**
 * Created by yotchang4s on 2017/05/29.
 */
@DefinePyType(name = "_ast.cmpop", parent = "_ast.AST")
public class PyCmpopTypeType extends AbstractCafeBabePyType {

    public PyCmpopTypeType(Python runtime) {
        super(runtime);
    }
}
