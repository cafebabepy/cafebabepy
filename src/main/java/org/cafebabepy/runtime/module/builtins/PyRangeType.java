package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.object.java.PyIntObject;
import org.cafebabepy.runtime.object.iterator.PyRangeIteratorObject;

import static org.cafebabepy.util.ProtocolNames.*;

/**
 * Created by yotchang4s on 2017/06/03.
 */
@DefinePyType(name = "builtins.range")
public class PyRangeType extends AbstractCafeBabePyType {

    public PyRangeType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject... args) {
        if (args.length == 0) {
            throw this.runtime.newRaiseTypeError("range expected 1 arguments, got 0");

        } else if (3 < args.length) {
            throw this.runtime.newRaiseTypeError("range expected at most 3 arguments, got " + args.length);
        }

        PyObject start;
        PyObject stop;
        PyObject step;

        if (args.length == 1) {
            start = this.runtime.number(0);
            stop = getInt(args[0]);
            step = this.runtime.number(1);

        } else if (args.length == 2) {
            start = getInt(args[0]);
            stop = getInt(args[1]);
            step = this.runtime.number(1);

        } else {
            start = getInt(args[0]);
            stop = getInt(args[1]);
            step = getInt(args[2]);
        }

        self.getScope().put("start", start);
        self.getScope().put("stop", stop);
        self.getScope().put("step", step);

        int stepInt = ((PyIntObject) step).getIntValue();
        if (stepInt <= 0) {
            throw this.runtime.newRaiseException("builtins.ValueError",
                    "range() arg 3 must not be zero");
        }
    }

    private PyObject getInt(PyObject object) {
        if (object instanceof PyIntObject) {
            return object;
        }
        PyObject indexMethod = object.getScope().get(__index__).orElseThrow(() ->
                this.runtime.newRaiseTypeError(
                        "'" + object.getFullName() + "' object cannot be interpreted as an integer")
        );

        PyObject intObject = indexMethod.call(object);
        if (intObject instanceof PyIntObject) {
            return intObject;
        }

        throw this.runtime.newRaiseTypeError("__index__ returned non-int (type " + intObject.getFullName() + ")");
    }

    @DefinePyFunction(name = __iter__)
    public PyObject __iter__(PyObject self) {
        if (!this.runtime.isInstance(self, this)) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__iter__' requires a 'range' object but received a '"
                            + self.getType().getFullName()
                            + "'");
        }

        PyObject start = self.getScope().getOrThrow("start");
        PyObject stop = self.getScope().getOrThrow("stop");
        PyObject step = self.getScope().getOrThrow("step");

        int startInt = ((PyIntObject) start).getIntValue();
        int stopInt = ((PyIntObject) stop).getIntValue();
        int stepInt = ((PyIntObject) step).getIntValue();

        return new PyRangeIteratorObject(this.runtime, startInt, stopInt, stepInt);
    }
}
