package org.cafebabepy.runtime.module._ast;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.DefinePyFunction;
import org.cafebabepy.runtime.module.DefinePyFunctionDefaultValue;
import org.cafebabepy.runtime.module.DefinePyType;

import static org.cafebabepy.util.ProtocolNames.__init__;

/**
 * Created by yotchang4s on 2017/05/29.
 */
@DefinePyType(name = "_ast.Attribute", parent = {"_ast.expr"})
public class PyAttributeType extends AbstractAST {

    public PyAttributeType(Python runtime) {
        super(runtime);
    }

    @DefinePyFunction(name = __init__)
    public void __init__(PyObject self, PyObject value, PyObject attr, PyObject ctx) {
        if (value != null) {
            self.getFrame().getLocals().put("value", value);
        }
        if (attr != null) {
            self.getFrame().getLocals().put("attr", attr);
        }
        if (ctx != null) {
            self.getFrame().getLocals().put("ctx", ctx);
        }
    }

    @DefinePyFunctionDefaultValue(methodName = __init__, parameterName = "value")
    private PyObject __init__value() {
        return null;
    }

    @DefinePyFunctionDefaultValue(methodName = __init__, parameterName = "attr")
    private PyObject __init__attr() {
        return null;
    }

    @DefinePyFunctionDefaultValue(methodName = __init__, parameterName = "ctx")
    private PyObject __init__ctx() {
        return this.runtime.None();
    }

    @DefinePyFunctionDefaultValue(methodName = __init__, parameterName = "lineno")
    private PyObject __init__lineno() {
        return this.runtime.None();
    }

    @DefinePyFunctionDefaultValue(methodName = __init__, parameterName = "col_offset")
    private PyObject __init__cal_offset() {
        return this.runtime.None();
    }

    @Override
    String[] _fields() {
        return new String[]{"value", "attr", "ctx"};
    }
}