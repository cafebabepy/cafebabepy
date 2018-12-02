package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyModule;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyModule;
import org.cafebabepy.runtime.object.PyObjectObject;

import static org.cafebabepy.util.ProtocolNames.__len__;

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

        /*
        // FIXME 再帰的にtupleを見る
        if (classInfo instanceof PyTupleType) {
            this.runtime.iter(classInfo, types::add);
        }
        */

        for (PyObject type : clazz.getTypes()) {
            if (type == classInfo) {
                return this.runtime.True();
            }
        }

        return this.runtime.False();
    }

    // FIXME Same CPython
    @DefinePyFunction(name = "print")
    public PyObject print(PyObject objects) {
        System.out.println(objects.toJava(String.class));

        return this.runtime.None();
    }

    @DefinePyFunction(name = "chr")
    public PyObject chr(PyObject i) {
        int value = i.toJava(int.class);
        if (value < 0 || 0x10FFFF < value) {
            throw this.runtime.newRaiseException("builtins.ValueError", "chr() arg not in range(0x110000)");
        }

        return this.runtime.str(new String(new int[]{value}, 0, 1));
    }

    @DefinePyFunction(name = "len")
    public PyObject len(PyObject s) {
        PyObject len = this.runtime.getattrOptional(s, __len__).orElseThrow(() ->
                this.runtime.newRaiseTypeError("object of type '" + s.getFullName() + "' has no len()")
        );

        return len.call();
    }

    @DefinePyFunction(name = "iter")
    public PyObject iter(PyObject object) {
        // FIXME sentinel
        return this.runtime.iter(object);
    }

    @DefinePyFunction(name = "reversed")
    public PyObject reversed(PyObject seq) {
        return this.runtime.reversed(seq);
    }

    @DefinePyFunction(name = "zip")
    public PyObject zip(PyObject[] iterables) {
        PyObject object = new PyObjectObject(this.runtime);
        object.initialize();

        this.runtime.eval(object, "<FIXME>",
                "def zip(*iterables):\n" +
                        "  # zip('ABCD', 'xy') --> Ax By\n" +
                        "  sentinel = object()\n" +
                        "  iterators = [iter(it) for it in iterables]\n" +
                        "  while iterators:\n" +
                        "    result = []\n" +
                        "    for it in iterators:\n" +
                        "      elem = next(it, sentinel)\n" +
                        "      if elem is sentinel:\n" +
                        "        return\n" +
                        "      result.append(elem)\n" +
                        "      yield tuple(result)"
        );

        PyObject zip = object.getFrame().lookup("zip");
        if (zip == null) {
            throw new CafeBabePyException("zip is not found");
        }

        return zip.call(iterables);
    }

    @DefinePyFunction(name = "globals")
    public PyObject globals() {
        return this.runtime.dict(this.runtime.getEvaluator().getFrame().getGlobalsPyObjectMap());
    }
}
