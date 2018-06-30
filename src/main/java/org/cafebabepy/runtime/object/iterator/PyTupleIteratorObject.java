package org.cafebabepy.runtime.object.iterator;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.java.PyTupleObject;

import java.util.Collection;

/**
 * Created by yotchang4s on 2017/06/19.
 */
public class PyTupleIteratorObject extends AbstractPyIteratorObject {

    public PyTupleIteratorObject(Python runtime, PyTupleObject tuple) {
        this(runtime, tuple.getRawValues());
    }

    public PyTupleIteratorObject(Python runtime, Collection<PyObject> collection) {
        super(runtime, collection);
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("builtins.tuple_iterator", false);
    }
}
