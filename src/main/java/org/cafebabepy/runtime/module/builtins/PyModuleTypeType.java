package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyType;

/**
 * Created by yotchang4s on 2017/05/12.
 */
@DefinePyType(name = "builtins.ModuleType", appear = false)
public class PyModuleTypeType extends AbstractCafeBabePyType {

    public PyModuleTypeType(Python runtime) {
        super(runtime);
    }
}
