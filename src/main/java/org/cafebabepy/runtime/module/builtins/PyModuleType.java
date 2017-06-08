package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

import java.util.Optional;

/**
 * Created by yotchang4s on 2017/05/12.
 */
@DefineCafeBabePyType(name = "builtins.ModuleType")
public class PyModuleType extends AbstractCafeBabePyType {

    public PyModuleType(Python runtime) {
        super(runtime);
    }

    @Override
    public Optional<String> getModuleName() {
        return Optional.of("builtins");
    }

    @Override
    public String getName() {
        return "module";
    }
}
