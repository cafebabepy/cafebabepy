package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyType;

/**
 * Created by yotchang4s on 2018/07/15.
 */
@DefinePyType(name = "_ast.Continue", parent = {"_ast.stmt"})
public class PyContinueType extends AbstractCafeBabePyType {

    public PyContinueType(Python runtime) {
        super(runtime);
    }
}
