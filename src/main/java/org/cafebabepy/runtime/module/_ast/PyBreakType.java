package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyType;

/**
 * Created by yotchang4s on 2018/07/15.
 */
@DefinePyType(name = "_ast.Break", parent = {"_ast.stmt"})
public class PyBreakType extends AbstractCafeBabePyType {

    public PyBreakType(Python runtime) {
        super(runtime);
    }
}
