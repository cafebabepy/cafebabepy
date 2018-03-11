package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyType;

/**
 * Created by yotchang4s on 2017/05/29.
 */
@DefinePyType(name = "_ast.GtE", parent = {"_ast.cmpop"})
public class PyGtEType extends AbstractCafeBabePyType {

    public PyGtEType(Python runtime) {
        super(runtime);
    }
}
