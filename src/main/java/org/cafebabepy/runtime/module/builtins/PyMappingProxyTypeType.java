package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.runtime.object.java.PyDictObject;

import java.util.Map;
import java.util.stream.Collectors;

import static org.cafebabepy.util.ProtocolNames.*;

/**
 * Created by yotchang4s on 2018/05/20.
 */
@DefinePyType(name = "builtins.MappingProxyType", appear = false)
public class PyMappingProxyTypeType extends AbstractCafeBabePyType {

    public PyMappingProxyTypeType(Python runtime) {
        super(runtime);
    }

    @Override
    public String getName() {
        return "mappingproxy";
    }

    @DefinePyFunction(name = __str__)
    @SuppressWarnings("unchecked")
    public PyObject __str__(PyObject self) {
        Map<PyObject, PyObject> map = (Map<PyObject, PyObject>) self.toJava(Map.class);

        String jstr = map.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.joining(", ", "mappingproxy({", "})"));

        return this.runtime.str(jstr);
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
        PyObject value = dict.getRawMap().get(key);
        if (value == null) {
            throw this.runtime.newRaiseException("builtins.KeyError", key.toJava(String.class));
        }

        return value;
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
        return this.runtime.bool(dict.getRawMap().containsKey(key));
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
        PyObject value = dict.getRawMap().get(key);
        if (value == null) {
            if (defaultValue.length == 1) {
                value = defaultValue[0];
            } else {
                value = this.runtime.None();
            }
        }

        return value;
    }
}
