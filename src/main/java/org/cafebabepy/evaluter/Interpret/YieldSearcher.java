package org.cafebabepy.evaluter.Interpret;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.util.ASTNodeVisitor;

import java.util.ArrayList;
import java.util.List;

class YieldSearcher extends ASTNodeVisitor {

    private List<PyObject> yields = new ArrayList<>();

    YieldSearcher(Python runtime) {
        super(runtime);
    }

    List<PyObject> get(PyObject node) {
        visit(node);

        List<PyObject> yields = this.yields;
        this.yields = new ArrayList<>();

        return yields;
    }

    @Override
    protected void visit(PyObject node) {
        if (this.runtime.isInstance(node, "_ast.Yield")) {
            this.yields.add(node);
        }

        super.visit(node);
    }
}
