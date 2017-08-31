package org.cafebabepy.runtime.module;

import org.cafebabepy.annotation.DefinePyModule;
import org.cafebabepy.runtime.Python;

import static org.cafebabepy.util.ProtocolNames.__main__;

/**
 * Created by yotchang4s on 2017/06/04.
 */
@DefinePyModule(name = __main__)
public class PyMainModule extends AbstractCafeBabePyModule {

    public PyMainModule(Python runtime) {
        super(runtime);
    }
}
