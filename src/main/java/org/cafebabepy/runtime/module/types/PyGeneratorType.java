package org.cafebabepy.runtime.module.types;

import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

import java.util.Optional;

/**
 * Created by yotchang4s on 2017/06/07.
 */
@DefineCafeBabePyType(name = "types.GeneratorType")
public class PyGeneratorType extends AbstractCafeBabePyType {
    public PyGeneratorType(Python runtime) {
        super(runtime);
    }

    @Override
    public Optional<String> getModuleName() {
        return Optional.of("builtins");
    }

    @Override
    public String getName() {
        return "generator";
    }
}
