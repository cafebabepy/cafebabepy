package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

/**
 * Created by yotchang4s on 2017/06/07.
 */
@DefineCafeBabePyType(name = "builtins.bool", parent = {"builtins.int"})
public class PyBoolType extends AbstractCafeBabePyType {

    public PyBoolType(Python runtime) {
        super(runtime);
    }
}
