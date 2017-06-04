package org.cafebabepy.runtime.module.types;

import org.cafebabepy.annotation.DefineCafeBabePyModule;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

/**
 * Created by yotchang4s on 2017/05/12.
 */
@DefineCafeBabePyModule(name = "types")
public class PyTypesModule extends AbstractCafeBabePyType {

    public PyTypesModule(Python runtime) {
        super(runtime);
    }
}
