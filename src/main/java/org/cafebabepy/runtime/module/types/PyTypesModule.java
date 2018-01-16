package org.cafebabepy.runtime.module.types;

import org.cafebabepy.runtime.module.DefinePyModule;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyModule;

/**
 * Created by yotchang4s on 2017/05/12.
 */
@DefinePyModule(name = "types")
public class PyTypesModule extends AbstractCafeBabePyModule {

    public PyTypesModule(Python runtime) {
        super(runtime);
    }
}
