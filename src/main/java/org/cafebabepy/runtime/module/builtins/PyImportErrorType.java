package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyType;

/**
 * Created by yotchang4s on 2017/05/31.
 */
@DefinePyType(name = "builtins.ImportError", parent = {"builtins.Exception"})
public class PyImportErrorType extends AbstractCafeBabePyType {

    public PyImportErrorType(Python runtime) {
        super(runtime);
    }
}
