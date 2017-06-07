package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

/**
 * Created by yotchang4s on 2017/05/31.
 */
@DefineCafeBabePyType(name = "builtins.StopIteration", parent = {"builtins.Exception"})
public class PyStopIterationType extends AbstractCafeBabePyType {

    public PyStopIterationType(Python runtime) {
        super(runtime);
    }
}
