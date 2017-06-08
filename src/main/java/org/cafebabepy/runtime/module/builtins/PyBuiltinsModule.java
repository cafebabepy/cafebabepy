package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefineCafeBabePyFunction;
import org.cafebabepy.annotation.DefineCafeBabePyModule;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyModule;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by yotchang4s on 2017/05/12.
 */
@DefineCafeBabePyModule(name = "builtins")
public class PyBuiltinsModule extends AbstractCafeBabePyModule {
    public PyBuiltinsModule(Python runtime) {
        super(runtime);
    }

    @DefineCafeBabePyFunction(name = "issubclass")
    public PyObject isSubClass(PyObject clazz, PyObject classInfo) {
        if (clazz == null || !clazz.isType()) {
            throw this.runtime.newRaiseTypeError(
                    "issubclass() arg 1 must be a class");
        }

        Set<PyObject> classInfoSet = new HashSet<>();
        appendIntoClassInfoSet(classInfoSet, classInfo);

        if (classInfoSet.contains(classInfo)) {
            return this.runtime.True();
        } else {
            return this.runtime.False();
        }
    }

    private void appendIntoClassInfoSet(Set<PyObject> classInfoSet, PyObject classinfo) {
        if (classinfo == null || !classinfo.isType()) {
            throw this.runtime.newRaiseTypeError(
                    "issubclass() arg 2 must be a class or tuple of classes");

        } else if (classinfo.getType() instanceof PyTupleType) {
            this.runtime.iter(classinfo, c -> appendIntoClassInfoSet(classInfoSet, c));

        } else {
            classInfoSet.add(classinfo);
        }
    }
}
