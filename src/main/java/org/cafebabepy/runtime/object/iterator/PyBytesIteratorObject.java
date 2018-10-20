package org.cafebabepy.runtime.object.iterator;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.java.PyBytesObject;

import java.util.Iterator;

/**
 * Created by yotchang4s on 2018/10/17.
 */
public class PyBytesIteratorObject extends AbstractPyIteratorObject {

    public PyBytesIteratorObject(Python runtime, PyBytesObject str) {
        super(runtime, new BytesIterator(runtime, str.getValue()));
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("builtins.bytes_iterator", false);
    }

    static class BytesIterator implements Iterator<PyObject> {

        private final Python runtime;

        private final int[] value;
        private final int count;
        private int index;

        BytesIterator(Python runtime, int[] value) {
            this.runtime = runtime;
            this.value = value;
            this.count = 0;
            this.index = 0;
        }

        @Override
        public boolean hasNext() {
            return index < count;
        }

        @Override
        public PyObject next() {
            if (!hasNext()) {
                throw this.runtime.newRaiseException("builtins.StopIteration");
            }

            return this.runtime.number(this.value[index++]);
        }
    }
}
