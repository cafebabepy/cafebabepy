package org.cafebabepy.runtime.module._weakref;

import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyModule;
import org.cafebabepy.runtime.module.DefinePyModule;

/**
 * Created by yotchang4s on 2018/10/28.
 */
@DefinePyModule(name = "_weakref")
public class PyWeakRefModule extends AbstractCafeBabePyModule {

    // FIXME stub
    public PyWeakRefModule(Python runtime) {
        super(runtime);
    }

    @Override
    public void initialize() {
        getFrame().getLocals().put("ref", this.runtime.typeOrThrow("weakref"));
    }
}