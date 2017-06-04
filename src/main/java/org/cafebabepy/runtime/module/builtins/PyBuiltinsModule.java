package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefineCafeBabePyModule;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyModule;

/**
 * Created by yotchang4s on 2017/05/12.
 */
@DefineCafeBabePyModule(name = "builtins")
public class PyBuiltinsModule extends AbstractCafeBabePyModule {
    public PyBuiltinsModule(Python runtime) {
        super(runtime);
    }
}
