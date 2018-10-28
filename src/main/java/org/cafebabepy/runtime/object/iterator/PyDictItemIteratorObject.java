package org.cafebabepy.runtime.object.iterator;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.java.PyDictItemsObject;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by yotchang4s on 2018/10/17.
 */
public class PyDictItemIteratorObject extends AbstractPyIteratorObject {

    public PyDictItemIteratorObject(Python runtime, PyDictItemsObject dictValues) {
        super(runtime, new DictItemIterator(runtime, dictValues.getValue()));
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("builtins.dict_itemiterator", false);
    }

    static class DictItemIterator implements Iterator<PyObject> {

        private final Python runtime;

        private Map<PyObject, PyObject> view;
        private Iterator<PyObject> iterator;

        DictItemIterator(Python runtime, Map<PyObject, PyObject> view) {
            this.runtime = runtime;

            this.view = view;
            this.iterator = view.keySet().iterator();
        }

        @Override
        public boolean hasNext() {
            try {
                return this.iterator.hasNext();

            } catch (ConcurrentModificationException e) {
                throw this.runtime.newRaiseException("RuntimeError", "dictionary changed size during iteration");
            }
        }

        @Override
        public PyObject next() {
            if (!hasNext()) {
                throw this.runtime.newRaiseException("builtins.StopIteration");
            }

            try {
                PyObject key = this.iterator.next();
                PyObject value = this.view.get(key);

                return this.runtime.tuple(key, value);

            } catch (ConcurrentModificationException e) {
                throw this.runtime.newRaiseException("RuntimeError", "dictionary changed size during iteration");
            }
        }
    }
}
