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
        this(runtime, tuple.getList());
    }

    public PyTupleIteratorObject(Python runtime, Collection<PyObject> collection) {
        super(runtime, runtime.typeOrThrow("builtins.tuple_iterator", false), collection);
    }
}
