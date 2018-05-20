package org.cafebabepy.runtime.module.sys;

import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyModule;
import org.cafebabepy.runtime.module.DefinePyModule;
import org.cafebabepy.runtime.object.java.PyDictObject;

/**
 * Created by yotchang4s on 2017/06/03.
 */
@DefinePyModule(name = "sys")
public class PySysModule extends AbstractCafeBabePyModule {

    public PySysModule(Python runtime) {
        super(runtime);
    }

    protected void defineModule() {
        PyDictObject modules = new PyDictObject(this.runtime, this.runtime.getSysModuleMap());
        getScope().put(this.runtime.str("modules"), modules);

        this.runtime.defineModule(this);
    }
}
