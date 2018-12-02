package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyType;

import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import static org.cafebabepy.util.ProtocolNames.__init__;
import static org.cafebabepy.util.ProtocolNames.__str__;

/**
 * Created by yotchang4s on 2018/08/11.
 */
@DefinePyType(name = "builtins.SimpleNamespace", appear = false)
public final class PySimpleNamespaceType extends AbstractCafeBabePyType {

    public PySimpleNamespaceType(Python runtime) {
        super(runtime);
    }

    @Override
    public PyObject getModule() {
        return this.runtime.moduleOrThrow("builtins"); // FIXME types
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, LinkedHashMap<String, PyObject> kwargs) {
        self.getFrame().putToNotAppearLocals("kwargs", this.runtime.dictStringKey(kwargs));
    }

    @DefinePyFunction(name = __str__)
    public PyObject __str__(PyObject self) {
        if (!this.runtime.isInstance(self, "SimpleNamespace", false)) {
            throw this.runtime.newRaiseTypeError("descriptor '__str__' requires a 'types.SimpleNamespace' object but received a '" + self.getFullName() + "'");
        }

        PyObject dict = self.getFrame().getFromNotAppearLocals("kwargs").orElseGet(this.runtime::dict);

        LinkedHashMap<PyObject, PyObject> items = new LinkedHashMap<>();
        this.runtime.iter(dict, key ->
                items.put(key, this.runtime.getitem(dict, key))
        );

        String str = items.entrySet().stream()
                .map(e -> e.getKey() + ", " + e.getValue())
                .collect(Collectors.joining(", ", "namespace(", ")"));

        return this.runtime.str(str);
    }
}
