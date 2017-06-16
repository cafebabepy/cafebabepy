package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefineCafeBabePyFunction;
import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

import java.util.Iterator;
import java.util.List;

import static org.cafebabepy.util.ProtocolNames.*;

/**
 * Created by yotchang4s on 2017/06/14.
 */
@DefineCafeBabePyType(name = "builtins.tuple_iterator", appear = false)
public class PyTupleIteratorType extends AbstractCafeBabePyType {

    public PyTupleIteratorType(Python runtime) {
        super(runtime);
    }

    @SuppressWarnings("unchecked")
    @DefineCafeBabePyFunction(name = __init__)
    public void __init__tuple_iterator(PyObject self, PyObject... args) {
        PyObject tuple = args[0];
        List<PyObject> list =
                (List<PyObject>) args[0].getJavaObject(PyTupleType.JAVA_LIST_NAME).orElseThrow(() ->
                        new CafeBabePyException("'" + PyTupleType.JAVA_LIST_NAME + "' is not found"));

        Iterator<PyObject> iter = list.iterator();
        PyObject generator = PyGeneratorType.newGenerator(this.runtime, stopper -> {
            if (!iter.hasNext()) {
                stopper.stop();
            }

            return iter.next();
        });

        self.getScope().put("_generator", generator);
    }

    @DefineCafeBabePyFunction(name = __next__)
    public PyObject __next__tuple_iterator(PyObject self) {
        PyObject generator = self.getObjectOrThrow("_generator");

        PyObject type = this.runtime.typeOrThrow("builtins.GeneratorType", false);
        return type.getObjectOrThrow(__next__).call(type, generator);
    }

    @DefineCafeBabePyFunction(name = __iter__)
    public PyObject __iter__tuple_iterator(PyObject self) {
        PyObject builtins = this.runtime.getBuiltinsModule();
        PyObject isinstance = builtins.getObjectOrThrow("isinstance");

        if (isinstance.call(self, typeOrThrow("builtins.tuple")).isFalse()) {
            throw this.runtime.newRaiseTypeError(
                    "descriptor '__iter__' requires a 'tuple' object but received a '" + getFullName() + "'");
        }

        return self;
    }
}
