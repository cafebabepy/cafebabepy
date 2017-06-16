package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefineCafeBabePyFunction;
import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

import java.util.Optional;
import java.util.function.Function;

import static org.cafebabepy.util.ProtocolNames.__next__;

/**
 * Created by yotchang4s on 2017/06/07.
 */
@DefineCafeBabePyType(name = "builtins.GeneratorType", appear = false)
public class PyGeneratorType extends AbstractCafeBabePyType {

    private static final String JAVA_ITER_NAME = "iter";

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
        Function<YieldStopper, PyObject> f = self.getJavaObject(JAVA_ITER_NAME)
                .map(o -> (Function<YieldStopper, PyObject>) o)
                .orElseThrow(() ->
                        new CafeBabePyException("'" + JAVA_ITER_NAME + "' is not found")
                );

        return f.apply(this.yieldStopper);
    }

    public final class YieldStopper {
        public void stop() {
            throw getRuntime().newRaiseException("builtins.StopIteration");
        }
    }

    public static PyObject newGenerator(Python runtime, Function<YieldStopper, PyObject> iter) {
        PyObject object = runtime.newPyObject("builtins.GeneratorType", false);

        object.putJavaObject(JAVA_ITER_NAME, iter);

        return object;
    }
}
