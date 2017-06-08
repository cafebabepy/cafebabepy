package org.cafebabepy.runtime.object;

import org.cafebabepy.annotation.DefineCafeBabePyFunction;
import org.cafebabepy.runtime.AbstractPyObject;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.cafebabepy.util.ProtocolNames.__next__;

/**
 * Created by yotchang4s on 2017/06/08.
 */
public final class PyGeneratorObject extends AbstractPyObject implements PyObject {

    private final PyObject object;

    private final Function<YieldStopper, PyObject> iter;

    private final YieldStopper stopper;

    private PyGeneratorObject(Python runtime, PyObject object, Function<YieldStopper, PyObject> iter) {
        super(runtime);

        this.object = object;
        this.iter = iter;
        this.stopper = new YieldStopper();

    }

    @DefineCafeBabePyFunction(name = __next__)
    public PyObject __next__() {
        return iter.apply(this.stopper);
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("types.GeneratorType");
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public List<PyObject> getSuperTypes() {
        return null;
    }

    @Override
    public Optional<String> getModuleName() {
        return null;
    }

    @Override
    public String asJavaString() {
        return null;
    }

    @Override
    public PyObject call(PyObject... args) {
        return null;
    }

    public final class YieldStopper {
        public void stop() {
            getRuntime().newRaiseException("builtins.StopIteration");
        }
    }

    public static PyGeneratorObject generator(Python runtime, PyObject object, Function<YieldStopper, PyObject> iter) {
        return new PyGeneratorObject(runtime, object, iter);
    }
}
