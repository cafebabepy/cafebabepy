package org.cafebabepy.parser;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.cafebabepy.parser.antlr.PythonParser;
import org.cafebabepy.parser.antlr.PythonParserBaseVisitor;
import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module._ast.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yotchang4s on 2017/05/29.
 */
// FIXME SyntaxError
class CafeBabePyAstCreateVisitor extends PythonParserBaseVisitor<PyObject> {

    private final Python runtime;

    CafeBabePyAstCreateVisitor(Python runtime) {
        this.runtime = runtime;
    }

    @Override
    public PyObject visitSingle_input(PythonParser.Single_inputContext ctx) {
        PyObject body;

        PythonParser.Simple_stmtContext simple_stmtContext = ctx.simple_stmt();
        PythonParser.Compound_stmtContext compound_stmtContext = ctx.compound_stmt();
        if (simple_stmtContext != null) {
            body = this.runtime.list(visitSimple_stmt(simple_stmtContext));

        } else if (compound_stmtContext != null) {
            body = this.runtime.list(visitCompound_stmt(compound_stmtContext));

        } else {
            body = this.runtime.list();
        }

        return this.runtime.newPyObject("_ast.Interactive", body);
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

        return this.runtime.newPyObject("_ast.Module", body);
    }

    @Override
    public PyObject visitStmt(PythonParser.StmtContext ctx) {
        PythonParser.Simple_stmtContext simple_stmtContext = ctx.simple_stmt();
        if (simple_stmtContext != null) {
            return visitSimple_stmt(simple_stmtContext);
        }

        PythonParser.Compound_stmtContext compound_stmtContext = ctx.compound_stmt();
        if (compound_stmtContext != null) {
            return visitCompound_stmt(compound_stmtContext);
        }

        throw this.runtime.newRaiseException("builtins.SyntaxError", "invalid syntax");
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
        PyObject testlist_star_expr = visitTestlist_star_expr(testlist_star_exprContextList.get(0));

        PythonParser.AnnassignContext annassignContext = ctx.annassign();
        if (annassignContext != null) {
            return createAnnasign(testlist_star_expr, annassignContext);

        } else if (testlist_star_exprContextList.size() >= 2) {
            return createAssign(testlist_star_exprContextList);
        }

        return this.runtime.newPyObject("_ast.Expr", testlist_star_expr);
    }

    private PyObject createAssign(List<PythonParser.Testlist_star_exprContext> testlist_star_exprContextList) {
        int count = testlist_star_exprContextList.size() - 1;
        PyObject[] targetArray = new PyObject[count];

        for (int i = 0; i < count; i++) {
            PythonParser.Testlist_star_exprContext testlist_star_exprContext = testlist_star_exprContextList.get(i);
            PyObject target = visitTestlist_star_expr(testlist_star_exprContext);
            toStore(target, 0);

            targetArray[i] = target;
        }

        PyObject targets = this.runtime.list(targetArray);

        PyObject value = visitTestlist_star_expr(
                testlist_star_exprContextList.get(testlist_star_exprContextList.size() - 1));

        return this.runtime.newPyObject("_ast.Assign", targets, value);
    }

    private void toStore(PyObject target, int attributeDepth) {
        PyObject type = target.getType();
        if (type instanceof PyStarredType) {
            target.getScope().put("ctx", this.runtime.newPyObject("_ast.Store"));
            PyObject value = this.runtime.getattr(target, "value");
            if (this.runtime.isIterable(value)) {
                this.runtime.iter(value, v -> toStore(v, 0));

            } else {
                toStore(value, 0);
            }

        } else if (type instanceof PyNameType) {
            if (attributeDepth == 0) {
                target.getScope().put("ctx", this.runtime.newPyObject("_ast.Store"));
            }

        } else if (type instanceof PyAttributeType) {
            if (attributeDepth == 0) {
                target.getScope().put("ctx", this.runtime.newPyObject("_ast.Store"));
            }

            PyObject value = this.runtime.getattr(target, "value");
            toStore(value, attributeDepth + 1);

        } else if (type instanceof PyListType) {
            target.getScope().put("ctx", this.runtime.newPyObject("_ast.Store"));
            PyObject elts = this.runtime.getattr(target, "elts");

            this.runtime.iter(elts, elt -> toStore(elt, 0));

        } else if (type instanceof PyNumType) {
            throw this.runtime.newRaiseException("builtins.SyntaxError",
                    "can't assign to literal");
        }
    }

    @SuppressWarnings("unchecked")
    private PyObject createAnnasign(PyObject testlist_star_expr, PythonParser.AnnassignContext annassignContext) {
        PyObject list = visitAnnassign(annassignContext);
        List<PyObject> testList = list.toJava(List.class);

        if (!this.runtime.isInstance(testlist_star_expr, "_ast.Name")) {
            throw this.runtime.newRaiseException("builtins.SyntaxError",
                    "illegal target for annotation");
        }
        testlist_star_expr.getScope().put("ctx", this.runtime.newPyObject("_ast.Store"));
        PyObject annotation = testList.get(0);
        PyObject value = this.runtime.None();
        if (testList.size() == 2) {
            value = testList.get(1);
        }
        // FIXME simple value fixed 1???
        PyObject simple = this.runtime.number(1);

        return this.runtime.newPyObject("_ast.Annassign", testlist_star_expr, annotation, value, simple);
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
    public PyObject visitImport_name(PythonParser.Import_nameContext ctx) {
        PyObject names = ctx.dotted_as_names().accept(this);

        return this.runtime.newPyObject("_ast.Import", names);
    }

    @Override
    public PyObject visitDotted_as_names(PythonParser.Dotted_as_namesContext ctx) {
        int count = ctx.dotted_as_name().size();
        List<PyObject> dottedAsNames = new ArrayList<>(count);

        for(int i = 0; i < count; i++) {
            PyObject dottedAsName = ctx.dotted_as_name(i).accept(this);
            dottedAsNames.add(dottedAsName);
        }

        return this.runtime.list(dottedAsNames);
    }

    @Override
    public PyObject visitDotted_as_name(PythonParser.Dotted_as_nameContext ctx) {
        PyObject name = ctx.dotted_name().accept(this);
        PyObject asName = this.runtime.None();

        if (ctx.NAME() != null) {
            asName = this.runtime.str(ctx.NAME().getSymbol().getText());
        }

        return this.runtime.newPyObject("_ast.alias", name, asName);
    }

    @Override
    public PyObject visitDotted_name(PythonParser.Dotted_nameContext ctx) {
        StringBuilder nameBuilder = new StringBuilder();

        nameBuilder.append(ctx.NAME().get(0).getSymbol().getText());

        int count = ctx.NAME().size();
        for (int i = 1; i < count; i++) {
            nameBuilder.append('.').append(ctx.NAME().get(i).getSymbol().getText());
        }

        return this.runtime.str(nameBuilder.toString());
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
    public PyObject visitPass_stmt(PythonParser.Pass_stmtContext ctx) {
        return this.runtime.newPyObject("_ast.Pass");
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
        for (PythonParser.TrailerContext trailerContext : trailerContextList) {
            PyObject trailer = visitTrailer(trailerContext);

            if (this.runtime.isInstance(trailer, "_ast.Call")) {
                if (expr == null) {
                    trailer.getScope().put("func", atom);

                } else {
                    trailer.getScope().put("func", expr);
                }

            } else if (this.runtime.isInstance(trailer, "_ast.Attribute")) {
                if (expr == null) {
                    trailer.getScope().put("value", atom);

                } else {
                    trailer.getScope().put("value", expr);
                }
            } else if (this.runtime.isInstance(trailer, "_ast.Subscript")) {
                if (expr == null) {
                    trailer.getScope().put("value", atom);

                } else {
                    trailer.getScope().put("value", expr);
                }
            }

            expr = trailer;
        }

        return expr;
    }

    @Override
    public PyObject visitAtom(PythonParser.AtomContext ctx) {
        TerminalNode nameNode = ctx.NAME();
        if (nameNode != null) {
            PyObject id = this.runtime.str(nameNode.getText());
            // Default
            PyObject load = this.runtime.newPyObject("_ast.Load");

            return this.runtime.newPyObject("_ast.Name", id, load);
        }
        TerminalNode trueNode = ctx.TRUE();
        if (trueNode != null) {
            return this.runtime.True();
        }
        TerminalNode falseNode = ctx.FALSE();
        if (falseNode != null) {
            return this.runtime.False();
        }
        TerminalNode noneNode = ctx.NONE();
        if (noneNode != null) {
            return this.runtime.None();
        }
        TerminalNode ellipsisNode = ctx.ELLIPSIS();
        if (ellipsisNode != null) {
            return this.runtime.Ellipsis();
        }
        List<PythonParser.StrContext> strContextList = ctx.str();
        if (!strContextList.isEmpty()) {
            PyObject str;
            if (strContextList.size() == 1) {
                str = visitStr(strContextList.get(0));

            } else {
                str = visitStr(strContextList.get(0));
                for (int i = 1; i < strContextList.size(); i++) {
                    str = this.runtime.add(str, visitStr(strContextList.get(i)));
                }
            }

            return this.runtime.newPyObject("_ast.Str", str);
        }

        String open = ctx.getChild(0).getText();
        String close = ctx.getChild(ctx.getChildCount() - 1).getText();

        if ("[".equals(open)) {
            if (!"]".equals(close)) {
                throw this.runtime.newRaiseException("builtins.SyntaxError", "invalid syntax");
            }

            if (ctx.yield_expr() != null) {
                return visitYield_expr(ctx.yield_expr());

            } else if (ctx.testlist_comp() != null) {
                PyObject testlist_comp = visitTestlist_comp(ctx.testlist_comp());
                if (this.runtime.isInstance(testlist_comp, "_ast.ListComp")) {
                    return testlist_comp;

                } else {
                    PyObject load = this.runtime.newPyObject("_ast.Load");
                    return this.runtime.newPyObject("_ast.List", testlist_comp, load);
                }

            } else {
                PyObject load = this.runtime.newPyObject("_ast.Load");
                return this.runtime.newPyObject("_ast.List", this.runtime.list(), load);
            }

        } else if ("(".equals(open)) {
            if (!")".equals(close)) {
                throw this.runtime.newRaiseException("builtins.SyntaxError", "invalid syntax");
            }

            PyObject testlist_comp = visitTestlist_comp(ctx.testlist_comp());
            if (this.runtime.isInstance(testlist_comp, "_ast.ListComp")) {
                PyObject elt = this.runtime.getattr(testlist_comp, "elt");
                PyObject generators = this.runtime.getattr(testlist_comp, "generators");

                return this.runtime.newPyObject("_ast.GeneratorExp", elt, generators);

            } else {
                PyObject load = this.runtime.newPyObject("_ast.Load");
                return this.runtime.newPyObject("_ast.Tuple", testlist_comp, load);
            }

        } else if ("{".equals(open)) {
            if (!"}".equals(close)) {
                throw this.runtime.newRaiseException("builtins.SyntaxError", "invalid syntax");
            }

            // dict or set
            return visitDictorsetmaker(ctx.dictorsetmaker());

        }

        return super.visitAtom(ctx);
    }

    @Override
    public PyObject visitExprlist(PythonParser.ExprlistContext ctx) {
        return visitTupleExpr(ctx, ctx.expr().size() + ctx.star_expr().size(), ctx.COMMA().size());
    }

    @Override
    public PyObject visitDictorsetmaker(PythonParser.DictorsetmakerContext ctx) {
        if (ctx != null) {
            if (ctx.comp_for() == null) {
                List<PyObject> keys = new ArrayList<>();
                List<PyObject> values = new ArrayList<>();

                boolean doubleStar = false;
                PyObject test = null;
                int count = ctx.getChildCount();
                for (int i = 0; i < count; i++) {
                    ParseTree c = ctx.getChild(i);
                    if ("**".equals(c.getText())) {
                        doubleStar = true;

                    } else {
                        if (doubleStar) {
                            keys.add(this.runtime.None());
                            values.add(c.accept(this));

                        } else {
                            PyObject element = c.accept(this);
                            if (element != null) {
                                if (test == null) {
                                    test = element;

                                } else {
                                    keys.add(test);
                                    values.add(element);

                                    test = null;
                                }
                            }
                        }
                        doubleStar = false;
                    }
                }

                return this.runtime.newPyObject("_ast.Dict", this.runtime.list(keys), this.runtime.list(values));
            }
        }
        return this.runtime.newPyObject("_ast.Dict", this.runtime.list(), this.runtime.list());
    }

    @Override
    public PyObject visitTestlist_star_expr(PythonParser.Testlist_star_exprContext ctx) {
        return visitTupleExpr(ctx, ctx.test().size() + ctx.star_expr().size(), ctx.COMMA().size());
    }

    @Override
    public PyObject visitTestlist_comp(PythonParser.Testlist_compContext ctx) {
        PythonParser.Comp_forContext comp_forContext = ctx.comp_for();
        if (comp_forContext == null) {
            if (ctx.test().size() + ctx.star_expr().size() == 1) {
                PyObject element = ctx.getChild(0).accept(this);
                return this.runtime.list(element);
            }

            List<PyObject> elements = new ArrayList<>(ctx.test().size() + ctx.star_expr().size());
            int count = ctx.getChildCount();
            for (int i = 0; i < count; i++) {
                ParseTree parseTree = ctx.getChild(i);
                if (parseTree != null) {
                    PyObject element = parseTree.accept(this);
                    if (element != null) {
                        elements.add(element);
                    }
                }
            }
            return this.runtime.list(elements);

        } else {
            PyObject elt = ctx.getChild(0).accept(this);
            PyObject comp_for = visitComp_for(comp_forContext);

            // ここだとまだ何の内包表記かわからないので一時的にListCompにする
            return this.runtime.newPyObject("_ast.ListComp", elt, comp_for);
        }
    }

    private PyObject visitTupleExpr(ParserRuleContext ctx, int exprSize, int commaSize) {
        if (exprSize == 1) {
            PyObject element = ctx.getChild(0).accept(this);
            if (commaSize == 0) {
                return element;

            } else {
                PyObject load = this.runtime.newPyObject("_ast.Load");
                return this.runtime.newPyObject("_ast.Tuple", this.runtime.list(element), load);
            }
        }

        List<PyObject> exprList = new ArrayList<>(commaSize);
        int count = ctx.getChildCount();
        for (int i = 0; i < count; i++) {
            PyObject element = ctx.getChild(i).accept(this);
            if (element == null) {
                continue;
            }

            exprList.add(element);
        }

        PyObject load = this.runtime.newPyObject("_ast.Load");
        return this.runtime.newPyObject("_ast.Tuple", this.runtime.list(exprList), load);
    }

    @Override
    public PyObject visitTrailer(PythonParser.TrailerContext ctx) {
        if (ctx.NAME() != null) {
            PyObject attr = this.runtime.str(ctx.NAME().getText());
            PyObject load = this.runtime.newPyObject("_ast.Load");

            return this.runtime.newPyObject("_ast.Attribute", this.runtime.None(), attr, load);
        }

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

        } else if ("[".equals(firstText) && "]".equals(lastText)) {
            PythonParser.SubscriptlistContext subscriptlistContext = ctx.subscriptlist();
            if (subscriptlistContext == null) {
                throw this.runtime.newRaiseException("builtins.SyntaxError", "sub script list not found");
            }
            PyObject subscriptlist = visitSubscriptlist(subscriptlistContext);

            // Default
            PyObject load = this.runtime.newPyObject("_ast.Load");

            return this.runtime.newPyObject("_ast.Subscript", this.runtime.None(), subscriptlist, load);

        } else {
            throw this.runtime.newRaiseException("builtins.SyntaxError", "Invalid ast");
        }
    }

    @Override
    public PyObject visitSubscriptlist(PythonParser.SubscriptlistContext ctx) {
        PyObject expr = visitTupleExpr(ctx, ctx.subscript().size(), ctx.COMMA().size());

        int[] sliceCount = new int[1];
        sliceCount[0] = 0;

        if (this.runtime.isInstance(expr, "builtins.tuple")) {
            this.runtime.iter(expr, x -> sliceCount[0]++);
        }

        if (sliceCount[0] == 0) {
            return this.runtime.newPyObject("_ast.Index", expr);

        } else {
            throw new CafeBabePyException("Not implements");
        }
    }

    @Override
    public PyObject visitSubscript(PythonParser.SubscriptContext ctx) {
        if (ctx.COLON() != null) {
            PyObject lower = this.runtime.None();
            PyObject upper = this.runtime.None();
            PyObject step = this.runtime.None();

            int colonCount = 0;
            int count = ctx.getChildCount();
            if (ctx.sliceop() != null) {
                count--;
            }

            for (int i = 0; i < count; i++) {
                ParseTree tree = ctx.getChild(i);
                PyObject element = tree.accept(this);
                if (element != null) {
                    if (colonCount == 0) {
                        lower = element;

                    } else if (colonCount == 1) {
                        upper = element;
                    }

                } else if (":".equals(tree.getText())) {
                    colonCount++;
                }
            }

            if (ctx.sliceop() != null) {
                step = ctx.sliceop().accept(this);
            }

            return this.runtime.newPyObject("_ast.Slice", lower, upper, step);

        } else {
            return ctx.test(0).accept(this);
        }
    }

    @Override
    public PyObject visitSliceop(PythonParser.SliceopContext ctx) {
        if (ctx.test() != null) {
            return ctx.test().accept(this);

        } else {
            return this.runtime.None();
        }
    }

    @Override
    public PyObject visitClassdef(PythonParser.ClassdefContext ctx) {
        PyObject name = this.runtime.str(ctx.NAME().getText());

        PythonParser.ArglistContext arglistContext = ctx.arglist();
        PyObject bases;
        if (arglistContext != null) {
            bases = visitArglist(arglistContext);

        } else {
            bases = this.runtime.list();
        }

        // TODO 何これ？
        PyObject keywords = this.runtime.list();

        PythonParser.SuiteContext suiteContext = ctx.suite();
        PyObject body = visitSuite(suiteContext);

        // TODO 何これ？
        PyObject decorator_list = this.runtime.None();

        return this.runtime.newPyObject("_ast.ClassDef",
                name, bases, keywords, body, decorator_list);
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
            for (PyObject e : comp_iterList) {
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

        return this.runtime.newPyObject("_ast.Num", number);
    }

    @Override
    public PyObject visitStr(PythonParser.StrContext ctx) {
        String rawString = ctx.getText();
        int firstQuoteIndex = rawString.indexOf('\'');
        if (firstQuoteIndex == -1) {
            firstQuoteIndex = rawString.indexOf('"');
        }

        // TODO prefix

        return this.runtime.str(rawString.substring(firstQuoteIndex + 1, rawString.length() - 1));
    }
}
