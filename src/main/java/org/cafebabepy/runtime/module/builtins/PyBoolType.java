package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefineCafeBabePyFunction;
import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

import java.util.Arrays;
import java.util.Collections;

import static org.cafebabepy.util.ProtocolNames.__init__;

/**
 * Created by yotchang4s on 2017/06/07.
 */
@DefineCafeBabePyType(name = "builtins.bool", parent = {"builtins.int"})
public class PyBoolType extends AbstractCafeBabePyType {

    public static final String JAVA_BOOLEAN_NAME = "boolean";

    public PyBoolType(Python runtime) {
        super(runtime);
    }

    // FIXME 引数無しにも対応する
    @DefineCafeBabePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject... args) {
        // FIXME superに対応する
        boolean bool = true;
        if (args.length == 0) {
            bool = false;

        } else {
            PyObject x = args[0];
            if (x == this.runtime.None()) {
                bool = false;
            }
            // FIXME 他にもパターンがある。Pythonのドキュメント参照。
        }

        self.putJavaObject(JAVA_BOOLEAN_NAME, bool);
    }

    public PyObject __bool__(PyObject self) {
        Boolean bool = self.getJavaObject(JAVA_BOOLEAN_NAME).map(b -> (Boolean) b).orElse(Boolean.TRUE);
        if (bool.booleanValue()) {
            return this.runtime.True();

        } else {
            return this.runtime.False();
        }
    }

    public static PyObject newBool(Python runtime, boolean bool) {
        PyObject type = runtime.getBuiltinsModule().getObjectOrThrow("bool");
        if (!(type instanceof PyBoolType)) {
            // FIXME To CPython message
            throw runtime.newRaiseException(
                    "builtins.TypeError", "'" + type.getName() + "' is not bool");
        }

        PyObject object = PyObject.callStatic(type);
        object.putJavaObject(JAVA_BOOLEAN_NAME, bool);

        return object;
    }
}
