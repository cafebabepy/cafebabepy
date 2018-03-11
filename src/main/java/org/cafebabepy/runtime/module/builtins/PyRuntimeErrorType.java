package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyType;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefinePyType(name = "builtins.RuntimeError", parent = {"builtins.Exception"})
public class PyRuntimeErrorType extends AbstractCafeBabePyType {

    public PyRuntimeErrorType(Python runtime) {
        super(runtime);
    }
}
