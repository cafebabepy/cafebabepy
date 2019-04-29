package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyFunctionDefaultValue;
import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.runtime.object.PyObjectObject;
import org.cafebabepy.runtime.object.iterator.PySetIteratorObject;
import org.cafebabepy.runtime.object.java.PyFloatObject;
import org.cafebabepy.runtime.object.java.PyFrozensetObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.cafebabepy.util.ProtocolNames.*;

/**
 * Created by yotchang4s on 2019/04/29.
 */
@DefinePyType(name = "builtins.frozenset")
public class PyFrozensetType extends AbstractCafeBabePyType {

    public PyFrozensetType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = "__init__")
    public void __init__(PyObject self, PyObject iterable) {
        if (!(self instanceof PyFrozensetObject)) {
            throw this.runtime.newRaiseTypeError("descriptor '__init__' requires a 'frozenset' object but received a '" + self.getType().getFullName() + "'");
        }

        PyObject v = getFrame().getNotAppearLocals().get("___init__itarable_default_value");
        if (v != null && v != iterable) {
            PyFrozensetObject object = (PyFrozensetObject) self;
            this.runtime.iter(iterable, item -> object.getView().add(item));
        }
    }

    @DefinePyFunctionDefaultValue(methodName = __init__, parameterName = "iterable")
    public PyObject __init___iterable() {
        PyObject object = getFrame().getNotAppearLocals().get("___init__itarable_default_value");
        if (object == null) {
            object = new PyObjectObject(this.runtime);
            object.initialize();

            getFrame().getNotAppearLocals().put("___init__itarable_default_value", object);
        }

        return object;
    }

    @DefinePyFunction(name = __len__)
    public PyObject __len__(PyObject self) {
        if (!(self instanceof PyFrozensetObject)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__len__' requires a 'frozenset' object but received a '"
                            + self.getType().getFullName()
                            + "'");
        }

        return this.runtime.number(((PyFrozensetObject) self).getView().size());
    }

    @DefinePyFunction(name = __iter__)
    public PyObject __iter__(PyObject self) {
        if (!(self instanceof PyFrozensetObject)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__iter__' requires a 'frozenset' object but received a '"
                            + self.getType().getFullName()
                            + "'");
        }

        return new PySetIteratorObject(this.runtime, ((PyFrozensetObject) self).getView());
    }

    @DefinePyFunction(name = __str__)
    public PyObject __str__(PyObject self) {
        List<String> jlist = new ArrayList<>();
        this.runtime.iter(self, v -> {
            String jv = v.toJava(String.class);
            jlist.add(jv);
        });

        String jstr = jlist.stream().collect(Collectors.joining(", ", "{", "}"));

        return this.runtime.str("frozenset" + jstr + ")");
    }

    @DefinePyFunction(name = __eq__)
    public PyObject __eq__(PyObject self, PyObject other) {
        if (!(self instanceof PyFrozensetObject)) {
            throw this.runtime.newRaiseTypeError("descriptor '__eq__' requires a 'frozenset' object but received a '" + self.getType() + "'");
        }

        if (!(other instanceof PyFrozensetObject)) {
            return this.runtime.NotImplemented();
        }

        PyFrozensetObject v1 = (PyFrozensetObject) self;
        PyFrozensetObject v2 = (PyFrozensetObject) other;

        return this.runtime.bool(v1.getView().equals(v2.getView()));
    }

    @DefinePyFunction(name = __hash__)
    public PyObject __hash__(PyObject self) {
        if (!(self instanceof PyFrozensetObject)) {
            throw this.runtime.newRaiseTypeError("descriptor '__hash__' requires a 'set' object but received a '" + self.getType() + "'");
        }

        PyFrozensetObject object = (PyFrozensetObject) self;

        return this.runtime.number(object.getView().hashCode());
    }
}
