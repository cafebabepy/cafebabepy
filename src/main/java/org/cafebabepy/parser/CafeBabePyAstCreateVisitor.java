package org.cafebabepy.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.cafebabepy.parser.antlr.PythonParser;
import org.cafebabepy.parser.antlr.PythonParserBaseVisitor;
import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module._ast.*;

import java.util.*;

/**
 * Created by yotchang4s on 2017/05/29.
 */
// FIXME SyntaxError
class CafeBabePyAstCreateVisitor extends PythonParserBaseVisitor<PyObject> {

    private final Python runtime;

    private final String file;
    private final String input;

    private boolean inFunction;

    private boolean inLoop;

    CafeBabePyAstCreateVisitor(Python runtime, String file, String input) {
        this.runtime = runtime;

        this.file = file;
        this.input = input;
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
    public PyObject visitDecorator(PythonParser.DecoratorContext ctx) {
        PyObject dotted_name = ctx.dotted_name().accept(this);

        PyObject load = this.runtime.newPyObject("_ast.Load");
        PyObject name = this.runtime.newPyObject("_ast.Name", dotted_name, load);

        if (ctx.arglist() != null) {
            PyObject call = createCall(ctx.arglist());
            call.getScope().put(this.runtime.str("func"), name);

            return call;
        }

        return name;
    }

    @Override
    public PyObject visitDecorators(PythonParser.DecoratorsContext ctx) {
        int count = ctx.decorator().size();
        PyObject[] decoratorArray = new PyObject[count];

        for (int i = 0; i < count; i++) {
            decoratorArray[i] = ctx.decorator(i).accept(this);
        }

        return this.runtime.list(decoratorArray);
    }

    @Override
    public PyObject visitDecorated(PythonParser.DecoratedContext ctx) {
        PyObject decorator_list = ctx.decorators().accept(this);

        PyObject def;
        if (ctx.classdef() != null) {
            def = ctx.classdef().accept(this);

        } else if (ctx.funcdef() != null) {
            def = ctx.funcdef().accept(this);

        } else if (ctx.async_funcdef() != null) {
            def = ctx.async_funcdef().accept(this);

        } else {
            throw new CafeBabePyException("def is not found");
        }

        def.getScope().put(this.runtime.str("decorator_list"), decorator_list);

        return def;
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
            target.getScope().put(this.runtime.str("ctx"), this.runtime.newPyObject("_ast.Store"));
            PyObject value = this.runtime.getattr(target, "value");
            if (this.runtime.isIterable(value)) {
                this.runtime.iter(value, v -> toStore(v, 0));

            } else {
                toStore(value, 0);
            }

        } else if (type instanceof PyNameType) {
            if (attributeDepth == 0) {
                target.getScope().put(this.runtime.str("ctx"), this.runtime.newPyObject("_ast.Store"));
            }

        } else if (type instanceof PyAttributeType) {
            if (attributeDepth == 0) {
                target.getScope().put(this.runtime.str("ctx"), this.runtime.newPyObject("_ast.Store"));
            }

            PyObject value = this.runtime.getattr(target, "value");
            toStore(value, attributeDepth + 1);

        } else if (type instanceof PyListType) {
            target.getScope().put(this.runtime.str("ctx"), this.runtime.newPyObject("_ast.Store"));
            PyObject elts = this.runtime.getattr(target, "elts");

            this.runtime.iter(elts, elt -> toStore(elt, 0));

        } else if (type instanceof PyNumType) { // FIXME all literal
            throw this.runtime.newRaiseException("builtins.SyntaxError", "can't assign to literal");
        }
    }

    @SuppressWarnings("unchecked")
    private PyObject createAnnasign(PyObject testlist_star_expr, PythonParser.AnnassignContext annassignContext) {
        PyObject list = visitAnnassign(annassignContext);
        List<PyObject> testList = list.toJava(List.class);

        if (!this.runtime.isInstance(testlist_star_expr, "_ast.Name")) {
            throw this.runtime.newRaiseException("builtins.SyntaxError", "illegal target for annotation");
        }
        testlist_star_expr.getScope().put(this.runtime.str("ctx"), this.runtime.newPyObject("_ast.Store"));
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
        boolean loop = this.inLoop;
        boolean function = this.inFunction;
        this.inLoop = false;
        this.inFunction = true;

        PyObject name = this.runtime.str(ctx.NAME().getText());
        PyObject args = this.runtime.None();
        PythonParser.ParametersContext parametersContext = ctx.parameters();
        if (parametersContext != null) {
            args = visitParameters(parametersContext);
        }
        PyObject body = visitSuite(ctx.suite());
        PyObject decorator_list = this.runtime.list();
        PyObject returns = this.runtime.None();// FIXME 未実装

        PythonParser.TestContext testContext = ctx.test();
        if (testContext != null) {
            returns = visitTest(testContext);
        }

        this.inFunction = function;
        this.inLoop = loop;

        return this.runtime.newPyObject("_ast.FunctionDef", name, args, body, decorator_list, returns);
    }

    @Override
    public PyObject visitParameters(PythonParser.ParametersContext ctx) {
        PythonParser.TypedargslistContext typedargslistContext = ctx.typedargslist();
        if (typedargslistContext == null) {
            return this.runtime.newPyObject(
                    "_ast.arguments",
                    this.runtime.list(), // arg* args
                    this.runtime.None(), // arg? vararg
                    this.runtime.list(), // arg* kwonlyargs
                    this.runtime.list(), // expr* kw_defaults
                    this.runtime.None(), // arg? kwarg
                    this.runtime.list()); // expr* defaults

        } else {
            return visitTypedargslist(typedargslistContext);
        }
    }

    @Override
    public PyObject visitTypedargslist(PythonParser.TypedargslistContext ctx) {
        List<PyObject> argsList = new ArrayList<>();
        PyObject vararg = this.runtime.None();
        PyObject kwarg = this.runtime.None();
        List<PyObject> kwonlyargsList = new ArrayList<>();
        List<PyObject> kw_defaultsList = new ArrayList<>();
        List<PyObject> defaultsList = new ArrayList<>();

        boolean argument = true;
        boolean defaultArgument = false;
        boolean defineDefaultArgument = false;
        boolean defineKeywordOnlyArgument = false;
        boolean keywordArgument = false;
        boolean beforeAsterisk = false;
        String beforeText = "";
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree tree = ctx.getChild(i);
            String text = tree.getText();

            PyObject element = tree.accept(this);

            if (element != null) {
                if (defaultArgument) {
                    if (defineKeywordOnlyArgument) {
                        kw_defaultsList.add(element);

                    } else {
                        defaultsList.add(element);
                    }

                    argument = false;
                    defineDefaultArgument = true;

                } else if (argument) {
                    if (keywordArgument) {
                        kwarg = element;
                        keywordArgument = false;

                    } else if (defineKeywordOnlyArgument) {
                        if (!beforeAsterisk && vararg.isNone()) {
                            vararg = element;

                        } else {
                            kwonlyargsList.add(element);
                        }

                        beforeAsterisk = false;

                    } else {
                        argsList.add(element);
                    }

                    argument = false;

                } else {
                    throw new CafeBabePyException("Unknown element");
                }

            } else if ("=".equals(text)) {
                defaultArgument = true;

            } else if (",".equals(text)) {
                if (i < ctx.getChildCount() - 1) { // last comma
                    if (defineDefaultArgument && !defaultArgument && !defineKeywordOnlyArgument) {
                        throw this.runtime.newRaiseException("builtins.SyntaxError", "non-default argument follows default argument");
                    }
                    argument = true;
                    defaultArgument = false;
                    beforeAsterisk = "*".equals(beforeText);
                }

            } else if ("*".equals(text)) {
                defineKeywordOnlyArgument = true;

            } else if ("**".equals(text)) {
                keywordArgument = true;
            }

            beforeText = text;
        }

        if (defineDefaultArgument && !defaultArgument && !defineKeywordOnlyArgument) {
            throw this.runtime.newRaiseException("builtins.SyntaxError", "non-default argument follows default argument");
        }

        PyObject args = this.runtime.list(argsList);
        PyObject kwonlyargs = this.runtime.list(kwonlyargsList);
        PyObject kw_defaults = this.runtime.list(kw_defaultsList);
        PyObject defaults = this.runtime.list(defaultsList);

        return this.runtime.newPyObject(
                "_ast.arguments", args, vararg, kwonlyargs, kw_defaults, kwarg, defaults);
    }

    @Override
    public PyObject visitTfpdef(PythonParser.TfpdefContext ctx) {
        PyObject arg = this.runtime.str(ctx.NAME().getText());
        PyObject annotation = this.runtime.None();

        if (ctx.test() != null) {
            annotation = ctx.test().accept(this);
        }

        return this.runtime.newPyObject("_ast.arg", arg, annotation);
    }

    @Override
    public PyObject visitVarargslist(PythonParser.VarargslistContext ctx) {
        List<PythonParser.VfpdefContext> vfpdefContextList = ctx.vfpdef();
        PyObject[] argArray = new PyObject[vfpdefContextList.size()];

        for (int i = 0; i < argArray.length; i++) {
            argArray[i] = vfpdefContextList.get(i).accept(this);
        }

        // TODO 対応する
        PyObject args = this.runtime.list(argArray);
        PyObject vararg = this.runtime.None();
        PyObject kwonlyargs = this.runtime.list();
        PyObject kw_defaults = this.runtime.list();
        PyObject kwarg = this.runtime.None();
        PyObject defaults = this.runtime.list();

        return this.runtime.newPyObject(
                "_ast.arguments", args, vararg, kwonlyargs, kw_defaults, kwarg, defaults);
    }

    @Override
    public PyObject visitVfpdef(PythonParser.VfpdefContext ctx) {
        PyObject arg = this.runtime.str(ctx.NAME().getText());

        return this.runtime.newPyObject("_ast.arg", arg, this.runtime.None());
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
    public PyObject visitRaise_stmt(PythonParser.Raise_stmtContext ctx) {
        PyObject exc = this.runtime.None();
        PyObject cause = this.runtime.None();

        if (ctx.test().size() > 0) {
            exc = ctx.test().get(0).accept(this);
            if (ctx.test().size() == 2) {
                cause = ctx.test(1).accept(this);
            }
        }

        return this.runtime.newPyObject("_ast.Raise", exc, cause);
    }

    @Override
    public PyObject visitImport_name(PythonParser.Import_nameContext ctx) {
        PyObject names = ctx.dotted_as_names().accept(this);

        return this.runtime.newPyObject("_ast.Import", names);
    }

    @Override
    public PyObject visitImport_from(PythonParser.Import_fromContext ctx) {
        int level = 0;
        if (ctx.DOT() != null) {
            level += ctx.DOT().size();
        }
        if (ctx.ELLIPSIS() != null) {
            level += (ctx.ELLIPSIS().size() * 3);
        }
        PyObject module = this.runtime.None();
        if (ctx.dotted_name() != null) {
            module = ctx.dotted_name().accept(this);
        }

        PyObject names;
        if (ctx.STAR() != null) {
            PyObject alias = this.runtime.newPyObject("_ast.alias", this.runtime.str("*"), this.runtime.None());

            names = this.runtime.list(alias);

        } else {
            names = ctx.import_as_names().accept(this);
        }

        return this.runtime.newPyObject("_ast.ImportFrom", module, names, this.runtime.number(level));
    }

    @Override
    public PyObject visitImport_as_names(PythonParser.Import_as_namesContext ctx) {
        List<PyObject> names = new ArrayList<>(ctx.import_as_name().size());
        int count = ctx.import_as_name().size();
        for (int i = 0; i < count; i++) {
            PyObject importAsName = ctx.import_as_name(i).accept(this);
            names.add(importAsName);
        }

        return this.runtime.list(names);
    }

    @Override
    public PyObject visitImport_as_name(PythonParser.Import_as_nameContext ctx) {
        PyObject name = this.runtime.str(ctx.NAME(0).getSymbol().getText());
        PyObject asName = this.runtime.None();

        if (ctx.NAME().size() == 2) {
            asName = this.runtime.str(ctx.NAME(1).getSymbol().getText());

        }

        return this.runtime.newPyObject("_ast.alias", name, asName);
    }

    @Override
    public PyObject visitDotted_as_names(PythonParser.Dotted_as_namesContext ctx) {
        int count = ctx.dotted_as_name().size();
        List<PyObject> dottedAsNames = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
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
        boolean loop = this.inLoop;
        this.inLoop = true;

        PyObject target = visitExprlist(ctx.exprlist());
        PyObject iter = visitTestlist(ctx.testlist());
        PyObject body;
        PyObject orelse = this.runtime.None();

        List<PythonParser.SuiteContext> suiteContextList = ctx.suite();
        body = visitSuite(suiteContextList.get(0));
        if (suiteContextList.size() == 2) {
            orelse = visitSuite(suiteContextList.get(1));
        }

        this.inLoop = loop;

        return this.runtime.newPyObject("_ast.For", target, iter, body, orelse);
    }

    @Override
    public PyObject visitTry_stmt(PythonParser.Try_stmtContext ctx) {
        int suiteIndex = 0;
        PyObject tryBody = ctx.suite(suiteIndex++).accept(this);

        List<PyObject> exceptHandlerList = new ArrayList<>();
        for (int i = 0; i < ctx.except_clause().size(); i++) {
            PyObject except_cause = ctx.except_clause(i).accept(this);

            PyObject type = this.runtime.getitem(except_cause, this.runtime.number(0));
            PyObject name = this.runtime.getitem(except_cause, this.runtime.number(1));
            PyObject exceptHandlerBody = ctx.suite(suiteIndex++).accept(this);

            PyObject exceptHandler = this.runtime.newPyObject("_ast.ExceptHandler", type, name, exceptHandlerBody);

            exceptHandlerList.add(exceptHandler);
        }

        PyObject handlers = this.runtime.list(exceptHandlerList);
        PyObject orelse = this.runtime.None();
        if (ctx.ELSE() != null) {
            orelse = ctx.suite(suiteIndex++).accept(this);
        }
        PyObject finalbody = this.runtime.None();
        if (ctx.FINALLY() != null) {
            finalbody = ctx.suite(suiteIndex++).accept(this);
        }

        return this.runtime.newPyObject("_ast.Try", tryBody, handlers, orelse, finalbody);
    }

    @Override
    public PyObject visitWith_stmt(PythonParser.With_stmtContext ctx) {
        List<PythonParser.With_itemContext> withItemsContextList = ctx.with_item();

        List<PyObject> withItemList = new ArrayList<>(withItemsContextList.size());

        for (int i = 0; i < withItemsContextList.size(); i++) {
            PyObject withItem = withItemsContextList.get(i).accept(this);

            withItemList.add(withItem);
        }

        PyObject withItems = this.runtime.list(withItemList);
        PyObject withSuite = ctx.suite().accept(this);

        return this.runtime.newPyObject("_ast.With", withItems, withSuite);
    }

    @Override
    public PyObject visitWith_item(PythonParser.With_itemContext ctx) {
        PyObject context_expr = ctx.test().accept(this);
        PyObject optional_vars = this.runtime.None();
        if (ctx.expr() != null) {
            optional_vars = ctx.expr().accept(this);
        }

        return this.runtime.newPyObject("_ast.withitem", context_expr, optional_vars);
    }

    @Override
    public PyObject visitExcept_clause(PythonParser.Except_clauseContext ctx) {
        PyObject type = this.runtime.None();
        PyObject name = this.runtime.None();

        if (ctx.test() != null) {
            type = ctx.test().accept(this);
        }

        if (ctx.NAME() != null) {
            name = this.runtime.str(ctx.NAME().getSymbol().getText());
        }

        return this.runtime.tuple(type, name);
    }

    @Override
    public PyObject visitTest(PythonParser.TestContext ctx) {
        if (ctx.lambdef() != null) {
            return ctx.lambdef().accept(this);

        } else {
            PyObject test = ctx.or_test().get(0).accept(this);
            if (ctx.or_test().size() == 2) {
                PyObject ifTest = ctx.or_test().get(1).accept(this);
                PyObject elseTest = ctx.test().accept(this);

                return this.runtime.newPyObject("_ast.IfExp", ifTest, test, elseTest);

            } else {
                return test;
            }
        }
    }

    @Override
    public PyObject visitLambdef(PythonParser.LambdefContext ctx) {
        PyObject arguments;
        if (ctx.varargslist() != null) {
            arguments = ctx.varargslist().accept(this);

        } else {
            arguments = this.runtime.newPyObject("_ast.arguments",
                    this.runtime.list(), // arg* args
                    this.runtime.None(), // arg? vararg
                    this.runtime.list(), // arg* kwonlyargs
                    this.runtime.list(), // expr* kw_defaults
                    this.runtime.None(), // arg? kwarg
                    this.runtime.list()); // expr* defaults
        }

        PyObject body = ctx.test().accept(this);

        return this.runtime.newPyObject("_ast.Lambda", arguments, body);
    }

    @Override
    public PyObject visitLambdef_nocond(PythonParser.Lambdef_nocondContext ctx) {
        PyObject arguments;
        if (ctx.varargslist() != null) {
            arguments = ctx.varargslist().accept(this);

        } else {
            arguments = this.runtime.newPyObject("_ast.arguments",
                    this.runtime.list(), // arg* args
                    this.runtime.None(), // arg? vararg
                    this.runtime.list(), // arg* kwonlyargs
                    this.runtime.list(), // expr* kw_defaults
                    this.runtime.None(), // arg? kwarg
                    this.runtime.list()); // expr* defaults
        }

        PyObject body = ctx.test_nocond().accept(this);

        return this.runtime.newPyObject("_ast.Lambda", arguments, body);
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
    public PyObject visitBreak_stmt(PythonParser.Break_stmtContext ctx) {
        if (!this.inLoop) {
            Token currentToken = ctx.getStop();
            int line = currentToken.getLine();
            int position = currentToken.getCharPositionInLine();

            String lineInput = this.input.split("(\r\n|\r|\n)")[line - 1];

            throw this.runtime.newRaiseException("builtins.SyntaxError",
                    this.runtime.str("'break' outside loop"),
                    this.runtime.str(this.file),
                    this.runtime.number(line),
                    this.runtime.number(position),
                    this.runtime.str(lineInput));
        }

        return this.runtime.newPyObject("_ast.Break");
    }

    @Override
    public PyObject visitContinue_stmt(PythonParser.Continue_stmtContext ctx) {
            throw this.runtime.newRaiseException("SyntaxError", "'continue' not properly in loop");
        if (!this.inLoop) {
        }

        return this.runtime.newPyObject("_ast.Continue");
    }

    @Override
    public PyObject visitReturn_stmt(PythonParser.Return_stmtContext ctx) {
        if (!this.inFunction) {
            throw this.runtime.newRaiseException("SyntaxError", "'return' outside function");
        }
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
        // <
        if (ctx.LESS_THAN() != null) {
            return this.runtime.newPyObject("_ast.Lt");
        }
        // >
        if (ctx.GREATER_THAN() != null) {
            return this.runtime.newPyObject("_ast.Gt");
        }
        // ==
        if (ctx.EQUALS() != null) {
            return this.runtime.newPyObject("_ast.Eq");
        }
        // >=
        if (ctx.GT_EQ() != null) {
            return this.runtime.newPyObject("_ast.GtE");
        }
        // <=
        if (ctx.LT_EQ() != null) {
            return this.runtime.newPyObject("_ast.LtE");
        }
        // <>
        if (ctx.NOT_EQ_1() != null) {
            return this.runtime.newPyObject("_ast.NotEq");
        }
        // !=
        if (ctx.NOT_EQ_2() != null) {
            return this.runtime.newPyObject("_ast.NotEq");
        }

        if (ctx.IN() != null) {
            // not in
            if (ctx.NOT() != null) {
                return this.runtime.newPyObject("_ast.NotIn");

            } else {
                return this.runtime.newPyObject("_ast.In");
            }
        }

        if (ctx.IS() != null) {
            // not is
            if (ctx.NOT() != null) {
                return this.runtime.newPyObject("_ast.IsNot");

            } else {
                return this.runtime.newPyObject("_ast.Is");
            }
        }

        throw this.runtime.newRaiseException("builtins.SyntaxError", "op '" + ctx.getText() + "' is not found");
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

        int count = ctx.trailer().size();
        if (count == 0) {
            return atom;
        }

        PyObject expr = null;
        for (PythonParser.TrailerContext trailerContext : ctx.trailer()) {
            PyObject trailer = trailerContext.accept(this);

            if (this.runtime.isInstance(trailer, "_ast.Call")) {
                if (expr == null) {
                    trailer.getScope().put(this.runtime.str("func"), atom);

                } else {
                    trailer.getScope().put(this.runtime.str("func"), expr);
                }

            } else if (this.runtime.isInstance(trailer, "_ast.Attribute")) {
                if (expr == null) {
                    trailer.getScope().put(this.runtime.str("value"), atom);

                } else {
                    trailer.getScope().put(this.runtime.str("value"), expr);
                }
            } else if (this.runtime.isInstance(trailer, "_ast.Subscript")) {
                if (expr == null) {
                    trailer.getScope().put(this.runtime.str("value"), atom);

                } else {
                    trailer.getScope().put(this.runtime.str("value"), expr);
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
            PyObject str = visitStr(strContextList.get(0));
            for (int i = 1; i < strContextList.size(); i++) {
                PyObject newStr = visitStr(strContextList.get(i));
                if (this.runtime.isInstance(newStr, "_ast.JoinedStr")) {
                    List<PyObject> joinedStrList = new ArrayList<>(visitStr(strContextList.get(i)).toJava(List.class));
                    PyObject joinedLastStr = joinedStrList.get(joinedStrList.size() - 1);

                    if (this.runtime.isInstance(newStr, "_ast.Str") && this.runtime.isInstance(joinedLastStr, "_ast.Str")) {
                        PyObject joinedStrStr = addASTStr(newStr, joinedLastStr);

                        if (joinedStrList.isEmpty()) {
                            joinedStrList.add(joinedStrStr);

                        } else {
                            joinedStrList.set(joinedStrList.size() - 1, joinedStrStr);
                        }

                    } else {
                        joinedStrList.add(newStr);
                    }

                    str.getScope().put(this.runtime.str("values"), this.runtime.list(joinedStrList));

                } else {
                    str = addASTStr(str, newStr);
                }
            }

            return str;
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
                    PyObject elts;
                    if (this.runtime.isInstance(testlist_comp, "builtins.list")) {
                        elts = testlist_comp;

                    } else {
                        elts = this.runtime.list(testlist_comp);
                    }
                    PyObject load = this.runtime.newPyObject("_ast.Load");
                    return this.runtime.newPyObject("_ast.List", elts, load);
                }

            } else {
                PyObject load = this.runtime.newPyObject("_ast.Load");
                return this.runtime.newPyObject("_ast.List", this.runtime.list(), load);
            }

        } else if ("(".equals(open)) {
            if (!")".equals(close)) {
                throw this.runtime.newRaiseException("builtins.SyntaxError", "invalid syntax");
            }

            if (ctx.testlist_comp() != null) {
                PyObject testlist_comp = ctx.testlist_comp().accept(this);
                if (this.runtime.isInstance(testlist_comp, "_ast.ListComp")) {
                    PyObject elt = this.runtime.getattr(testlist_comp, "elt");
                    PyObject generators = this.runtime.getattr(testlist_comp, "generators");

                    return this.runtime.newPyObject("_ast.GeneratorExp", elt, generators);

                } else if (this.runtime.isInstance(testlist_comp, "_ast.expr")) {
                    return testlist_comp;

                } else {
                    PyObject load = this.runtime.newPyObject("_ast.Load");
                    return this.runtime.newPyObject("_ast.Tuple", testlist_comp, load);
                }
            }

            PyObject load = this.runtime.newPyObject("_ast.Load");
            return this.runtime.newPyObject("_ast.Tuple", this.runtime.list(), load);

        } else if ("{".equals(open)) {
            if (!"}".equals(close)) {
                throw this.runtime.newRaiseException("builtins.SyntaxError", "invalid syntax");
            }

            if (ctx.dictorsetmaker() != null) {
                // dict or set
                return ctx.dictorsetmaker().accept(this);

            } else {
                return this.runtime.newPyObject("_ast.Dict", this.runtime.list(), this.runtime.list());
            }

        }

        return super.visitAtom(ctx);
    }

    private PyObject addASTStr(PyObject str1, PyObject str2) {
        String javaStr = "";

        Optional<PyObject> str1Opt = str1.getScope().get(this.runtime.str("s"));
        if (str1Opt.isPresent()) {
            javaStr += str1Opt.get().toJava(String.class);
        }

        Optional<PyObject> str2Opt = str2.getScope().get(this.runtime.str("s"));
        if (str2Opt.isPresent()) {
            javaStr += str2Opt.get().toJava(String.class);
        }

        return this.runtime.newPyObject("_ast.Str", this.runtime.str(javaStr));
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
                if (ctx.COMMA().size() >= 1) {
                    return this.runtime.list(element);

                } else {
                    return element;
                }
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
            return createCall(ctx.arglist());

        } else if ("[".equals(firstText) && "]".equals(lastText)) {
            PythonParser.SubscriptlistContext subscriptlistContext = ctx.subscriptlist();
            if (subscriptlistContext == null) {
                throw this.runtime.newRaiseException("builtins.SyntaxError", "sub script list not found");
            }
            PyObject subscriptlist = visitSubscriptlist(subscriptlistContext);

            // Default
            PyObject load = this.runtime.newPyObject("_ast.Load");

            return this.runtime.newPyObject("_ast.Subscript", this.runtime.None(), subscriptlist, load);

        }

        throw this.runtime.newRaiseException("builtins.SyntaxError", "Invalid ast");
    }

    private PyObject createCall(PythonParser.ArglistContext ctx) {
        PyObject args;
        PyObject keywords;

        if (ctx != null) {
            List<PyObject> argsJList = new ArrayList<>();
            List<PyObject> keywordJList = new ArrayList<>();
            this.runtime.iter(ctx.accept(this), arg -> {
                if (this.runtime.isInstance(arg, "_ast.keyword")) {
                    keywordJList.add(arg);

                } else {
                    argsJList.add(arg);
                }
            });

            args = this.runtime.list(argsJList);
            keywords = this.runtime.list(keywordJList);

        } else {
            args = this.runtime.list();
            keywords = this.runtime.list();
        }

        return this.runtime.newPyObject("_ast.Call", this.runtime.None(), args, keywords);
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
        boolean loop = this.inLoop;
        boolean function = this.inFunction;
        this.inLoop = false;
        this.inFunction = false;

        PyObject name = this.runtime.str(ctx.NAME().getText());

        PyObject bases;
        if (ctx.arglist() != null) {
            bases = ctx.arglist().accept(this);

        } else {
            bases = this.runtime.list();
        }

        PyObject keywords = this.runtime.list();

        PythonParser.SuiteContext suiteContext = ctx.suite();
        PyObject body = visitSuite(suiteContext);

        PyObject decorator_list = this.runtime.list();

        this.inFunction = function;
        this.inLoop = loop;

        return this.runtime.newPyObject("_ast.ClassDef",
                name, bases, keywords, body, decorator_list);
    }

    @Override
    public PyObject visitArglist(PythonParser.ArglistContext ctx) {
        PyObject[] argumentArray = new PyObject[ctx.argument().size()];

        Set<PyObject> duplicateCheckSet = new HashSet<>();
        for (int i = 0; i < argumentArray.length; i++) {
            argumentArray[i] = ctx.argument().get(i).accept(this);
            if (this.runtime.isInstance(argumentArray[i], "_ast.keyword")) {
                PyObject arg = this.runtime.getattr(argumentArray[i], "arg");
                if (!duplicateCheckSet.add(arg) && !arg.isNone()) {
                    throw this.runtime.newRaiseException("builtins.SyntaxError", "keyword argument repeated");
                }
            }
        }

        boolean defineKeyword = false;
        boolean unpacking = false;
        for (PyObject argument : argumentArray) {
            if (this.runtime.isInstance(argument, "_ast.keyword")) {
                PyObject arg = this.runtime.getattr(argument, "arg");
                if (arg.isNone()) {
                    defineKeyword = false;
                    unpacking = true;

                } else {
                    defineKeyword = true;
                }

            } else if (defineKeyword) {
                throw this.runtime.newRaiseException("builtins.SyntaxError", "positional argument follows keyword argument");

            } else if (unpacking) {
                throw this.runtime.newRaiseException("builtins.SyntaxError", "positional argument follows keyword argument unpacking");
            }
        }

        boolean generator = false;
        for (PyObject argument : argumentArray) {
            if (generator) {
                throw this.runtime.newRaiseException("SyntaxError", "Generator expression must be parenthesized");
            }
            if (this.runtime.isInstance(argument, "_ast.GeneratorExp")) {
                generator = true;
            }
        }

        return this.runtime.list(argumentArray);
    }

    @Override
    public PyObject visitArgument(PythonParser.ArgumentContext ctx) {
        PyObject argument;
        if (ctx.test().size() == 1) {
            if (ctx.comp_for() != null) {
                PyObject elt = ctx.test().get(0).accept(this);
                PyObject generators = ctx.comp_for().accept(this);

                return this.runtime.newPyObject("_ast.GeneratorExp", elt, generators);

            } else {
                String text = ctx.getChild(0).getText();
                if ("*".equals(text)) {
                    PyObject value = ctx.getChild(1).accept(this);
                    PyObject load = this.runtime.newPyObject("_ast.Load");

                    argument = this.runtime.newPyObject("_ast.Starred", value, load);

                } else if ("**".equals(text)) {
                    PyObject value = ctx.getChild(1).accept(this);

                    argument = this.runtime.newPyObject("_ast.keyword", this.runtime.None(), value);

                } else {
                    argument = ctx.test().get(0).accept(this);
                }
            }

        } else { // test is two
            PyObject argTest = ctx.test().get(0).accept(this);
            PyObject value = ctx.test().get(1).accept(this);

            // _ast.Name only
            PyObject arg = this.runtime.getattr(argTest, "id");

            argument = this.runtime.newPyObject("_ast.keyword", arg, value);
        }

        return argument;
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
            comprehension.getScope().put(this.runtime.str("ifs"), ifs);

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
    public PyObject visitYield_expr(PythonParser.Yield_exprContext ctx) {
        if (ctx.yield_arg() != null) {
            return ctx.yield_arg().accept(this);
        }

        return this.runtime.newPyObject("_ast.Yield", this.runtime.None());
    }

    @Override
    public PyObject visitYield_arg(PythonParser.Yield_argContext ctx) {
        if (ctx.test() != null) {
            PyObject value = ctx.test().accept(this);

            return this.runtime.newPyObject("_ast.YieldFrom", value);
        }

        PyObject value = ctx.testlist().accept(this);

        return this.runtime.newPyObject("_ast.Yield", value);
    }

    @Override
    public PyObject visitNumber(PythonParser.NumberContext ctx) {
        // TODO over jva int
        String text = ctx.getChild(0).getText();

        PyObject number;
        if (text.startsWith("0x")) {
            number = this.runtime.number(Integer.decode(text));

        } else if (text.contains(".")) {
            number = this.runtime.number(Float.parseFloat(text));

        } else {
            number = this.runtime.number(Integer.parseInt(text));
        }

        return this.runtime.newPyObject("_ast.Num", number);
    }

    @Override
    public PyObject visitStr(PythonParser.StrContext ctx) {
        String rawString = ctx.STRING_LITERAL().getText();
        String str;

        boolean fstring = false;

        int tripleDoubleIndex = rawString.indexOf("\"\"\"");
        if (tripleDoubleIndex == 1) {
            fstring = (rawString.charAt(0) == 'f');
            str = rawString.substring(4, rawString.length() - 3);

        } else if (tripleDoubleIndex == 0) {
            str = rawString.substring(3, rawString.length() - 3);

        } else {
            int tripleSingleIndex = rawString.indexOf("'''");
            if (tripleSingleIndex == 1) {
                fstring = (rawString.charAt(0) == 'f');
                str = rawString.substring(4, rawString.length() - 3);

            } else if (tripleSingleIndex == 0) {
                str = rawString.substring(3, rawString.length() - 3);

            } else {
                fstring = (rawString.charAt(0) == 'f');
                if (fstring) {
                    str = rawString.substring(2, rawString.length() - 1);

                } else {
                    str = rawString.substring(1, rawString.length() - 1);
                }
            }
        }

        if (fstring) {
            return fstring(str, 0);

        } else {
            return this.runtime.newPyObject("_ast.Str", this.runtime.str(str));
        }
    }

    // FIXME move compile
    private PyObject fstring(String fstring, int depth) {
        if (depth >= 2) {
            throw this.runtime.newRaiseException("SyntaxError", "f-string: expressions nested too deeply");
        }

        List<PyObject> joinStr = new ArrayList<>();

        int startIndex = fstring.indexOf('{');
        int endIndex = fstring.lastIndexOf('}');
        if (startIndex == -1 && endIndex == -1) {
            return this.runtime.newPyObject("_ast.Str", this.runtime.str(fstring));

        } else if ((startIndex != -1 && endIndex == -1)) {
            throw this.runtime.newRaiseException("SyntaxError", "f-string: expecting '}'");

        } else if (startIndex == -1 && endIndex != -1) {
            throw this.runtime.newRaiseException("SyntaxError", "single '}' is not allowed");
        }

        if (startIndex > 0) {
            PyObject str = this.runtime.str(fstring.substring(0, startIndex));
            PyObject startStr = this.runtime.newPyObject("_ast.Str", str);

            joinStr.add(startStr);
        }

        String replacementField = fstring.substring(startIndex + 1, endIndex);
        if (replacementField.isEmpty()) {
            throw this.runtime.newRaiseException("SyntaxError", "f-string: empty expression not allowed");
        }

        int conversion = -1;
        int formatSpecIndex = replacementField.indexOf(':');

        int conversionIndex = replacementField.indexOf('!');
        if (conversionIndex != -1) {
            // Example f'{a:{b!s}}' and f'{a}'
            int secondStartIndex = replacementField.indexOf('{', formatSpecIndex);
            if (secondStartIndex == -1) {
                if (conversionIndex + 1 != replacementField.length()
                        && (replacementField.charAt(conversionIndex + 1) == 's'
                        || replacementField.charAt(conversionIndex + 1) == 'r'
                        || replacementField.charAt(conversionIndex + 1) == 'a')) {

                    // Example f'{a!s :>}'
                    if (conversionIndex + 1 == replacementField.length()) {
                        throw this.runtime.newRaiseException("SyntaxError", "f-string: expecting '}'");
                    }
                    conversion = replacementField.charAt(conversionIndex + 1);

                } else {
                    throw this.runtime.newRaiseException("SyntaxError", "f-string: invalid conversion character: expected 's', 'r', or 'a'");
                }
            }
        }

        String test;
        if (conversionIndex != -1) {
            test = replacementField.substring(0, conversionIndex);

        } else if (formatSpecIndex != -1) {
            test = replacementField.substring(0, formatSpecIndex);

        } else {
            test = replacementField.substring(0, endIndex - startIndex - 1);
        }

        CodePointCharStream stream = CharStreams.fromString(test);
        CafeBabePyLexer lexer = new CafeBabePyLexer(stream);
        TokenStream tokens = new CommonTokenStream(lexer);
        CafeBabePyParser parser = new CafeBabePyParser(tokens);
        CafeBabePyAstCreateVisitor creator = new CafeBabePyAstCreateVisitor(this.runtime, this.file, this.input);

        PyObject value = creator.visit(parser.or_test());

        PyObject formatSpec = this.runtime.None();

        if (formatSpecIndex != -1) {
            formatSpec = fstring(replacementField.substring(formatSpecIndex + 1), depth + 1);
        }

        PyObject formattedValue = this.runtime.newPyObject("_ast.FormattedValue", value, this.runtime.number(conversion), formatSpec);
        joinStr.add(formattedValue);

        // Example f'{a} {b}c'
        if ((endIndex + 2) <= fstring.length()) {
            String endString = fstring.substring(endIndex + 1);
            if (!fstring.equals(endString)) {
                joinStr.add(fstring(endString, 0));
            }
        }

        return this.runtime.newPyObject("_ast.JoinedStr", this.runtime.list(joinStr));
    }
}
