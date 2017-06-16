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

        return visitor.call(self.getType(), self, node);
    }

    @DefineCafeBabePyFunction(name = "generic_visit")
    public void generic_visit(PyObject self, PyObject node) {
        PyObject visit = self.getType().getObjectOrThrow("visit");

        PyObject builtins = this.runtime.getBuiltinsModule();

        PyObject astModule = this.runtime.moduleOrThrow("ast");
        PyObject iter_fields = astModule.getObjectOrThrow("iter_fields");
        PyObject isinstance = builtins.getObjectOrThrow("isinstance");

        PyObject list = typeOrThrow("builtins.list");
        PyObject ast = this.runtime.typeOrThrow("_ast.AST");

        PyObject iter_fieldsResult = iter_fields.call(astModule, node);

        this.runtime.iter(iter_fieldsResult, fvs -> {
            PyObject[] fieldAndValue = new PyObject[2];
            this.runtime.iterIndex(fvs, (fv, i) -> fieldAndValue[i] = fv);

            PyObject value = fieldAndValue[1];
            if (isinstance.call(builtins, value, list).isTrue()) {
                this.runtime.iter(value, item -> {
                    if (isinstance.call(builtins, value, ast).isTrue()) {
                        visit.call(self, self, item);
                    }
                });

            } else if (isinstance.call(builtins, value, ast).isTrue()) {
                visit.call(self, self, value);
            }
        });

        /*
        PyObject astModule = this.runtime.moduleOrThrow("ast");
        PyObject function = astModule.getObjectOrThrow("iter_fields");

        PyObject result = function.call(astModule, node);
        List<PyObject> list = result.getJavaObject(PyListType.JAVA_LIST_NAME)
                .map(l -> (List<PyObject>) l)
                .orElse(Collections.emptyList());

        PyObject astType = this.runtime.typeOrThrow("_ast.AST");

        for (PyObject fieldAndValue : list) {
            fieldAndValue.getJavaObject(PyTupleType.JAVA_LIST_NAME);
            PyObject field = fieldAndValue;
            PyObject value;

            PyObject listType = this.runtime.typeOrThrow("builtins.list");
            if (listType == fieldAndValue.getType()) {
                List<PyObject> value = fieldAndValue.getJavaObject(PyTupleType.JAVA_LIST_NAME)
                        .map(l -> (List<PyObject>) l)
                        .orElse(Collections.emptyList());

                for(PyObject item : value) {

                }

            } else if(result.getType() == astType) {
                self.call(fieldAndValue.);
            }
        }
        */
    }
}
