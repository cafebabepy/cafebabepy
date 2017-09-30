package org.cafebabepy.runtime.object.iterator;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.java.PyListObject;

import java.util.Collection;

/**
 * Created by yotchang4s on 2017/06/19.
 */
public class PyListIteratorObject extends AbstractPyIteratorObject {

    public PyListIteratorObject(Python runtime, PyListObject list) {
        this(runtime, list.getList());
    }

    public PyListIteratorObject(Python runtime, Collection<PyObject> collection) {
        super(runtime, collection);
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("builtins.list_iterator", false);
    }
}
