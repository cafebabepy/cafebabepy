package org.cafebabepy.runtime.object.iterator;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.java.PySetObject;

import java.util.Collection;

/**
 * Created by yotchang4s on 2018/10/28.
 */
public class PySetIteratorObject extends AbstractPyIteratorObject {

    public PySetIteratorObject(Python runtime, PySetObject set) {
        this(runtime, set.getView());
    }

    public PySetIteratorObject(Python runtime, Collection<PyObject> collection) {
        super(runtime, collection);
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("builtins.set_iterator", false);
    }
}
