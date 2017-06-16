package org.cafebabepy.runtime.module.ast;

import org.cafebabepy.annotation.DefineCafeBabePyFunction;
import org.cafebabepy.annotation.DefineCafeBabePyModule;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyModule;
import org.cafebabepy.runtime.module.builtins.PyGeneratorType;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by yotchang4s on 2017/06/04.
 */
@DefineCafeBabePyModule(name = "ast")
public class PyAstModule extends AbstractCafeBabePyModule {

    public PyAstModule(Python runtime) {
        super(runtime);
    }

    @DefineCafeBabePyFunction(name = "iter_fields")
    public PyObject iter_fields(PyObject node) {
        Map<String, PyObject> objectMap = node.getObjects();
        Iterator<Map.Entry<String, PyObject>> objectIter = objectMap.entrySet().iterator();

        return PyGeneratorType.newGenerator(this.runtime, stopper -> {
            if (!objectIter.hasNext()) {
                stopper.stop();
                // Always RaiseException

                return this.runtime.None();
            }

            Map.Entry<String, PyObject> e = objectIter.next();

            return this.runtime.tuple(this.runtime.str(e.getKey()), e.getValue());
        });
    }
}
