package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefineCafeBabePyFunction;
import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.object.PyStrObject;

import static org.cafebabepy.util.ProtocolNames.__str__;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefineCafeBabePyType(name = "builtins.str")
public class PyStrType extends AbstractCafeBabePyType {

    public PyStrType(Python runtime) {
        super(runtime);
    }

    @DefineCafeBabePyFunction(name = __str__)
    public PyObject __str__(PyObject self) {
        PyObject strType = this.runtime.typeOrThrow("builtins.str");

        PyObject builtins = this.runtime.getBuiltinsModule();
        PyObject isinstance = builtins.getObjectOrThrow("isinstance");
        if (isinstance.callSelf(builtins, self, strType).isFalse()) {
            throw this.runtime.newRaiseTypeError("descriptor '__str__' requires a 'str' object but received a '" + self.getFullName() + "'");
        }

        return self;
    }

    public static PyObject newStr(Python runtime, String value) {
        PyObject result = new PyStrObject(runtime, value);
        result.preInitialize();
        result.postInitialize();

        return result;
    }
}
