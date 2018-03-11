package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyType;

/**
 * Created by yotchang4s on 2017/05/31.
 */
@DefinePyType(name = "builtins.StopIteration", parent = {"builtins.Exception"})
public class PyStopIterationType extends AbstractCafeBabePyType {

    public PyStopIterationType(Python runtime) {
        super(runtime);
    }
}
