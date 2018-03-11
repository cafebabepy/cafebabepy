package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyType;

/**
 * Created by yotchang4s on 2017/08/09.
 */
@DefinePyType(name = "_ast.Pass", parent = {"_ast.stmt"})
public class PyPassType extends AbstractCafeBabePyType {

    public PyPassType(Python runtime) {
        super(runtime);
    }
}
