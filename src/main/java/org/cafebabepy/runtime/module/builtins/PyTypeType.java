package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.evaluter.Interpret.PyInterpretClassObject;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.runtime.object.PyObjectObject;
import org.cafebabepy.runtime.object.java.PyListObject;
import org.cafebabepy.runtime.object.java.PySetObject;
import org.cafebabepy.runtime.object.java.PyTupleObject;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.cafebabepy.util.ProtocolNames.*;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefinePyType(name = "builtins.type")
public final class PyTypeType extends AbstractCafeBabePyType {

    public PyTypeType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __call__)
    public PyObject __call__(PyObject[] args, LinkedHashMap<String, PyObject> kwargs) {
        PyObject self = args[0];

        if (!self.isType()) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__call__' requires a 'type' object but received a '" + self.getFullName() + "'");
        }

        if (this.runtime.isSubClass(self, "type")) { // class new
            if (self == this) {
                PyObject[] newArgs = new PyObject[args.length + 1];
                newArgs[0] = self;
                System.arraycopy(args, 0, newArgs, 1, args.length);

                return this.runtime.getattr(self, __new__).call(newArgs, kwargs);

            } else {
                // metaclass
                return this.runtime.getattr(self, __new__).call(args, kwargs);
            }

        } else { // object new
            PyObject object = this.runtime.getattr(self, __new__).call(self);

            PyObject[] newArgs = new PyObject[args.length - 1];
            System.arraycopy(args, 1, newArgs, 0, newArgs.length);
            this.runtime.getattr(object, __init__).call(newArgs, kwargs);

            return object;
        }
    }

    @DefinePyFunction(name = __prepare__)
    public PyObject __prepare__(PyObject self, PyObject[] args, LinkedHashMap<String, PyObject> kwargs) {
        return this.runtime.dict();
    }

    @DefinePyFunction(name = __new__)
    public PyObject __new__(PyObject self, PyObject cls, PyObject[] args, LinkedHashMap<String, PyObject> kwargs) {
        if (!cls.isType()) {
            throw this.runtime.newRaiseTypeError(
                    self.getFullName() + ".__new__(X): X is not a type object (" + cls.getFullName() + ")");
        }
        if (!this.runtime.isSubClass(cls, self)) {
            throw this.runtime.newRaiseTypeError(
                    self.getFullName() + ".__new__(" + cls.getFullName() + "): object is not a subtype of " + self.getFullName());
        }

        if (!this.runtime.isSubClass(cls, "type")) {
            PyObject instance;

            if (self.getClass() == PyTupleType.class) {
                instance = new PyTupleObject(this.runtime);

            } else if (self.getClass() == PyListType.class) {
                instance = new PyListObject(this.runtime);

            } else if (self.getClass() == PySetType.class) {
                instance = new PySetObject(this.runtime);

            } else {
                instance = new PyObjectObject(this.runtime, self);
            }

            return instance;
        }

        if (args.length == 1 && kwargs.isEmpty()) {
            return args[0].getType();

        } else {
            int argsValidCount = args.length;
            int argsInvalidCount = kwargs.size();

            String name = null;
            List<PyObject> bases = null;
            Map<PyObject, PyObject> dict = Collections.emptyMap();

            if (kwargs.containsKey("name")) {
                name = kwargs.get("name").toJava(String.class);
                argsValidCount++;
                argsInvalidCount--;
            }
            if (kwargs.containsKey("bases")) {
                bases = kwargs.get("bases").toJava(List.class);
                argsValidCount++;
                argsInvalidCount--;
            }
            if (kwargs.containsKey("dict")) {
                dict = kwargs.get("dict").toJava(Map.class);
                argsValidCount++;
                argsInvalidCount--;
            }

            if (argsValidCount == 3 && argsInvalidCount == 0) {
                if (1 <= args.length) {
                    name = args[0].toJava(String.class);
                    if (2 <= args.length) {
                        bases = args[1].toJava(List.class);
                        if (3 == args.length) {
                            dict = args[2].toJava(Map.class);
                        }
                    }
                }

                PyObject clazz = new PyInterpretClassObject(this.runtime, name, bases);
                for (Map.Entry<PyObject, PyObject> e : dict.entrySet()) {
                    // valid {1: 2}
                    clazz.getFrame().getLocals().put(e.getKey().toJava(String.class), e.getValue());
                }

                return clazz;

            } else {
                throw this.runtime.newRaiseTypeError("type() takes 1 or 3 arguments");
            }
        }
    }

    @DefinePyFunction(name = __getattribute__)
    public PyObject __getattribute__(PyObject cls, PyObject key) {
        String javaKey = key.toJava(String.class);

        return this.runtime.builtins_type__getattribute__(cls, javaKey).orElseGet(() -> {
            PyObject specialVar = null;
            if (__name__.equals(javaKey)) {
                specialVar = cls.getFrame().getNotAppearLocals().get(javaKey);
            }

            if (specialVar != null) {
                return specialVar;
            }

            throw this.runtime.newRaiseException("AttributeError",
                    "type object '" + cls.getName() + "' object has no attribute '" + javaKey + "'");
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

        self.getFrame().getLocals().put(name.toJava(String.class), value);
    }

    @DefinePyFunction(name = __delattr__)
    public void __delattr__(PyObject self, PyObject name) {
        PyObject strType = this.runtime.typeOrThrow("builtins.str");

        if (!this.runtime.isInstance(name, strType)) {
            throw this.runtime.newRaiseTypeError(
                    "attribute name must be string, not '" + name.getFullName() + "'"
            );
        }

        self.getFrame().getLocals().remove(name.toJava(String.class));
    }


    @DefinePyFunction(name = __str__)
    public PyObject __str__(PyObject self) {
        if (!self.isType()) {
            return this.runtime.str(self);
        }

        return this.runtime.str("<class '" + self.getFullName() + "'>");
    }
}
