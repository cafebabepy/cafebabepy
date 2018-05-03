package org.cafebabepy.runtime.object.iterator;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.AbstractPyObjectObject;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by yotchang4s on 2017/06/22.
 */
abstract class AbstractPyIteratorObject extends AbstractPyObjectObject implements Iterator<PyObject> {

    private final Iterator<PyObject> iterator;

    AbstractPyIteratorObject(Python runtime, Collection<PyObject> collection) {
        this(runtime, collection.iterator());
    }

    AbstractPyIteratorObject(Python runtime, Iterator<PyObject> iterator) {
        super(runtime);

        this.iterator = iterator;
    }

    @Override
    public final boolean hasNext() {
        return this.iterator.hasNext();
    }

    @Override
    public final PyObject next() {
        if (this.iterator.hasNext()) {
            return this.iterator.next();

        } else {
            throw this.runtime.newRaiseException("builtins.StopIteration");
        }
    }
}