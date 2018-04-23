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
        PyListObject list = (PyListObject) self;

        if (key instanceof PyIntObject) {
            PyIntObject index = (PyIntObject) key;

            return list.get(index);

        } else if (this.runtime.isInstance(key, "builtins.slice")) {
            PyObject start = this.runtime.getattr(key, "start");
            PyObject stop = this.runtime.getattr(key, "stop");
            PyObject step = this.runtime.getattr(key, "step");

            int startInt;
            int stopInt;
            int stepInt;

            if (start.isNone()) {
                startInt = 0;
            } else {
                startInt = getInt(start).getIntValue();
            }
            if (stop.isNone()) {
                stopInt = list.getRawList().size();

            } else {
                stopInt = getInt(stop).getIntValue();
            }
            if (step.isNone()) {
                stepInt = 1;

            } else {
                stepInt = getInt(step).getIntValue();
                if (stepInt == 0) {
                    throw this.runtime.newRaiseException("builtins.ValueError", "slice step cannot be zero");
                }
            }
            if (startInt < 0) {
                startInt = list.getRawList().size() + startInt;
            }
            if (stopInt < 0) {
                stopInt = list.getRawList().size() + stopInt;
            }
            List<PyObject> jlist = new ArrayList<>();
            for (int i = startInt; i < stopInt && i < list.getRawList().size(); i += stepInt) {
                jlist.add(list.getRawList().get(i));
            }

            return this.runtime.list(jlist);

        } else {
            throw this.runtime.newRaiseTypeError(
                    "list indices must be integers or slices, not " + key.getType().getFullName());
        }
    }

    private PyIntObject getInt(PyObject object) {
        if (object instanceof PyIntObject) {
            return (PyIntObject) object;
        }
        PyObject indexMethod = this.runtime.getattrOptional(object, __index__).orElseThrow(() ->
                this.runtime.newRaiseTypeError(
                        "'" + object.getFullName() + "' object cannot be interpreted as an integer")
        );

        PyObject intObject = indexMethod.call(object);
        if (intObject instanceof PyIntObject) {
            return (PyIntObject) intObject;
        }

        throw this.runtime.newRaiseTypeError("__index__ returned non-int (type " + intObject.getFullName() + ")");
    }

    @DefinePyFunction(name = __setitem__)
    public void __setitem__(PyObject self, PyObject key, PyObject value) {
        if (!(self instanceof PyListObject)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__setitem__' requires a 'list' object but received a '"
                            + self.getType().getFullName()
                            + "'");
        }

        PyListObject list = (PyListObject) self;

        if (key instanceof PyIntObject) {
            List<PyObject> jlist = ((PyListObject) self).getRawList();
            PyIntObject index = (PyIntObject) key;

            if (jlist.size() <= index.getIntValue()) {
                throw this.runtime.newRaiseException("builtins.IndexError", "list assignment index out of range");
            }

            jlist.set(index.getIntValue(), value);

        } else if (this.runtime.isInstance(key, "builtins.slice")) {
            if (!this.runtime.isIterable(value)) {
                throw this.runtime.newRaiseTypeError("can only assign an iterable");
            }

            PyObject start = this.runtime.getattr(key, "start");
            PyObject stop = this.runtime.getattr(key, "stop");
            PyObject step = this.runtime.getattr(key, "step");

            int startInt;
            int stopInt;
            int stepInt;

            if (start.isNone()) {
                startInt = 0;
            } else {
                startInt = getInt(start).getIntValue();
            }
            if (stop.isNone()) {
                stopInt = list.getRawList().size();

            } else {
                stopInt = getInt(stop).getIntValue();
            }
            if (step.isNone()) {
                stepInt = 1;

            } else {
                stepInt = getInt(step).getIntValue();
                if (stepInt == 0) {
                    throw this.runtime.newRaiseException("builtins.ValueError", "slice step cannot be zero");
                }
            }
            if (startInt < 0) {
                startInt = list.getRawList().size() + startInt;
            }
            if (stopInt < 0) {
                stopInt = list.getRawList().size() + stopInt;
            }

            if (stopInt > list.getRawList().size()) {
                stopInt = list.getRawList().size() - 1;
            }

            List<PyObject> newList = new ArrayList<>();

            for (int i = 0; i < startInt; i += stepInt) {
                newList.add(list.getRawList().get(i));
            }
            for (int i = stopInt; i < list.getRawList().size(); i += stepInt) {
                newList.add(list.getRawList().get(i));
            }

            List<PyObject> iterList = new ArrayList<>();
            this.runtime.iter(value, iterList::add);

            if (iterList.size() == 1) {
                newList.add(startInt, iterList.get(0));

            } else {
                newList.addAll(startInt, iterList);
            }

            list.getRawList().clear();
            list.getRawList().addAll(newList);

        } else {
            throw this.runtime.newRaiseTypeError(
                    "list indices must be integers or slices, not " + key.getType().getFullName());
        }
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
