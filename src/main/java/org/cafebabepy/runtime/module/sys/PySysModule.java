package org.cafebabepy.runtime.module.sys;

import org.cafebabepy.annotation.DefineCafeBabePyModule;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyModule;

/**
 * Created by yotchang4s on 2017/06/03.
 */
@DefineCafeBabePyModule(name = "sys")
public class PySysModule extends AbstractCafeBabePyModule {

    public PySysModule(Python runtime) {
        super(runtime);
    }
}
