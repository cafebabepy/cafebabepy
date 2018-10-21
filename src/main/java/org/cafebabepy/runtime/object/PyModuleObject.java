package org.cafebabepy.runtime.object;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;

/**
 * Created by yotchang4s on 2017/06/04.
 */
public class PyModuleObject extends AbstractPyObjectObject {

    private final String name;

    public PyModuleObject(Python runtime, String name) {
        super(runtime);

        this.name = name;
    }

    @Override
    public boolean isModule() {
        return true;
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("builtins.ModuleType", false);
    }

    @Override
    public String getName() {
        return this.name;
    }
}
