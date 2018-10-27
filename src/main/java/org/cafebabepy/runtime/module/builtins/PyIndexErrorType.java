package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyType;

/**
 * Created by yotchang4s on 2018/10/28.
 */
@DefinePyType(name = "builtins.IndexError", parent = {"builtins.LookupError"})
public class PyIndexErrorType extends AbstractCafeBabePyType {

    public PyIndexErrorType(Python runtime) {
        super(runtime);
    }
}
