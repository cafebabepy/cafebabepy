package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyType;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefinePyType(name = "builtins.IndentationError", parent = {"builtins.SyntaxError"})
public class PyIndentationErrorType extends AbstractCafeBabePyType {

    public PyIndentationErrorType(Python runtime) {
        super(runtime);
    }
}
