package org.cafebabepy.parser;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module._ast.PyListType;
import org.cafebabepy.runtime.module._ast.PyNameType;
import org.cafebabepy.runtime.module._ast.PyNumType;
import org.cafebabepy.runtime.module._ast.PyStarredType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yotchang4s on 2017/05/29.
 */
// FIXME SyntaxError
class CafeBabePyAstCreateVisitor extends PythonBaseVisitor<PyObject> {

    private final Python runtime;

    public CafeBabePyAstCreateVisitor(Python runtime) {
        this.runtime = runtime;
    }

    @Override
    public PyObject visitFile_input(PythonParser.File_inputContext ctx) {
        List<PyObject> bodyList = new ArrayList<>();
        for (PythonParser.StmtContext stmtContext : ctx.stmt()) {
            PyObject body = visitStmt(stmtContext);
            if (this.runtime.isInstance(body, "builtins.list")) {
                this.runtime.iter(body, bodyList::add);

            } else {
                bodyList.add(body);
            }
        }
        PyObject body = this.runtime.list(bodyList);
        PyObject module = this.runtime.newPyObject("_ast.Module", body);

        return module;
    }

    @Override
    public PyObject visitSimple_stmt(PythonParser.Simple_stmtContext ctx) {
        List<PyObject> small_stmtList = new ArrayList<>();
        for (PythonParser.Small_stmtContext small_stmtContext : ctx.small_stmt()) {
            PyObject small_stmt = visitSmall_stmt(small_stmtContext);
            small_stmtList.add(small_stmt);
        }

        return this.runtime.list(small_stmtList);
    }

    @Override
    public PyObject visitExpr_stmt(PythonParser.Expr_stmtContext ctx) {
        List<PythonParser.Testlist_star_exprContext> testlist_star_exprContextList = ctx.testlist_star_expr();

        PythonParser.AnnassignContext annassignContext = ctx.annassign();
        if (annassignContext != null) {
            PyObject testlist_star_expr = visitTestlist_star_expr(testlist_star_exprContextList.get(0));

            return createAnnasign(testlist_star_expr, annassignContext);

        } else if (testlist_star_exprContextList.size() >= 2) {
            return createAssign(testlist_star_exprContextList);
        }

        PyObject children = visitChildren(ctx);
        PyObject expr_stmt = this.runtime.newPyObject("_ast.Expr", children);

        return expr_stmt;
    }

    private PyObject createAssign(List<PythonParser.Testlist_star_exprContext> testlist_star_exprContextList) {
        int count = testlist_star_exprContextList.size() - 1;
        PyObject[] targetArray = new PyObject[count];

        for (int i = 0; i < count; i++) {
            PythonParser.Testlist_star_exprContext testlist_star_exprContext = testlist_star_exprContextList.get(i);
            PyObject target = visitTestlist_star_expr(testlist_star_exprContext);
            toStore(target);

            targetArray[i] = target;
        }

        PyObject targets = this.runtime.list(targetArray);

        PyObject value = visitTestlist_star_expr(
                testlist_star_exprContextList.get(testlist_star_exprContextList.size() - 1));

        return this.runtime.newPyObject("_ast.Assign", targets, value);
    }

    private void toStore(PyObject target) {
        PyObject type = target.getType();
        if (type instanceof PyStarredType) {
            target.getScope().put("ctx", this.runtime.newPyObject("_ast.Store"));
            PyObject value = target.getObjectOrThrow("value");
            if (this.runtime.isIterable(value)) {
                this.runtime.iter(target, this::toStore);
            }

        } else if (type instanceof PyNameType) {
            target.getScope().put("ctx", this.runtime.newPyObject("_ast.Store"));

        } else if (type instanceof PyListType) {
            target.getScope().put("ctx", this.runtime.newPyObject("_ast.Store"));
            PyObject elts = target.getObjectOrThrow("elts");

            this.runtime.iter(elts, this::toStore);

        } else if (type instanceof PyNumType) {
            throw this.runtime.newRaiseException("builtins.SyntaxError",
                    "can't assign to literal");
        }
    }

    private PyObject createAnnasign(PyObject testlist_star_expr, PythonParser.AnnassignContext annassignContext) {
        PyObject list = visitAnnassign(annassignContext);
        List<PyObject> testList = this.runtime.toList(list);

        PyObject target = testlist_star_expr;
        if (!this.runtime.isInstance(target, "_ast.Name")) {
            throw this.runtime.newRaiseException("builtins.SyntaxError",
                    "illegal target for annotation");
        }
        target.getScope().put("ctx", this.runtime.newPyObject("_ast.Store"));
        PyObject annotation = testList.get(0);
        PyObject value = this.runtime.None();
        if (testList.size() == 2) {
            value = testList.get(1);
        }
        // FIXME simple value fixed 1???
        PyObject simple = this.runtime.number(1);

        return this.runtime.newPyObject("_ast.Annassign", target, annotation, value, simple);
    }

    @Override
    public PyObject visitFuncdef(PythonParser.FuncdefContext ctx) {
        PyObject name = this.runtime.str(ctx.NAME().getText());
        PyObject args = this.runtime.None();
        PythonParser.ParametersContext parametersContext = ctx.parameters();
        if (parametersContext != null) {
            args = visitParameters(parametersContext);
        }
        PyObject body = visitSuite(ctx.suite());
        PyObject decorator_list = this.runtime.None(); // FIXME 未実装
        PyObject returns = this.runtime.None();// 無視

        PythonParser.TestContext testContext = ctx.test();
        if (testContext != null) {
            returns = visitTest(testContext);
        }

        return this.runtime.newPyObject("_ast.FunctionDef", name, args, body, decorator_list, returns);
    }

    @Override
    public PyObject visitParameters(PythonParser.ParametersContext ctx) {
        PythonParser.TypedargslistContext typedargslistContext = ctx.typedargslist();
        if (typedargslistContext == null) {
            return this.runtime.newPyObject("_ast.arguments");

        } else {
            return visitTypedargslist(typedargslistContext);
        }
    }

    @Override
    public PyObject visitTypedargslist(PythonParser.TypedargslistContext ctx) {
        List<PythonParser.TfpdefContext> tfpdefContextList = ctx.tfpdef();
        PyObject[] argArray = new PyObject[tfpdefContextList.size()];

        for (int i = 0; i < argArray.length; i++) {
            argArray[i] = visitTfpdef(tfpdefContextList.get(i));
        }

        PyObject args = this.runtime.list(argArray);
        PyObject vararg = this.runtime.None();
        PyObject kwonlyargs = this.runtime.None();
        PyObject kw_defaults = this.runtime.None();
        PyObject kwarg = this.runtime.None();
        PyObject defaults = this.runtime.None();

        return this.runtime.newPyObject(
                "_ast.arguments", args, vararg, kwonlyargs, kw_defaults, kwarg, defaults);
    }

    @Override
    public PyObject visitTfpdef(PythonParser.TfpdefContext ctx) {
        PyObject arg = this.runtime.str(ctx.NAME().getText());
        PyObject annotation = this.runtime.None();

        PythonParser.TestContext testContext = ctx.test();
        if (testContext != null) {
            annotation = visitTest(testContext);
        }

        return this.runtime.newPyObject("_ast.arg", arg, annotation);
    }

    @Override
    public PyObject visitAnnassign(PythonParser.AnnassignContext ctx) {
        List<PythonParser.TestContext> testContextList = ctx.test();
        PyObject[] array = new PyObject[testContextList.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = visitTest(testContextList.get(i));
        }

        if (array.length != 1 && array.length != 2) {
            throw this.runtime.newRaiseException("builtins.SyntaxError",
                    "Annassign test is invalid count " + array.length);
        }

        return this.runtime.list(array);
    }

    @Override
    public PyObject visitIf_stmt(PythonParser.If_stmtContext ctx) {
        PyObject test = null;
        PyObject body = null;
        PyObject orElse = this.runtime.None();

        List<PythonParser.TestContext> testContextList = ctx.test();
        List<PythonParser.SuiteContext> suiteContextList = ctx.suite();
        int testIndex = testContextList.size() - 1;
        int suiteIndex = suiteContextList.size() - 1;
        int count = ctx.getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            String name = ctx.getChild(i).getText();
            switch (name) {
                case "if":
                    test = visitTest(testContextList.get(testIndex));
                    body = visitSuite(suiteContextList.get(suiteIndex));

                    testIndex--;
                    suiteIndex--;
                    break;

                case "elif":
                    PyObject elifTest = visitTest(testContextList.get(testIndex));
                    PyObject elifBody = visitSuite(suiteContextList.get(suiteIndex));
                    orElse = this.runtime.newPyObject("_ast.If", elifTest, elifBody, orElse);

                    testIndex--;
                    suiteIndex--;
                    break;

                case "else":
                    orElse = visitSuite(suiteContextList.get(suiteIndex));

                    suiteIndex--;
                    break;
            }
        }

        return this.runtime.newPyObject("_ast.If", test, body, orElse);
    }

    @Override
    public PyObject visitFor_stmt(PythonParser.For_stmtContext ctx) {
        PyObject target = visitExprlist(ctx.exprlist());
        PyObject iter = visitTestlist(ctx.testlist());
        PyObject body;
        PyObject orelse = this.runtime.None();

        List<PythonParser.SuiteContext> suiteContextList = ctx.suite();
        body = visitSuite(suiteContextList.get(0));
        if (suiteContextList.size() == 2) {
            orelse = visitSuite(suiteContextList.get(1));
        }

        return this.runtime.newPyObject("_ast.For", target, iter, body, orelse);
    }

    @Override
    public PyObject visitTest(PythonParser.TestContext ctx) {
        List<PythonParser.Or_testContext> or_testContext = ctx.or_test();

        PyObject test = visitOr_test(or_testContext.get(0));
        if (or_testContext.size() == 2) {
            PyObject ifTest = visitOr_test(or_testContext.get(1));
            PyObject elseTest = visitTest(ctx.test());

            return this.runtime.newPyObject("_ast.IfExp", ifTest, test, elseTest);

        } else {
            return test;
        }
    }

    @Override
    public PyObject visitNot_test(PythonParser.Not_testContext ctx) {
        PythonParser.ComparisonContext comparisonContext = ctx.comparison();
        if (comparisonContext != null) {
            return visitComparison(comparisonContext);

        } else {
            PyObject notTest = visitNot_test(ctx.not_test());

            PyObject op = this.runtime.newPyObject("_ast.Not");
            return this.runtime.newPyObject("_ast.UnaryOp", op, notTest);
        }
    }

    @Override
    public PyObject visitStar_expr(PythonParser.Star_exprContext ctx) {
        PythonParser.ExprContext exprContext = ctx.expr();
        PyObject value = visitExpr(exprContext);
        PyObject context = this.runtime.newPyObject("_ast.Load");

        return this.runtime.newPyObject("_ast.Starred", value, context);
    }

    @Override
    public PyObject visitSuite(PythonParser.SuiteContext ctx) {
        PythonParser.Simple_stmtContext simple_stmtContext = ctx.simple_stmt();
        if (simple_stmtContext != null) {
            PyObject body = visitSimple_stmt(simple_stmtContext);

            return this.runtime.newPyObject("_ast.Suite", body);
        } else {
            List<PyObject> stmtList = new ArrayList<>();
            for (PythonParser.StmtContext stmtContext : ctx.stmt()) {
                PyObject stmt = visitStmt(stmtContext);
                stmtList.add(stmt);
            }

            return this.runtime.list(stmtList);
        }
    }

    @Override
    public PyObject visitReturn_stmt(PythonParser.Return_stmtContext ctx) {
        PyObject value = this.runtime.None();

        PythonParser.TestlistContext testlistContext = ctx.testlist();
        if (testlistContext != null) {
            value = visitTestlist(testlistContext);
        }

        return this.runtime.newPyObject("_ast.Return", value);
    }

    @Override
    public PyObject visitComparison(PythonParser.ComparisonContext ctx) {
        int count = ctx.getChildCount();
        if (count == 1) {
            return visitExpr(ctx.expr(0));
        }

        List<PythonParser.ExprContext> exprContextList = ctx.expr();

        List<PyObject> comparatorList = new ArrayList<>(exprContextList.size());
        for (PythonParser.ExprContext exprContext : exprContextList) {
            PyObject comparator = visitExpr(exprContext);
            comparatorList.add(comparator);
        }

        List<PythonParser.Comp_opContext> comp_opContextList = ctx.comp_op();

        List<PyObject> opList = new ArrayList<>(comp_opContextList.size());
        for (PythonParser.Comp_opContext comp_opContext : comp_opContextList) {
            PyObject op = visitComp_op(comp_opContext);
            opList.add(op);
        }

        if (comparatorList.size() == 1) {
            return comparatorList.get(0);
        }

        PyObject left = comparatorList.get(0);
        comparatorList.remove(0);

        PyObject ops = this.runtime.list(opList);
        PyObject comparators = this.runtime.list(comparatorList);

        return this.runtime.newPyObject("_ast.Compare", left, ops, comparators);
    }

    @Override
    public PyObject visitComp_op(PythonParser.Comp_opContext ctx) {
        String op = ctx.getText();
        switch (op) {
            case "<":
                return this.runtime.newPyObject("_ast.Lt");

            case "<=":
                return this.runtime.newPyObject("_ast.LtE");

            case ">":
                return this.runtime.newPyObject("_ast.Gt");

            case ">=":
                return this.runtime.newPyObject("_ast.GtE");

            case "==":
                return this.runtime.newPyObject("_ast.Eq");

            case "!=":
                return this.runtime.newPyObject("_ast.NotEq");

            default:
                throw this.runtime.newRaiseException("builtins.SyntaxError", "op '" + op + "' is not found");
        }
    }

    @Override
    public PyObject visitArith_expr(PythonParser.Arith_exprContext ctx) {
        List<PythonParser.TermContext> termContextList = ctx.term();

        int termIndex = 0;
        PyObject term = visitTerm(termContextList.get(termIndex));
        termIndex++;

        int count = ctx.getChildCount();
        for (int i = 1; i < count; i += 2) {
            String op = ctx.getChild(i).getText();
            PyObject operator;
            switch (op) {
                case "+":
                    operator = this.runtime.newPyObject("_ast.Add");
                    break;

                case "-":
                    operator = this.runtime.newPyObject("_ast.Sub");
                    break;

                default:
                    throw this.runtime.newRaiseException("builtins.SyntaxError",
                            "op '" + op + "' is not found");
            }

            PyObject rightTerm = visitTerm(termContextList.get(termIndex));
            termIndex++;

            term = this.runtime.newPyObject("_ast.BinOp", term, operator, rightTerm);
        }

        return term;
    }

    @Override
    public PyObject visitTerm(PythonParser.TermContext ctx) {
        List<PythonParser.FactorContext> factorContextList = ctx.factor();

        int factorIndex = 0;
        PyObject factor = visitFactor(factorContextList.get(factorIndex));
        factorIndex++;

        int count = ctx.getChildCount();
        for (int i = 1; i < count; i += 2) {
            String op = ctx.getChild(i).getText();
            PyObject operator;
            switch (op) {
                case "*":
                    operator = this.runtime.newPyObject("_ast.Mult");
                    break;

                case "%":
                    operator = this.runtime.newPyObject("_ast.Mod");
                    break;

                default:
                    throw this.runtime.newRaiseException("builtins.SyntaxError", "op '" + op + "' is not found");
            }

            PyObject rightFactor = visitFactor(factorContextList.get(factorIndex));
            factorIndex++;

            factor = this.runtime.newPyObject("_ast.BinOp", factor, operator, rightFactor);
        }

        return factor;
    }

    @Override
    public PyObject visitFactor(PythonParser.FactorContext ctx) {
        PythonParser.PowerContext powerContext = ctx.power();
        if (powerContext != null) {
            return visitPower(powerContext);

        } else {
            PythonParser.FactorContext factorContext = ctx.factor();
            PyObject factor = visitFactor(factorContext);

            PyObject op;

            switch (ctx.getChild(0).getText()) {
                case "-":
                    op = this.runtime.newPyObject("_ast.USub");
                    break;

                case "+":
                    op = this.runtime.newPyObject("_ast.UAdd");
                    break;

                case "~":
                    op = this.runtime.newPyObject("_ast.Invert");
                    break;

                default:
                    throw this.runtime.newRaiseException("builtins.SyntaxError", "Invalid factor");
            }

            return this.runtime.newPyObject("_ast.UnaryOp", op, factor);
        }
    }

    @Override
    public PyObject visitAtom_expr(PythonParser.Atom_exprContext ctx) {
        PythonParser.AtomContext atomContext = ctx.atom();
        PyObject atom = visitAtom(atomContext);

        List<PythonParser.TrailerContext> trailerContextList = ctx.trailer();
        int count = trailerContextList.size();
        if (count == 0) {
            return atom;
        }

        PyObject expr = null;
        for (int i = 0; i < count; i++) {
            PythonParser.TrailerContext trailerContext = trailerContextList.get(i);
            PyObject trailer = visitTrailer(trailerContext);

            if (this.runtime.isInstance(trailer, "_ast.Call")) {
                if (expr == null) {
                    trailer.getScope().put("func", atom);
                    expr = trailer;

                } else {
                    trailer.getScope().put("func", expr);
                }
            }
        }

        return expr;
    }

    @Override
    public PyObject visitAtom(PythonParser.AtomContext ctx) {
        TerminalNode name = ctx.NAME();
        if (name != null) {
            PyObject id = this.runtime.str(name.getText());
            // Default
            PyObject load = this.runtime.newPyObject("_ast.Load");

            return this.runtime.newPyObject("_ast.Name", id, load);
        }
        List<PythonParser.StrContext> strContextList = ctx.str();
        if (!strContextList.isEmpty()) {
            PyObject str;
            if (strContextList.size() == 1) {
                str = visitStr(strContextList.get(0));

            } else {
                str = visitStr(strContextList.get(0));
                for (int i = 1; i < strContextList.size(); i++) {
                    PyObject rightStr = visitStr(strContextList.get(i));
                    str = this.runtime.add(str, rightStr);
                }
            }

            return this.runtime.newPyObject("_ast.Str", str);
        }

        String open = ctx.getChild(0).getText();
        String close = ctx.getChild(ctx.getChildCount() - 1).getText();

        if ("[".equals(open) && "]".equals(close)) {
            // list
            return visitList(ctx.testlist_comp());

        } else if ("(".equals(open) && ")".equals(close)) {
            // tuple and ()
            return visitTuple(ctx.testlist_comp());
        }

        return super.visitAtom(ctx);
    }

    private PyObject visitList(PythonParser.Testlist_compContext testlist_compContext) {
        if (testlist_compContext != null) {
            PyObject testList_comp = visitTestlist_comp(testlist_compContext);
            return visitAtomToTestlist_comp(testList_comp, "_ast.ListComp", "_ast.List");

        } else {
            PyObject load = this.runtime.newPyObject("_ast.Load");
            return this.runtime.newPyObject("_ast.List", this.runtime.list(), load);
        }
    }

    private PyObject visitTuple(PythonParser.Testlist_compContext testlist_compContext) {
        if (testlist_compContext != null) {
            PyObject resultVisit = visitTestlist_comp(testlist_compContext);
            List<PyObject> resultVisitList = this.runtime.toList(resultVisit);

            if (resultVisitList.size() == 1) {
                return resultVisitList.get(0);

            } else {
                return visitAtomToTestlist_comp(resultVisit, "_ast.GeneratorExp", "_ast.Tuple");
            }

        } else {
            PyObject load = this.runtime.newPyObject("_ast.Load");
            return this.runtime.newPyObject("_ast.Tuple", this.runtime.list(), load);
        }
    }

    private PyObject visitAtomToTestlist_comp(PyObject testList_Comp,
                                              String comp, String structure) {
        PyObject load = this.runtime.newPyObject("_ast.Load");

        List<PyObject> resultVisitList = this.runtime.toList(testList_Comp);
        int comprehensionCount = 0;
        PyObject type = this.runtime.typeOrThrow("_ast.comprehension");
        for (int i = 1; i < resultVisitList.size(); i++) {
            if (this.runtime.isInstance(resultVisitList.get(i), type)) {
                comprehensionCount++;
            }
        }
        if (0 < comprehensionCount) {
            if (resultVisitList.size() - 1 != comprehensionCount) {
                throw this.runtime.newRaiseException("builtins.SyntaxError", "Invalid comprehension");
            }
            PyObject elt = resultVisitList.get(0);
            PyObject generators = this.runtime.list(resultVisitList.subList(1, resultVisitList.size()));
            return this.runtime.newPyObject(comp, elt, generators);

        } else {
            return this.runtime.newPyObject(structure, testList_Comp, load);
        }
    }

    @Override
    public PyObject visitTestlist_comp(PythonParser.Testlist_compContext ctx) {
        List<PyObject> list = new ArrayList<>();

        PythonParser.Comp_forContext comp_forContext = ctx.comp_for();
        if (comp_forContext == null) {
            int count = ctx.getChildCount();
            for (int i = 0; i < count; i++) {
                ParseTree c = ctx.getChild(i);
                PyObject element = c.accept(this);
                if (element != null) {
                    list.add(element);
                }
            }

        } else {
            list.add(ctx.getChild(0).accept(this));

            PyObject comp_for = visitComp_for(comp_forContext);
            this.runtime.iter(comp_for, list::add);
        }

        return this.runtime.list(list);
    }

    @Override
    public PyObject visitTrailer(PythonParser.TrailerContext ctx) {
        String firstText = ctx.getChild(0).getText();
        String lastText = ctx.getChild(ctx.getChildCount() - 1).getText();

        if ("(".equals(firstText) && ")".equals(lastText)) {
            PythonParser.ArglistContext arglistContext = ctx.arglist();
            PyObject arglist = this.runtime.None();
            if (arglistContext != null) {
                arglist = visitArglist(arglistContext);
            }

            // FIXME keywords
            return this.runtime.newPyObject("_ast.Call", this.runtime.None(), arglist, this.runtime.None());
        }

        throw this.runtime.newRaiseException("builtins.SyntaxError", "Invalid ast");
    }

    @Override
    public PyObject visitArglist(PythonParser.ArglistContext ctx) {
        List<PythonParser.ArgumentContext> argumentContextList = ctx.argument();

        PyObject[] argumentArray = new PyObject[argumentContextList.size()];
        for (int i = 0; i < argumentArray.length; i++) {
            argumentArray[i] = visitArgument(argumentContextList.get(i));
        }

        return this.runtime.list(argumentArray);
    }

    @Override
    public PyObject visitComp_for(PythonParser.Comp_forContext ctx) {
        PyObject target = visitExprlist(ctx.exprlist());
        PyObject iter = visitOr_test(ctx.or_test());
        PyObject ifs = this.runtime.None();
        PyObject is_async = this.runtime.None();

        List<PyObject> comp_iterList = new ArrayList<>();
        PyObject comprehension = this.runtime.newPyObject("_ast.comprehension", target, iter, ifs, is_async);

        PythonParser.Comp_iterContext comp_iterContext = ctx.comp_iter();
        if (comp_iterContext != null) {
            List<PyObject> comprehensionList = new ArrayList<>();
            List<PyObject> ifsList = new ArrayList<>();

            PyObject comp_iter = visitComp_iter(comp_iterContext);
            this.runtime.iter(comp_iter, comp_iterList::add);

            comprehensionList.add(comprehension);

            PyObject type = this.runtime.typeOrThrow("_ast.comprehension");
            for (int i = 0; i < comp_iterList.size(); i++) {
                PyObject e = comp_iterList.get(i);
                if (!this.runtime.isInstance(e, type)) {
                    ifsList.add(e);

                } else {
                    comprehensionList.add(e);
                }
            }

            ifs = this.runtime.list(ifsList);
            comprehension.getScope().put("ifs", ifs);

            return this.runtime.list(comprehensionList);

        } else {
            return this.runtime.list(comprehension);
        }
    }

    @Override
    public PyObject visitComp_if(PythonParser.Comp_ifContext ctx) {
        PyObject test_nocond = visitTest_nocond(ctx.test_nocond());
        PythonParser.Comp_iterContext comp_iterContext = ctx.comp_iter();
        if (comp_iterContext != null) {
            List<PyObject> comp_iterList = new ArrayList<>();

            PyObject comp_iter = visitComp_iter(comp_iterContext);

            comp_iterList.add(test_nocond);
            this.runtime.iter(comp_iter, comp_iterList::add);

            return this.runtime.list(comp_iterList);

        } else {
            return this.runtime.list(test_nocond);
        }
    }

    @Override
    public PyObject visitNumber(PythonParser.NumberContext ctx) {
        String text = ctx.getChild(0).getText();
        PyObject number = this.runtime.number(Integer.parseInt(text));
        PyObject object = this.runtime.newPyObject("_ast.Num", number);

        return object;
    }

    @Override
    public PyObject visitStr(PythonParser.StrContext ctx) {
        return this.runtime.str(ctx.getText());
    }
}
