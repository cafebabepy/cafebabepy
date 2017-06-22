package org.cafebabepy.runtime.object;

import org.cafebabepy.runtime.Python;

/**
 * Created by yotchang4s on 2017/06/19.
 */
public class PyStrObject extends AbstractPyObjectObject {

    private final String value;

    public PyStrObject(Python runtime, String value) {
        super(runtime, runtime.typeOrThrow("builtins.str"));

        this.value = value;
    }

    @Override
    public String asJavaString() {
        return this.value;
    }
}
