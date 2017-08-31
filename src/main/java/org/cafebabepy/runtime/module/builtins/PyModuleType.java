package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefinePyType;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

/**
 * Created by yotchang4s on 2017/05/12.
 */
@DefinePyType(name = "builtins.module", appear = false)
public class PyModuleType extends AbstractCafeBabePyType {

    public PyModuleType(Python runtime) {
        super(runtime);
    }
}
