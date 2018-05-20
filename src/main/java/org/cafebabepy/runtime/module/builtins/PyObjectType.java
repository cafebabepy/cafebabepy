package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.PyObjectScope;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.runtime.object.java.PyDictObject;
import org.cafebabepy.runtime.object.java.PyMappingProxyTypeObject;
import org.cafebabepy.runtime.object.proxy.PyMethodTypeObject;
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
        Optional<PyObject> resultOpt = self.getScope().get(name);
        if (resultOpt.isPresent()) {
            return resultOpt.get();

        } else if (this.runtime.str(__dict__).equals(name)) {
            if (self.existsDict()) {
                PyObject dict;

                if (self.isType()) {
                    dict = new PyMappingProxyTypeObject(this.runtime, self.getScope().getsRaw());

                } else {
                    dict = new PyDictObject(this.runtime, self.getScope().getsRaw());
                }

                self.getScope().put(this.runtime.str(__dict__), dict);

                return dict;
            }
        }

        boolean isParent = false;

        resultOpt = getFromTypes(self, name);
        if (!resultOpt.isPresent()) {
            resultOpt = getFromType(self, name);
            if (!resultOpt.isPresent()) {
                resultOpt = getFromParent(self, name);
                if (!resultOpt.isPresent()) {
                    if (self.isModule()) {
                        throw this.runtime.newRaiseException("builtins.AttributeError",
                                "module '" + self.getName() + "' has no attribute '" + name + "'");

                    } else if (self.isType()) {
                        throw this.runtime.newRaiseException("builtins.AttributeError",
                                "type object '" + self.getName() + "' has no attribute '" + name + "'");

                    } else {
                        throw this.runtime.newRaiseException("builtins.AttributeError",
                                "'" + self.getName() + "' object has no attribute '" + name + "'");
                    }

                } else {
                    // Module
                    isParent = true;
                }
            }
        }

        PyObject result = resultOpt.get();
        if (!result.isCallable() || isParent) {
            return result;

        } else {
            synchronized (this) {
                //if (this.methodMap == null) {
                //    this.methodMap = new LinkedHashMap<>();
                //}

                //PyObject method = this.methodMap.get(name);
                PyObject method = null;
                if (method == null) {
                    //method = newPyObject("builtins.MethodType", result, object);
                    method = new PyMethodTypeObject(this.runtime, self, result);
                    //this.methodMap.put(name, method);
                }

                return method;
            }
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

        self.getScope().put(name, value);
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

    @DefinePyFunction(name = __hash__)
    public PyObject __hash__(PyObject self) {
        return this.runtime.number(System.identityHashCode(self));
    }

    public Optional<PyObject> getFromType(PyObject object, PyObject name) {
        if (object.isType()) {
            Optional<PyObject> typeObject = this.runtime.typeOrThrow("builtins.type").getScope().get(name);
            if (typeObject.isPresent()) {
                return typeObject;
            }
        }

        return Optional.empty();
    }

    public Optional<PyObject> getFromTypes(PyObject object, PyObject name) {
        for (PyObject type : object.getTypes()) {
            Optional<PyObject> typeObject = type.getScope().get(name);
            if (typeObject.isPresent()) {
                return typeObject;
            }
        }

        return Optional.empty();
    }

    public Optional<PyObject> getFromParent(PyObject object, PyObject name) {
        Optional<PyObjectScope> parentOpt = object.getScope().getParent();
        while (parentOpt.isPresent()) {
            PyObjectScope parent = parentOpt.get();
            Optional<PyObject> result = parent.get(name);

            if (result.isPresent()) {
                return result;
            }

            parentOpt = parent.getParent();
        }

        return Optional.empty();
    }
}
