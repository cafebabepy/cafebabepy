package org.cafebabepy.runtime.util;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;

import java.util.Optional;

public abstract class ASTNodeVisitor {
    protected final Python runtime;

    public ASTNodeVisitor(Python runtime) {
        this.runtime = runtime;
    }

    protected void visit(PyObject node) {
        Optional<PyObject> _fieldsOpt = this.runtime.getattrOptional(node, "_fields");
        if (!_fieldsOpt.isPresent()) {
            if (this.runtime.isIterable(node)) {
                this.runtime.iter(node, this::visit);
            }

            return;
        }

        PyObject _fields = _fieldsOpt.get();

        this.runtime.iter(_fields, field -> {
            PyObject value = this.runtime.getattr(node, field.toJava(String.class));
            if (this.runtime.isInstance(value, "list")) {
                this.runtime.iter(value, item -> {
                    if (this.runtime.isInstance(item, "_ast.AST")) {
                        visit(item);
                    }
                });

            } else if (this.runtime.isInstance(value, "_ast.AST")) {
                visit(value);
            }
        });
    }
}
