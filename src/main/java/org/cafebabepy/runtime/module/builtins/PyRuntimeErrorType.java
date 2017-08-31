package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefinePyType;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefinePyType(name = "builtins.RuntimeError", parent = {"builtins.Exception"})
public class PyRuntimeErrorType extends AbstractCafeBabePyType {

    public PyRuntimeErrorType(Python runtime) {
        super(runtime);
    }
}
