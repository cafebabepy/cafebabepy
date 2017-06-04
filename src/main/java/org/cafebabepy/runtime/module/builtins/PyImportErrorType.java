package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

/**
 * Created by yotchang4s on 2017/05/31.
 */
@DefineCafeBabePyType(name = "builtins.ImportError", parent = {"builtins.Exception"})
public class PyImportErrorType extends AbstractCafeBabePyType {

    public PyImportErrorType(Python runtime) {
        super(runtime);
    }
}
