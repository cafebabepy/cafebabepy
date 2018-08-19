package org.cafebabepy.evaluter.Interpret;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.util.ASTNodeVisitor;

class YieldSearcher extends ASTNodeVisitor {

    YieldSearcher(Python runtime) {
        super(runtime);
    }

    public boolean search(PyObject node) {
        try {
            visit(node);
            return false;

        } catch (VisitStop ignore) {
            return true;
        }
    }

    @Override
    protected void visit(PyObject node) {
        if (this.runtime.isInstance(node, "_ast.Yield")) {
            throw new VisitStop();
        }

        super.visit(node);
    }

    private static class VisitStop extends RuntimeException {
    }
}
