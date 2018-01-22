package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.util.StringUtils;

import java.util.Optional;

import static org.cafebabepy.util.ProtocolNames.*;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefinePyType(name = "builtins.object")
public final class PyObjectType extends AbstractCafeBabePyType {

    public PyObjectType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject... args) {
    }

    @DefinePyFunction(name = __getattribute__)
    public PyObject __getattribute__(PyObject self, PyObject name) {
        PyObject v = this.runtime.getattr(self, name.toJava(String.class));

        Optional<PyObject> getOpt = this.runtime.getattrOptional(v, __get__);
        if (getOpt.isPresent()) {
            PyObject get = getOpt.get();
            return get.call(v, v.getType());

        } else {
            return v;
        }
    }

    @DefinePyFunction(name = __setattr__)
    public void __setattr__(PyObject self, PyObject name, PyObject value) {
        PyObject strType = this.runtime.typeOrThrow("builtins.str");

        if (!this.runtime.isInstance(self, strType)) {
            throw this.runtime.newRaiseTypeError(
                    "attribute name must be string, not '" + name.getFullName() + "'"
            );
        }

        self.getScope().put(name.toJava(String.class), value);
    }

    @DefinePyFunction(name = __str__)
    public PyObject __str__(PyObject self) {
        if (self.isType()) {
            String str;

            String[] fullName = StringUtils.splitLastDot(self.getFullName());
            if ("builtins".equals(fullName[0])) {
                str = "<class '" + fullName[1] + "'>";

            } else {
                str = "<class '" + fullName[0] + "." + fullName[1] + "'>";
            }

            return this.runtime.str(str);

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

            String str;

            String[] fullName = StringUtils.splitLastDot(self.getFullName());
            if ("builtins".equals(fullName[0])) {
                str = "<" + fullName[1] + " object at 0x" + hashCode + ">";
            } else {
                str = "<" + fullName[0] + "." + fullName[1] + " object at 0x" + hashCode + ">";
            }

            return this.runtime.str(str);
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
}
