package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

/**
 * Created by yotchang4s on 2017/06/03.
 */
@DefineCafeBabePyType(name = "builtins.list")
public class PyListType extends AbstractCafeBabePyType {

    public PyListType(Python runtime) {
        super(runtime);
    }
}
