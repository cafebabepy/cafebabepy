package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

/**
 * Created by yotchang4s on 2017/05/13.
 */
// TODO builtins????
@DefineCafeBabePyType(name = "builtins.NotImplementedType", appear = false)
public class PyNotImplementedTypeType extends AbstractCafeBabePyType {

    public PyNotImplementedTypeType(Python runtime) {
        super(runtime);
    }
}
