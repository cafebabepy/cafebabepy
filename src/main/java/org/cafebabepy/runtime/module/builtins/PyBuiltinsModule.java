package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefinePyFunction;
import org.cafebabepy.annotation.DefinePyModule;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyModule;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by yotchang4s on 2017/05/12.
 */
@DefinePyModule(name = "builtins")
public class PyBuiltinsModule extends AbstractCafeBabePyModule {
    public PyBuiltinsModule(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = "isinstance")
    public PyObject isinstance(PyObject object, PyObject classInfo) {
        if (!classInfo.isType() && !(classInfo instanceof PyTupleType)) {
            throw this.runtime.newRaiseTypeError(
                    "isinstance() arg 2 must be a type or tuple of types");
        }

        return issubclass(object.getType(), classInfo);
    }

    @DefinePyFunction(name = "issubclass")
    public PyObject issubclass(PyObject clazz, PyObject classInfo) {
        if (!clazz.isType()
                || (!classInfo.isType() && !(classInfo instanceof PyTupleType))) {
            throw this.runtime.newRaiseTypeError(
                    "issubclass() arg 2 must be a type or tuple of types");
        }

        Set<PyObject> objectTypeSet = new HashSet<>();
        objectTypeSet.addAll(clazz.getTypes());

        // FIXME 再帰的にtupleを見る
        if (classInfo instanceof PyTupleType) {
            this.runtime.iter(classInfo, objectTypeSet::add);
        }

        if (objectTypeSet.contains(classInfo)) {
            return this.runtime.True();
        }

        return this.runtime.False();
    }

    // FIXME Same CPython
    @DefinePyFunction(name = "print")
    public PyObject print(PyObject objects) {
        System.out.println(objects.toJava(String.class));

        return this.runtime.None();
    }
}
