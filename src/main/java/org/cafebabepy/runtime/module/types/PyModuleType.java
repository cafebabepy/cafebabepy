package org.cafebabepy.runtime.module.types;

import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

/**
 * Created by yotchang4s on 2017/05/12.
 */
@DefineCafeBabePyType(name = "types.ModuleType")
public class PyModuleType extends AbstractCafeBabePyType {

    public PyModuleType(Python runtime) {
        super(runtime);
    }

    @Override
    public String getName() {
        return "module";
    }
}
