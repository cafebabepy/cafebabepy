package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.annotation.DefineCafeBabePyFunction;
import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

import static org.cafebabepy.util.ProtocolNames.__init__;

/**
 * Created by yotchang4s on 2017/08/09.
 */
@DefineCafeBabePyType(name = "_ast.Pass", parent = {"_ast.stmt"})
public class PyPassType extends AbstractCafeBabePyType {

    public PyPassType(Python runtime) {
        super(runtime);
    }
}
