package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefineCafeBabePyType(name = "builtins.object", parent = {})
public class PyObjectType extends AbstractCafeBabePyType {

    public PyObjectType(Python runtime) {
        super(runtime);
    }
}
