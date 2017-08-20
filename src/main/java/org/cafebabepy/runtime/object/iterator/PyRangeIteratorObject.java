package org.cafebabepy.runtime.object.iterator;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.AbstractPyObjectObject;

/**
 * Created by yotchang4s on 2017/06/19.
 */
public class PyRangeIteratorObject extends AbstractPyObjectObject {

    private int start;
    private int stop;
    private int step;

    private int i;

    public PyRangeIteratorObject(Python runtime, int start, int stop, int step) {
        super(runtime, runtime.typeOrThrow("builtins.range_iterator", false));

        this.start = start;
        this.stop = stop;
        this.step = step;

        this.i = 0;
    }

    public final boolean hasNext() {
        int number = this.start + this.step * this.i;
        if (this.step >= 0) {
            return this.i >= 0 && number < this.stop;

        } else {
            return this.i >= 0 && number > this.stop;
        }
    }

    public final PyObject next() {
        if (hasNext()) {
            int number = this.start + this.step * this.i;
            PyObject result = this.runtime.number(number);
            this.i++;

            return result;

        } else {
            throw this.runtime.newRaiseException("builtins.StopIteration");
        }
    }
}
