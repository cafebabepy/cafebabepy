package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyType;

/**
 * Created by yotchang4s on 2018/10/24.
 */
@DefinePyType(name = "builtins.frame", appear = false)
public class PyFrameType extends AbstractCafeBabePyType {

    // FIXME stub
    public PyFrameType(Python runtime) {
        super(runtime);
    }
}
