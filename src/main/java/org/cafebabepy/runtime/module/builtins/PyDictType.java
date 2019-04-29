package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.runtime.object.java.PyDictItemsObject;
import org.cafebabepy.runtime.object.java.PyDictKeysObject;
import org.cafebabepy.runtime.object.java.PyDictObject;
import org.cafebabepy.runtime.object.java.PyDictValuesObject;

import java.util.Map;
import java.util.stream.Collectors;

import static org.cafebabepy.util.ProtocolNames.*;

/**
 * Created by yotchang4s on 2018/03/10.
 */
@DefinePyType(name = "builtins.dict")
public class PyDictType extends AbstractCafeBabePyType {

    public PyDictType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __iter__)
    public PyObject __iter__(PyObject self) {
        return this.runtime.iter(keys(self)).call();
    }

    @DefinePyFunction(name = __getitem__)
    public PyObject __getitem__(PyObject self, PyObject key) {
        if (!this.runtime.isInstance(self, "builtins.dict")) {
            throw this.runtime.newRaiseTypeError("descriptor '__getitem__' requires a 'dict' object but received a '" + self.getType().getName() + "'");
        }

        if (!PyDictObject.class.isAssignableFrom(self.getClass())) {
            throw new CafeBabePyException(PyDictObject.class.getName() + " is not assignable from " + self.getClass().getName());
        }

        PyDictObject dict = (PyDictObject) self;
        PyObject value = dict.getView().get(key);
        if (value == null) {
            throw this.runtime.newRaiseException("builtins.KeyError", key.toJava(String.class));
        }

        return value;
    }

    @DefinePyFunction(name = __setitem__)
    public PyObject __setitem__(PyObject self, PyObject key, PyObject value) {
        if (!this.runtime.isInstance(self, "builtins.dict")) {
            throw this.runtime.newRaiseTypeError("descriptor '__setitem__' requires a 'dict' object but received a '" + self.getType().getName() + "'");
        }

        if (!PyDictObject.class.isAssignableFrom(self.getClass())) {
            throw new CafeBabePyException(PyDictObject.class.getName() + " is not assignable from " + self.getClass().getName());
        }

        PyDictObject dict = (PyDictObject) self;
        dict.getView().put(key, value);

        return this.runtime.None();
    }

    @DefinePyFunction(name = __contains__)
    public PyObject __contains__(PyObject self, PyObject key) {
        if (!this.runtime.isInstance(self, "builtins.dict")) {
            throw this.runtime.newRaiseTypeError("descriptor '__contains__' requires a 'dict' object but received a '" + self.getType().getName() + "'");
        }

        if (!PyDictObject.class.isAssignableFrom(self.getClass())) {
            throw new CafeBabePyException(PyDictObject.class.getName() + " is not assignable from " + self.getClass().getName());
        }

        PyDictObject dict = (PyDictObject) self;
        return this.runtime.bool(dict.getView().containsKey(key));
    }

    @DefinePyFunction(name = __delitem__)
    public void __delitem__(PyObject self, PyObject key) {
        if (!this.runtime.isInstance(self, "builtins.dict")) {
            throw this.runtime.newRaiseTypeError("descriptor '__delitem__' requires a 'dict' object but received a '" + self.getType().getName() + "'");
        }

        if (!PyDictObject.class.isAssignableFrom(self.getClass())) {
            throw new CafeBabePyException(PyDictObject.class.getName() + " is not assignable from " + self.getClass().getName());
        }

        PyDictObject dict = (PyDictObject) self;
        PyObject remove = dict.getView().remove(key);
        if (remove == null) {
            throw this.runtime.newRaiseException("builtins.KeyError", String.valueOf(key));
        }
    }

    // FIXME default value
    @DefinePyFunction(name = "get")
    public PyObject get(PyObject self, PyObject key, PyObject... defaultValue) {
        if (!this.runtime.isInstance(self, "builtins.dict")) {
            throw this.runtime.newRaiseTypeError("descriptor 'get' requires a 'dict' object but received a '" + self.getType().getName() + "'");
        }

        if (!PyDictObject.class.isAssignableFrom(self.getClass())) {
            throw new CafeBabePyException(PyDictObject.class.getName() + " is not assignable from " + self.getClass().getName());
        }

        if (defaultValue.length > 1) {
            throw this.runtime.newRaiseTypeError("get expected at most 2 arguments, got " + (1 + defaultValue.length));
        }

        PyDictObject dict = (PyDictObject) self;
        PyObject value = dict.getView().get(key);
        if (value == null) {
            if (defaultValue.length == 1) {
                value = defaultValue[0];
            } else {
                value = this.runtime.None();
            }
        }

        return value;
    }

    @DefinePyFunction(name = __str__)
    @SuppressWarnings("unchecked")
    public PyObject __str__(PyObject self) {
        Map<PyObject, PyObject> map = (Map<PyObject, PyObject>) self.toJava(Map.class);

        String jstr = map.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.joining(", ", "{", "}"));

        return this.runtime.str(jstr);
    }

    @DefinePyFunction(name = __eq__)
    public PyObject __eq__(PyObject self, PyObject other) {
        if (!(self instanceof PyDictObject)) {
            throw this.runtime.newRaiseTypeError("descriptor '__eq__' requires a 'dict' object but received a '" + self.getType() + "'");
        }

        if (!(other instanceof PyDictObject)) {
            return this.runtime.NotImplemented();
        }

        PyDictObject v1 = (PyDictObject) self;
        PyDictObject v2 = (PyDictObject) other;

        return this.runtime.bool(v1.getView().equals(v2.getView()));
    }

    @DefinePyFunction(name = __hash__)
    public PyObject __hash__(PyObject self) {
        if (!(self instanceof PyDictObject)) {
            throw this.runtime.newRaiseTypeError("descriptor '__hash__' requires a 'dict' object but received a '" + self.getType() + "'");
        }

        PyDictObject list = (PyDictObject) self;

        return this.runtime.number(list.getView().hashCode());
    }

    @DefinePyFunction(name = "keys")
    public PyObject keys(PyObject self) {
        if (!(self instanceof PyDictObject)) {
            throw this.runtime.newRaiseTypeError("descriptor 'keys' requires a 'dict' object but received a '" + self.getType() + "'");
        }

        PyDictObject dict = (PyDictObject) self;

        return new PyDictKeysObject(this.runtime, dict.getView().keySet());
    }

    @DefinePyFunction(name = "values")
    public PyObject values(PyObject self) {
        if (!(self instanceof PyDictObject)) {
            throw this.runtime.newRaiseTypeError("descriptor 'values' requires a 'dict' object but received a '" + self.getType() + "'");
        }

        PyDictObject dict = (PyDictObject) self;

        return new PyDictValuesObject(this.runtime, dict.getView().keySet());
    }

    @DefinePyFunction(name = "items")
    public PyObject items(PyObject self) {
        if (!(self instanceof PyDictObject)) {
            throw this.runtime.newRaiseTypeError("descriptor 'values' requires a 'dict' object but received a '" + self.getType() + "'");
        }

        PyDictObject dict = (PyDictObject) self;

        return new PyDictItemsObject(this.runtime, dict.getView());
    }
}
