package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefineCafeBabePyType(name = "builtins.ValueError", parent = {"builtins.Exception"})
public class PyValueErrorType extends AbstractCafeBabePyType {

    public PyValueErrorType(Python runtime) {
        super(runtime);
    }
}
