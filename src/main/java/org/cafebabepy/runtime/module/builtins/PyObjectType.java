package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.runtime.object.java.PyDictObject;
import org.cafebabepy.runtime.object.java.PyMappingProxyTypeObject;

import java.util.Map;

import static org.cafebabepy.util.ProtocolNames.*;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefinePyType(name = "builtins.object")
public final class PyObjectType extends AbstractCafeBabePyType {

    public PyObjectType(Python runtime) {
        super(runtime);
    }

    @Override
    public void initialize() {
        super.initialize();

        if (existsDict()) {
            PyObject dict;

            if (isType()) {
                dict = new PyMappingProxyTypeObject(this.runtime, getScope().getsRaw());

            } else {
                dict = new PyDictObject(this.runtime, getScope().getsRaw());
            }

            getScope().put(this.runtime.str(__dict__), dict);
        }
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject[] args, Map<String, PyObject> kwargs) {
    }

    @DefinePyFunction(name = __getattribute__)
    public PyObject __getattribute__(PyObject self, PyObject key) {
        return this.runtime.builtins_object__getattribute__(self, key).orElseThrow(() ->
                this.runtime.newRaiseException("builtins.AttributeError",
                        "'" + self.getName() + "' object has no attribute " + key)
        );
    }

    @DefinePyFunction(name = __setattr__)
    public void __setattr__(PyObject self, PyObject name, PyObject value) {
        PyObject strType = this.runtime.typeOrThrow("builtins.str");

        if (!this.runtime.isInstance(self, strType)) {
            throw this.runtime.newRaiseTypeError(
                    "attribute name must be string, not '" + name.getFullName() + "'"
            );
        }

        self.getScope().put(name, value);
    }

    @DefinePyFunction(name = __str__)
    public PyObject __str__(PyObject self) {
        if (self.isType()) {
            return this.runtime.str("<class '" + self.getFullName() + "'>");

        } else if (self.isModule()) {
            String str;

            String moduleName = self.getFullName();
            if ("builtins".equals(moduleName)) {
                str = "<module '" + moduleName + "' (built-in)>";
            } else {
                // FIXME fromをどうする？
                str = "<module '" + moduleName + "' from '" + "???" + "'>";
            }

            return this.runtime.str(str);
        } else {
            String hashCode = Integer.toHexString(System.identityHashCode(self));

            return this.runtime.str("<" + self.getFullName() + " object at 0x" + hashCode + ">");
        }
    }

    @DefinePyFunction(name = __eq__)
    public PyObject __eq__(PyObject self, PyObject other) {
        if (self == other) {
            return this.runtime.True();

        } else {
            return this.runtime.NotImplemented();
        }
    }

    @DefinePyFunction(name = __ne__)
    public PyObject __ne__(PyObject self, PyObject other) {
        return this.runtime.NotImplemented();
    }

    @DefinePyFunction(name = __hash__)
    public PyObject __hash__(PyObject self) {
        return this.runtime.number(System.identityHashCode(self));
    }
}
