package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

class AbstractAST extends AbstractCafeBabePyType {

    public AbstractAST(Python runtime) {
        super(runtime);
    }

    @Override
    public void initialize() {
        super.initialize();

        List<PyObject> _fields = Arrays.stream(_fields()).map(this.runtime::str).collect(toList());
        getFrame().putToLocals("_fields", this.runtime.tuple(_fields));
    }

    String[] _fields() {
        return new String[0];
    }
}
