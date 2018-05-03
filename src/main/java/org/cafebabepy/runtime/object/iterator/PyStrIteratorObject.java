package org.cafebabepy.runtime.object.iterator;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.java.PyStrObject;
import org.cafebabepy.util.StringUtils;

import java.util.Iterator;

/**
 * Created by yotchang4s on 2018/05/03.
 */
public class PyStrIteratorObject extends AbstractPyIteratorObject {

    public PyStrIteratorObject(Python runtime, PyStrObject str) {
        super(runtime, new StringIterator(runtime, str.getValue()));
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("builtins.str_iterator", false);
    }

    static class StringIterator implements Iterator<PyObject> {

        private final Python runtime;

        private final String value;
        private final int count;
        private int index;

        StringIterator(Python runtime, String value) {
            this.runtime = runtime;
            this.value = value;
            this.count = this.value.codePointCount(0, value.length());
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
            return this.runtime.str(StringUtils.codePointAt(this.value, this.index++));
        }
    }
}
