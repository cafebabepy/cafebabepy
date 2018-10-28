package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyFunctionDefaultValue;
import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.runtime.object.PyObjectObject;
import org.cafebabepy.runtime.object.iterator.PySetIteratorObject;
import org.cafebabepy.runtime.object.java.PySetObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.cafebabepy.util.ProtocolNames.*;

/**
 * Created by yotchang4s on 2018/10/28.
 */
@DefinePyType(name = "builtins.set")
public class PySetType extends AbstractCafeBabePyType {

    public PySetType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = "__init__")
    public void __init__(PyObject self, PyObject iterable) {
        if (!(self instanceof PySetObject)) {
            throw this.runtime.newRaiseTypeError("descriptor '__init__' requires a 'set' object but received a '" + self.getType().getFullName() + "'");
        }

        getScope().get(this.runtime.str("___init__itarable_default_value"), false).ifPresent(v -> {
            if (v != iterable) {
                PySetObject object = (PySetObject) self;
                this.runtime.iter(iterable, item -> object.getView().add(item));
            }
        });
    }

    @DefinePyFunctionDefaultValue(methodName = __init__, parameterName = "iterable")
    public PyObject __init___iterable() {
        return getScope().get(this.runtime.str("___init__itarable_default_value"), false).orElseGet(() -> {
            PyObject object = new PyObjectObject(this.runtime);
            getScope().put(this.runtime.str("___init__itarable_default_value"), object, false);

            return object;
        });
    }

    @DefinePyFunction(name = __len__)
    public PyObject __len__(PyObject self) {
        if (!(self instanceof PySetObject)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__len__' requires a 'set' object but received a '"
                            + self.getType().getFullName()
                            + "'");
        }

        return ((PySetObject) self).getLen();
    }

    @DefinePyFunction(name = __iter__)
    public PyObject __iter__(PyObject self) {
        if (!(self instanceof PySetObject)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__iter__' requires a 'set' object but received a '"
                            + self.getType().getFullName()
                            + "'");
        }

        return new PySetIteratorObject(this.runtime, (PySetObject) self);
    }

    @DefinePyFunction(name = __str__)
    public PyObject __str__(PyObject self) {
        List<String> jlist = new ArrayList<>();
        this.runtime.iter(self, v -> {
            String jv = v.toJava(String.class);
            jlist.add(jv);
        });

        String jstr = jlist.stream().collect(Collectors.joining(", ", "{", "}"));

        return this.runtime.str(jstr);
    }

    @DefinePyFunction(name = __eq__)
    public PyObject __eq__(PyObject self, PyObject other) {
        if (!(self instanceof PySetObject)) {
            throw this.runtime.newRaiseTypeError("descriptor '__eq__' requires a 'set' object but received a '" + self.getType() + "'");
        }

        if (!(other instanceof PySetObject)) {
            return this.runtime.NotImplemented();
        }

        PySetObject v1 = (PySetObject) self;
        PySetObject v2 = (PySetObject) other;

        return this.runtime.bool(v1.getView().equals(v2.getView()));
    }

    @DefinePyFunction(name = __hash__)
    public PyObject __hash__(PyObject self) {
        if (!(self instanceof PySetObject)) {
            throw this.runtime.newRaiseTypeError("descriptor '__hash__' requires a 'set' object but received a '" + self.getType() + "'");
        }

        PySetObject object = (PySetObject) self;

        return this.runtime.number(object.getView().hashCode());
    }
}
