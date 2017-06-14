package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefineCafeBabePyFunction;
import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.object.java.JavaPyObject;

import static org.cafebabepy.util.ProtocolNames.__init__;
import static org.cafebabepy.util.ProtocolNames.__new__;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefineCafeBabePyType(name = "builtins.object")
public class PyObjectType extends AbstractCafeBabePyType {

    public PyObjectType(Python runtime) {
        super(runtime);
    }


    @DefineCafeBabePyFunction(name = __new__)
    public final PyObject __new__builtins_object(PyObject cls) {
        return new JavaPyObject(this.runtime, cls);
    }

    @DefineCafeBabePyFunction(name = __init__)
    public final void __init__builtins_object(PyObject self, PyObject... args) {
    }
}
