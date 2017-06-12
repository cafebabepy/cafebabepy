package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefineCafeBabePyFunction;
import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

import java.util.Optional;
import java.util.function.Function;

import static org.cafebabepy.util.ProtocolNames.__next__;

/**
 * Created by yotchang4s on 2017/06/07.
 */
@DefineCafeBabePyType(name = "builtins.GeneratorType")
public class PyGeneratorType extends AbstractCafeBabePyType {

    private Function<YieldStopper, PyObject> iter;

    private YieldStopper yieldStopper;

    public PyGeneratorType(Python runtime) {
        super(runtime);

        this.yieldStopper = new YieldStopper();
    }

    @Override
    public Optional<String> getModuleName() {
        return Optional.of("builtins");
    }

    @Override
    public String getName() {
        return "generator";
    }

    @DefineCafeBabePyFunction(name = __next__)
    public PyObject __next__(PyObject self) {
        return iter.apply(this.yieldStopper);
    }

    public final class YieldStopper {
        public void stop() {
            throw getRuntime().newRaiseException("builtins.StopIteration");
        }
    }

    public static PyGeneratorType newGenerator(Python runtime, Function<YieldStopper, PyObject> iter) {
        PyGeneratorType generatorType = new PyGeneratorType(runtime);
        generatorType.preInitialize();
        generatorType.postInitialize();

        generatorType.iter = iter;

        return generatorType;
    }
}
