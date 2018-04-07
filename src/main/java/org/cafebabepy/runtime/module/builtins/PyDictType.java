package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.runtime.object.java.PyDictObject;
import org.cafebabepy.runtime.object.java.PyIntObject;
import org.cafebabepy.runtime.object.java.PyTupleObject;

import java.util.Map;
import java.util.stream.Collectors;

import static org.cafebabepy.util.ProtocolNames.__getitem__;
import static org.cafebabepy.util.ProtocolNames.__setitem__;
import static org.cafebabepy.util.ProtocolNames.__str__;

/**
 * Created by yotchang4s on 2018/03/10.
 */
@DefinePyType(name = "builtins.dict")
public class PyDictType extends AbstractCafeBabePyType {

    public PyDictType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __str__)
    @SuppressWarnings("unchecked")
    public PyObject __str__(PyObject self) {
        Map<PyObject, PyObject> map = (Map<PyObject, PyObject>) self.toJava(Map.class);

        String jstr = map.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.joining(", ", "{", "}"));

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

    @DefinePyFunction(name = __setitem__)
    public PyObject __setitem__(PyObject self, PyObject key, PyObject value) {
        if (!this.runtime.isInstance(self, "builtins.dict")) {
            throw this.runtime.newRaiseTypeError("descriptor '__setitem__' requires a 'dict' object but received a '" + self.getType().getName() + "'");
        }

        if (!PyDictObject.class.isAssignableFrom(self.getClass())) {
            throw new CafeBabePyException(PyDictObject.class.getName() + " is not assignable from " + self.getClass().getName());
        }

        PyDictObject dict = (PyDictObject) self;
        dict.getRawMap().put(key, value);

        return this.runtime.None();
    }
}
