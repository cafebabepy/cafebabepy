package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.runtime.object.iterator.PyListIteratorObject;
import org.cafebabepy.runtime.object.java.PyIntObject;
import org.cafebabepy.runtime.object.java.PyListObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.cafebabepy.util.ProtocolNames.*;

/**
 * Created by yotchang4s on 2017/06/03.
 */
@DefinePyType(name = "builtins.list")
public class PyListType extends AbstractCafeBabePyType {

    public PyListType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __getitem__)
    public PyObject __getitem__(PyObject self, PyObject key) {
        if (!(self instanceof PyListObject)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__getitem__' requires a 'list' object but received a '"
                            + self.getType().getFullName()
                            + "'");
        }
        if (!(key instanceof PyIntObject)) {
            throw this.runtime.newRaiseTypeError(
                    "list indices must be integers or slices, not " + key.getType().getFullName());
        }

        PyListObject list = (PyListObject) self;
        PyIntObject index = (PyIntObject) key;

        return list.get(index);
    }

    @DefinePyFunction(name = __len__)
    public PyObject __len__(PyObject self) {
        if (!(self instanceof PyListObject)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__len__' requires a 'list' object but received a '"
                            + self.getType().getFullName()
                            + "'");
        }

        return ((PyListObject) self).getLen();
    }

    @DefinePyFunction(name = __iter__)
    public PyObject __iter__(PyObject self) {
        if (!(self instanceof PyListObject)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__iter__' requires a 'list' object but received a '"
                            + self.getType().getFullName()
                            + "'");
        }

        return new PyListIteratorObject(this.runtime, (PyListObject) self);
    }

    @DefinePyFunction(name = __str__)
    public PyObject __str__(PyObject self) {
        List<String> jlist = new ArrayList<>();
        this.runtime.iter(self, v -> {
            String jv = v.toJava(String.class);
            jlist.add(jv);
        });

        String jstr = jlist.stream().collect(Collectors.joining(", ", "[", "]"));

        return this.runtime.str(jstr);
    }

    @DefinePyFunction(name = __eq__)
    public PyObject __eq__(PyObject self, PyObject other) {
        if (!(self instanceof PyListObject)) {
            throw this.runtime.newRaiseTypeError("descriptor '__eq__' requires a 'list' object but received a '" + self.getType() + "'");
        }

        if (!(other instanceof PyListObject)) {
            return this.runtime.NotImplemented();
        }

        PyListObject v1 = (PyListObject) self;
        PyListObject v2 = (PyListObject) other;

        return this.runtime.bool(v1.getRawList().equals(v2.getRawList()));
    }

    @DefinePyFunction(name = __hash__)
    public PyObject __hash__(PyObject self) {
        if (!(self instanceof PyListObject)) {
            throw this.runtime.newRaiseTypeError("descriptor '__hash__' requires a 'list' object but received a '" + self.getType() + "'");
        }

        PyListObject list = (PyListObject) self;

        return this.runtime.number(list.getRawList().hashCode());
    }
}
