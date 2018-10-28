package org.cafebabepy.runtime.object.iterator;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.java.PyListObject;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by yotchang4s on 2018/10/28.
 */
public class PyListReverseIteratorObject extends AbstractPyIteratorObject {

    public PyListReverseIteratorObject(Python runtime, PyListObject collection) {
        super(runtime, reverse(collection.getValues()));
    }

    private static Collection<PyObject> reverse(List<PyObject> collection) {
        Collections.reverse(collection);

        return collection;
    }

    @Override
    public PyObject getType() {
        return this.runtime.typeOrThrow("builtins.list_reverseiterator", false);
    }
}
