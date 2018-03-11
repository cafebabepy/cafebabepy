package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;

import java.util.Map;
import java.util.stream.Collectors;

import static org.cafebabepy.util.ProtocolNames.__str__;

/**
 * Created by yotchang4s on 2018/03/10.
 */
@DefinePyType(name = "builtins.dict")
public class PyDictType extends AbstractCafeBabePyType {

    public PyDictType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __str__)
    @SuppressWarnings("unchecked")
    public PyObject __str__(PyObject self) {
        Map<PyObject, PyObject> map = (Map<PyObject, PyObject>) self.toJava(Map.class);

        String jstr = map.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.joining(", ", "{", "}"));

        return this.runtime.str(jstr);
    }
}
