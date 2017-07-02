package org.cafebabepy.runtime.module.ast;

import org.cafebabepy.annotation.DefineCafeBabePyFunction;
import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

/**
 * Created by yotchang4s on 2017/06/07.
 */
@DefineCafeBabePyType(name = "ast.NodeVisitor")
public class PyNodeVisitorType extends AbstractCafeBabePyType {

    public PyNodeVisitorType(Python runtime) {
        super(runtime);
    }

    @DefineCafeBabePyFunction(name = "visit")
    public PyObject visit(PyObject self, PyObject node) {
        String method = "visit_" + node.getName();
        PyObject visitor = self.getObject(method).orElse(getObjectOrThrow("generic_visit"));

        return visitor.callSelf(self, node);
    }

    @DefineCafeBabePyFunction(name = "generic_visit")
    public void generic_visit(PyObject self, PyObject node) {
        PyObject visit = self.getType().getObjectOrThrow("visit");

        PyObject astModule = this.runtime.moduleOrThrow("ast");
        PyObject iter_fields = astModule.getObjectOrThrow("iter_fields");

        PyObject list = typeOrThrow("builtins.list");
        PyObject ast = this.runtime.typeOrThrow("_ast.AST");

        PyObject iter_fieldsResult = iter_fields.call(astModule, node);

        this.runtime.iter(iter_fieldsResult, fvs -> {
            PyObject[] fieldAndValue = new PyObject[2];
            this.runtime.iterIndex(fvs, (fv, i) -> fieldAndValue[i] = fv);

            PyObject value = fieldAndValue[1];
            if (this.runtime.isInstance(value, list)) {
                this.runtime.iter(value, item -> {
                    if (this.runtime.isInstance(item, ast)) {
                        visit.call(self, item);
                    }
                });

            } else if (this.runtime.isInstance(value, ast)) {
                visit.call(self, value);
            }
        });
    }
}
