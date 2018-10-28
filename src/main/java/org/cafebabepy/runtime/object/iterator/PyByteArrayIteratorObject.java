package org.cafebabepy.runtime.object.iterator;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;

import java.util.Iterator;

/**
 * Created by yotchang4s on 2018/10/28.
 */
public class PyByteArrayIteratorObject extends AbstractPyIteratorObject {

    // FIXME stub
    public PyByteArrayIteratorObject(Python runtime) {
        super(runtime, new ByteArrayIterator(runtime));
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("builtins.bytearray_iterator", false);
    }

    static class ByteArrayIterator implements Iterator<PyObject> {

        private final Python runtime;

        ByteArrayIterator(Python runtime) {
            this.runtime = runtime;
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public PyObject next() {
            throw this.runtime.newRaiseException("builtins.StopIteration");
        }
    }
}
