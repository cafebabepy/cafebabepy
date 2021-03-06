package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;

import static org.cafebabepy.util.ProtocolNames.__init__;

/**
 * Created by yotchang4s on 2018/10/24.
 */
@DefinePyType(name = "builtins.traceback", appear = false)
public class PyTracebackType extends AbstractCafeBabePyType {

    public PyTracebackType(Python runtime) {
        super(runtime);
    }
}
