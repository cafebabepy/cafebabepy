package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefineCafeBabePyFunction;
import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

import static org.cafebabepy.util.ProtocolNames.__bool__;

/**
 * Created by yotchang4s on 2017/06/07.
 */
@DefineCafeBabePyType(name = "builtins.bool", parent = {"builtins.int"})
public class PyBoolType extends AbstractCafeBabePyType {

    public PyBoolType(Python runtime) {
        super(runtime);
    }

    @DefineCafeBabePyFunction(name = __bool__)
    public PyObject __bool__(PyObject self) {
        if(self.isFalse()) {
            return this.runtime.False();

        } else {
            return this.runtime.True();
        }
    }
}
