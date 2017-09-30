package org.cafebabepy.runtime.object.iterator;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.AbstractPyObjectObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * Created by yotchang4s on 2017/06/22.
 */
abstract class AbstractPyIteratorObject extends AbstractPyObjectObject {

    private final Collection<PyObject> collection;
    private final Iterator<PyObject> iterator;

    AbstractPyIteratorObject(Python runtime, Collection<PyObject> collection) {
        super(runtime);

        this.collection = Collections.unmodifiableCollection(new ArrayList<>(collection));
        this.iterator = collection.iterator();
    }

    public final Collection<PyObject> getCollection() {
        return this.collection;
    }

    public final boolean hasNext() {
        return this.iterator.hasNext();
    }

    public final PyObject next() {
        if (this.iterator.hasNext()) {
            return this.iterator.next();

        } else {
            throw this.runtime.newRaiseException("builtins.StopIteration");
        }
    }
}