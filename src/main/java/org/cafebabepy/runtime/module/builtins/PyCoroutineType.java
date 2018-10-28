package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;

/**
 * Created by yotchang4s on 2018/10/24.
 */
@DefinePyType(name = "builtins.coroutine", appear = false)
public class PyCoroutineType extends AbstractCafeBabePyType {

    // FIXME stub
    public PyCoroutineType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = "close")
    public void close(PyObject self) {
    }
}
