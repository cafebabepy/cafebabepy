package org.cafebabepy.runtime.module.sys;

import org.cafebabepy.runtime.PyObject;
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

    }

    @Override
    public void initialize() {
        super.initialize();

        PyDictObject modules = new PyDictObject(this.runtime, this.runtime.getSysModuleMap());
        getScope().put(this.runtime.str("modules"), modules);

        this.runtime.defineModule(this);

        PyObject version_info = this.runtime.newPyObject("sys.version_info", false);
        getScope().put(this.runtime.str("version_info"), version_info);
    }
}
