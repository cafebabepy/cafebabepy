package org.cafebabepy.runtime.module;

import org.cafebabepy.annotation.DefineCafeBabePyModule;
import org.cafebabepy.runtime.Python;

/**
 * Created by yotchang4s on 2017/06/04.
 */
@DefineCafeBabePyModule(name = Python.MAIN_MODULE_NAME)
public class PyMainModule extends AbstractCafeBabePyModule {

    public PyMainModule(Python runtime) {
        super(runtime);
    }
}
