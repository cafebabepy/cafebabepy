package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefinePyType;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

/**
 * Created by yotchang4s on 2017/10/01.
 */
@DefinePyType(name = "builtins.NotImplementedError", parent = {"builtins.RuntimeError"})
public class PyNotImplementedError extends AbstractCafeBabePyType {

    public PyNotImplementedError(Python runtime) {
        super(runtime);
    }
}
