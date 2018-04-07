package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyType;

/**
 * Created by yotchang4s on 2018/04/07.
 */
@DefinePyType(name = "builtins.KeyError", parent = {"builtins.LookupError"})
public class PyKeyErrorType extends AbstractCafeBabePyType {

    public PyKeyErrorType(Python runtime) {
        super(runtime);
    }
}
