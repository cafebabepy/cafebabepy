package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.runtime.object.java.PyDictObject;
import org.cafebabepy.runtime.object.java.PyMappingProxyTypeObject;

import java.util.Map;
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

    @Override
    public void initialize() {
        super.initialize();

        if (existsDict()) {
            PyObject dict;

            if (isType()) {
                dict = new PyMappingProxyTypeObject(this.runtime, getFrame().getLocalsPyObjectMap());

            } else {
                dict = new PyDictObject(this.runtime, getFrame().getLocalsPyObjectMap());
            }

            getFrame().getLocals().put(__dict__, dict);
        }

        getFrame().getLocals().put(__name__, this.runtime.str(getName()));
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject[] args, Map<String, PyObject> kwargs) {
    }

    @DefinePyFunction(name = __getattribute__)
    public PyObject __getattribute__(PyObject self, PyObject key) {
        String javaKey = key.toJava(String.class);

        return this.runtime.builtins_object__getattribute__(self, javaKey).orElseGet(() -> {
            PyObject specialVar = null;
            if (__name__.equals(key.toJava(String.class))) {
                specialVar = self.getFrame().getNotAppearLocals().get(javaKey);
            }

            if (specialVar != null) {
                return specialVar;
            }

            throw this.runtime.newRaiseException("AttributeError",
                    "'" + self.getName() + "' object has no attribute " + key);
        });
    }

    @DefinePyFunction(name = __setattr__)
    public void __setattr__(PyObject self, PyObject name, PyObject value) {
        PyObject strType = this.runtime.typeOrThrow("builtins.str");

        if (!this.runtime.isInstance(name, strType)) {
            throw this.runtime.newRaiseTypeError(
                    "attribute name must be string, not '" + name.getFullName() + "'"
            );
        }

        String javaName = name.toJava(String.class);

        PyObject existsValue = lookup(self, javaName);
        if (existsValue != null) {
            Optional<PyObject> setOpt = this.runtime.getattrOptional(existsValue, __set__);
            if (setOpt.isPresent()) {
                setOpt.get().call(self, value);
                return;
            }
        }

        self.getFrame().getLocals().put(javaName, value);
    }

    @DefinePyFunction(name = __delattr__)
    public void __delattr__(PyObject self, PyObject name) {
        PyObject strType = this.runtime.typeOrThrow("builtins.str");

        if (!this.runtime.isInstance(name, strType)) {
            throw this.runtime.newRaiseTypeError(
                    "attribute name must be string, not '" + name.getFullName() + "'"
            );
        }

        String javaName = name.toJava(String.class);

        PyObject existsValue = lookup(self, javaName);
        if (existsValue != null) {
            Optional<PyObject> deleteOpt = this.runtime.getattrOptional(existsValue, __delete__);
            if (deleteOpt.isPresent()) {
                deleteOpt.get().call(self);
                return;
            }
        }

        self.getFrame().getLocals().remove(javaName);
    }

    private PyObject lookup(PyObject object, String name) {
        PyObject attr = object.getFrame().lookup(name);
        if (attr != null) {
            return attr;
        }

        return lookupType(object, name);
    }

    private PyObject lookupType(PyObject object, String name) {
        for (PyObject type : object.getTypes()) {
            PyObject typeObject = type.getFrame().lookup(name);
            if (typeObject != null) {
                return typeObject;
            }
        }

        return null;
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

    @DefinePyFunction(name = __repr__)
    public PyObject __repr__(PyObject self) {
        if (this == self) {
            return this.runtime.getattr(this, __repr__).call();
        }

        return this.runtime.getattr(self, __repr__).call();
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
