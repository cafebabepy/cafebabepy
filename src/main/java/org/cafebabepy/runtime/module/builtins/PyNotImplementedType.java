package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefinePyType;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

/**
 * Created by yotchang4s on 2017/05/13.
 */
// TODO builtins????
@DefinePyType(name = "builtins.NotImplemented", appear = false)
public class PyNotImplementedType extends AbstractCafeBabePyType {

    public PyNotImplementedType(Python runtime) {
        super(runtime);
    }
}
