package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefineCafeBabePyFunction;
import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

import java.util.List;

import static org.cafebabepy.util.ProtocolNames.__getattribute__;
import static org.cafebabepy.util.ProtocolNames.__init__;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefineCafeBabePyType(name = "builtins.super")
public class PySuperType extends AbstractCafeBabePyType {

    public PySuperType(Python runtime) {
        super(runtime);
    }

    @DefineCafeBabePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject... args) {
        if (args.length > 1) {
            throw this.runtime.newRaiseTypeError(
                    "super() takes at most 2 arguments (" + (args.length + 1) + " given)");
        }
        /*
        if (!self.isType()) {
            throw this.runtime.newRaiseTypeError(
                    "super() argument 1 must be type, not " + self.getType());
        }
        */

        if (args.length == 1) {
            self.getScope().put("_proxy_object", args[0]);
        }
    }

    @DefineCafeBabePyFunction(name = __getattribute__)
    public PyObject __getattribute__(PyObject self, PyObject name) {
        PyObject strType = typeOrThrow("builtins.str");

        if (!this.runtime.isInstance(name, strType)) {
            throw this.runtime.newRaiseTypeError(
                    "attribute name must be string, not '" + name.getFullName() + "'"
            );
        }

        PyObject object = self.getScope().get("_proxy_object").orElseThrow(() ->
                getRuntime().newRaiseException("builtins.AttributeError",
                        "'" + self.getFullName() + "' object has no attribute '_proxy_object'"));

        PyObject selfProxy;
        List<PyObject> types = object.getTypes();
        if (types.size() == 1) {
            selfProxy = types.get(0);

        } else {
            selfProxy = types.get(1);
        }

        return selfProxy.getObjectOrThrow(name.asJavaString());
    }
}
