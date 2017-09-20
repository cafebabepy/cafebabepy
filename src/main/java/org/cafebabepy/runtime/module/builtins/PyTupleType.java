package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefinePyFunction;
import org.cafebabepy.annotation.DefinePyType;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.object.java.PyIntObject;
import org.cafebabepy.runtime.object.iterator.PyTupleIteratorObject;
import org.cafebabepy.runtime.object.java.PyTupleObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.cafebabepy.util.ProtocolNames.*;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefinePyType(name = "builtins.tuple")
public class PyTupleType extends AbstractCafeBabePyType {

    public PyTupleType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __getitem__)
    public PyObject __getitem__(PyObject self, PyObject key) {
        if (!(self instanceof PyTupleObject)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__getitem__' requires a 'tuple' object but received a '"
                            + self.getType().getFullName()
                            + "'");
        }
        if (!(key instanceof PyIntObject)) {
            throw this.runtime.newRaiseTypeError(
                    "tuple indices must be integers or slices, not " + key.getType().getFullName());
        }

        PyTupleObject tuple = (PyTupleObject) self;
        PyIntObject index = (PyIntObject) key;

        return tuple.get(index);
    }

    @DefinePyFunction(name = __len__)
    public PyObject __len__(PyObject self) {
        if (!(self instanceof PyTupleObject)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__getitem__' requires a 'tuple' object but received a '"
                            + self.getType().getFullName()
                            + "'");
        }

        return ((PyTupleObject) self).getLen();
    }

    @DefinePyFunction(name = __iter__)
    public PyObject __iter__(PyObject self) {
        if (!(self instanceof PyTupleObject)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__iter__' requires a 'tuple' object but received a '"
                            + self.getType().getFullName()
                            + "'");
        }

        return new PyTupleIteratorObject(this.runtime, (PyTupleObject) self);
    }

    @DefinePyFunction(name = __str__)
    public PyObject __str__(PyObject self) {
        List<String> jlist = new ArrayList<>();
        this.runtime.iter(self, v -> {
            String jv = v.toJava(String.class);
            jlist.add(jv);
        });

        String jstr = jlist.stream().collect(Collectors.joining(", ", "(", ")"));

        return this.runtime.str(jstr);
    }
}
