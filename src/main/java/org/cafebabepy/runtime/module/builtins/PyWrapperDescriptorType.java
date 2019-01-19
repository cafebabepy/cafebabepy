package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.runtime.object.java.PyJavaFunctionObject;
import org.cafebabepy.runtime.object.java.PyMethodWrapperObject;

import java.util.LinkedHashMap;

import static org.cafebabepy.util.ProtocolNames.__get__;
import static org.cafebabepy.util.ProtocolNames.__str__;

/**
 * Created by yotchang4s on 2018/06/24.
 */
@DefinePyType(name = "builtins.wrapper_descriptor", appear = false)
public class PyWrapperDescriptorType extends AbstractCafeBabePyType {

    public PyWrapperDescriptorType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __get__)
    public PyObject __get__(PyObject self, /* FIXME default argument */ PyObject... args) {
        if (!this.runtime.isInstance(self, "builtins.wrapper_descriptor", false)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__get__' requires a 'wrapper_descriptor' object but received a '" + self.getFullName() + "'");
        }

        if (args[0].isNone() && !this.runtime.isSubClass(args[1], "builtins.NoneType", false)) {
            return self;
        }

        return new PyMethodWrapperObject(this.runtime, self, args[0]);
    }

    @Override
    public PyObject call(PyObject[] args, LinkedHashMap<String, PyObject> keywords) {
        // FIXME ???
        if (args.length == 0) {
            throw this.runtime.newRaiseTypeError("descriptor '???' of 'object' object needs an argument");
        }

        return super.call(args, keywords);
    }

    @DefinePyFunction(name = __str__)
    public PyObject __str__(PyObject self) {
        if (!this.equals(self.getType())) {
            return this.runtime.str(self);
        }

        if (!(self instanceof PyJavaFunctionObject)) {
            throw new CafeBabePyException(self + " is not PyJavaFunctionObject");
        }

        PyJavaFunctionObject object = (PyJavaFunctionObject) self;

        return this.runtime.str("<slot wrapper '" + self.getName() + "' of '" + object.getTarget().getFullName() + "' objects>");
    }
}
