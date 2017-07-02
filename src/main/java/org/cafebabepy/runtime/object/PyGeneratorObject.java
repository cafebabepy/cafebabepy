package org.cafebabepy.runtime.object;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.builtins.PyGeneratorType;

import java.util.Collection;
import java.util.function.Function;

/**
 * Created by yotchang4s on 2017/06/19.
 */
public class PyGeneratorObject extends AbstractPyObjectObject {

    private final static YieldStopper YIELD_STOPPER = new YieldStopper();

    private final Function<YieldStopper, PyObject> iter;

    public PyGeneratorObject(Python runtime, Function<YieldStopper, PyObject> iter) {
        super(runtime, runtime.typeOrThrow("builtins.GeneratorType", false));

        this.iter = iter;
    }

    public final PyObject next() {
        return this.iter.apply(YIELD_STOPPER);
    }

    public final static class YieldStopper {
        public void stop(Python runtime) {
            throw runtime.newRaiseException("builtins.StopIteration");
        }
    }
}
