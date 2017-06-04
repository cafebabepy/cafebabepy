package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

/**
 * Created by yotchang4s on 2017/05/31.
 */
@DefineCafeBabePyType(name = "builtins.ModuleNotFoundError", parent = {"builtins.ImportError"})
public class PyModuleNotFoundErrorType extends AbstractCafeBabePyType {

    public PyModuleNotFoundErrorType(Python runtime) {
        super(runtime);
    }
}
