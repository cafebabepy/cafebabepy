package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefineCafeBabePyFunction;
import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.object.PyGeneratorObject;

import java.util.Optional;

import static org.cafebabepy.util.ProtocolNames.__iter__;
import static org.cafebabepy.util.ProtocolNames.__next__;

/**
 * Created by yotchang4s on 2017/06/07.
 */
@DefineCafeBabePyType(name = "builtins.generator", appear = false)
public class PyGeneratorType extends AbstractCafeBabePyType {

    public PyGeneratorType(Python runtime) {
        super(runtime);
    }

    @Override
    public Optional<String> getModuleName() {
        return Optional.of("builtins");
    }

    @DefineCafeBabePyFunction(name = __iter__)
    public PyObject __iter__(PyObject self) {
        if (!(self instanceof PyGeneratorObject)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__iter__' requires a 'generator' object but received a '"
                            + self.getType().getFullName()
                            + "'");
        }

        return this;
    }

    @DefineCafeBabePyFunction(name = __next__)
    public PyObject __next__(PyObject self) {
        if (!(self instanceof PyGeneratorObject)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__next__' requires a 'generator' object but received a '"
                            + self.getType().getFullName()
                            + "'");
        }

        return ((PyGeneratorObject) self).next();
    }
}
