package org.cafebabepy.evaluter.Interpret;

import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.RaiseException;
import org.cafebabepy.runtime.module._ast.*;
import org.cafebabepy.runtime.object.proxy.PyLexicalScopeProxyObject;

import java.util.*;

import static org.cafebabepy.util.ProtocolNames.*;

/**
 * Created by yotchang4s on 2017/06/09.
 */
public class InterpretEvaluator {

    private Python runtime;

    private ImportManager importManager;

    public InterpretEvaluator(Python runtime) {
        this.runtime = runtime;
        this.importManager = new ImportManager(runtime);
    }

    /*
    public PyObject evalMainModule(PyObject node) {
        PyObject context = this.runtime.moduleOrThrow("__main__");
        return eval(context, node);

        System.out.println(node.asJavaString());
        PyObject superType = typeOrThrow("builtins.super");
        PyObject superObject = superType.call(self);

        PyObject getattribute = superType.getObjectOrThrow(__getattribute__);
        PyObject visit = getattribute.call(superObject, this.runtime.str("visit"));

        visit.call(self, node);
    }
    */

    public PyObject eval(PyObject context, PyObject node) {
        if (node.isNone()) {
            return node;

        } else if (node.isEllipsis()) {
            return node;

        } else if (this.runtime.isInstance(node, "builtins.bool")) {
            return node;
        }

        if (this.runtime.isInstance(node, "builtins.list")) {
            PyObject[] end = new PyObject[1];
            this.runtime.iter(node, n -> end[0] = eval(context, n));
            if (end[0] == null) {
                return this.runtime.None();

            } else {
                return end[0];
            }
        }

        switch (node.getName()) {
            case "Module":
                return evalModule(context, node);

            case "Interactive":
                return evalInteractive(context, node);

            case "Suite":
                return evalSuite(context, node);

            case "Import":
                return evalImport(context, node);

            case "ImportFrom":
                return evalImportFrom(context, node);

            case "FunctionDef":
                return evalFunctionDef(context, node);

            case "ClassDef":
                return evalClassDef(context, node);

            case "If":
                return evalIfAndIfExp(context, node);

            case "Raise":
                return evalRaise(context, node);

            case "Try":
                return evalTry(context, node);

            case "With":
                return evalWith(context, node);

            case "For":
                return evalFor(context, node);

            case "Expr":
                return evalExpr(context, node);

            case "Pass":
                return evalPass(context, node);

            case "Break":
                return evalBreak(context, node);

            case "Continue":
                return evalContinue(context, node);

            case "Assign":
                return evalAssign(context, node);

            case "Annassign":
                return evalAnnassign(context, node);

            case "IfExp":
                return evalIfAndIfExp(context, node);

            case "List":
                return evalList(context, node);

            case "ListComp":
                return evalListComp(context, node);

            case "GeneratorExp":
                return evalGeneratorExp(context, node);

            case "BinOp":
                return evalBinOp(context, node);

            case "UnaryOp":
                return evalUnaryOp(context, node);

            case "Lambda":
                return evalLambda(context, node);

            case "Call":
                return evalCall(context, node);

            case "Return":
                return evalReturn(context, node);

            case "Compare":
                return evalCompare(context, node);

            case "Name":
                return evalName(context, node);

            case "Num":
                return evalNum(context, node);

            case "Str":
                return evalStr(context, node);

            case "FormattedValue":
                return evalFormattedValue(context, node);

            case "JoinedStr":
                return evalJoinedStr(context, node);

            case "Attribute":
                return evalAttribute(context, node);

            case "Dict":
                return evalDict(context, node);

            case "Tuple":
                return evalTuple(context, node);

            case "Subscript":
                return evalSubscript(context, node);

            case "Index":
                return evalIndex(context, node);

            case "Slice":
                return evalSlice(context, node);
        }

        throw new CafeBabePyException("Unknown AST '" + node.getName() + "'");
    }

    private PyObject evalModule(PyObject context, PyObject node) {
        PyObject body = this.runtime.getattr(node, "body");
        return eval(context, body);
    }

    private PyObject evalInteractive(PyObject context, PyObject node) {
        PyObject body = this.runtime.getattr(node, "body");
        return eval(context, body);
    }

    private PyObject evalSuite(PyObject context, PyObject node) {
        PyObject body = this.runtime.getattr(node, "body");

        PyObject[] result = new PyObject[1];
        result[0] = this.runtime.None();
        this.runtime.iter(body, b -> {
            result[0] = eval(context, body);
        });

        return result[0];
    }

    private PyObject evalImport(PyObject context, PyObject node) {
        PyObject names = this.runtime.getattr(node, "names");

        this.runtime.iter(names, n -> {
            PyObject name = this.runtime.getattr(n, "name");
            PyObject asname = this.runtime.getattr(n, "asname");

            this.importManager.importAsName(context, name, asname);
        });

        return this.runtime.None();
    }

    private PyObject evalImportFrom(PyObject context, PyObject node) {
        PyObject module = this.runtime.getattr(node, "module");
        PyObject names = this.runtime.getattr(node, "names");
        PyObject level = this.runtime.getattr(node, "level");

        this.importManager.importFrom(context, module, names, level);

        return this.runtime.None();
    }

    private PyObject evalFunctionDef(PyObject context, PyObject node) {
        PyObject name = this.runtime.getattr(node, "name");
        PyObject args = this.runtime.getattr(node, "args");
        PyObject body = this.runtime.getattr(node, "body");
        PyObject decorator_list = this.runtime.getattr(node, "decorator_list");
        PyObject returns = this.runtime.getattr(node, "returns"); // FIXME ???

        PyObject function = new PyInterpretFunctionObject(
                this.runtime, name.toJava(String.class), context, args, body);
        function.initialize();

        List<PyObject> decorators = new ArrayList<>();
        this.runtime.iter(decorator_list, decorators::add);
        Collections.reverse(decorators);

        PyObject decoratorEvalValue = function;

        int decoratorCount = decorators.size();
        for (int i = 0; i < decoratorCount; i++) {
            PyObject decorator = decorators.get(i);

            PyObject decoratorEvalFunction = eval(context, decorator);
            decoratorEvalValue = decoratorEvalFunction.call(decoratorEvalValue);
        }

        context.getScope().put(name, decoratorEvalValue);

        return this.runtime.None();
    }

    private PyObject evalClassDef(PyObject context, PyObject node) {
        PyObject name = this.runtime.getattr(node, "name");
        PyObject bases = this.runtime.getattr(node, "bases");
        PyObject keywords = this.runtime.getattr(node, "keywords");
        PyObject body = this.runtime.getattr(node, "body");
        PyObject decorator_list = this.runtime.getattr(node, "decorator_list");

        List<PyObject> baseList = new ArrayList<>();
        Set<String> duplicateCheckSet = new LinkedHashSet<>();
        this.runtime.iter(bases, base -> {
            boolean exists = !duplicateCheckSet.add(base.toJava(String.class));
            if (exists) {
                throw this.runtime.newRaiseTypeError("duplicate base class " + base.getName());
            }
            baseList.add(base);
        });

        String n = name.toJava(String.class);
        if (!context.isModule()) {
            n = context.getName() + "." + n;
        }

        PyObject clazz = new PyInterpretClassObject(
                this.runtime, context, n, baseList);

        context.getScope().put(name, clazz);

        eval(clazz, body);

        return this.runtime.None();
    }

    private PyObject evalIfAndIfExp(PyObject context, PyObject node) {
        PyObject test = this.runtime.getattr(node, "test");
        PyObject body = this.runtime.getattr(node, "body");
        PyObject orElse = this.runtime.getattr(node, "orelse");

        PyObject evalTest = eval(context, test);
        if (evalTest.isTrue()) {
            return eval(context, body);

        } else {
            return eval(context, orElse);
        }
    }

    private PyObject evalRaise(PyObject context, PyObject node) {
        PyObject exc = this.runtime.getattr(node, "exc");

        // FIXME raise Exception from xxx
        PyObject cause = this.runtime.getattr(node, "cause");

        PyObject evalExc = eval(context, exc);
        if (!this.runtime.isInstance(evalExc, "BaseException")) {
            throw this.runtime.newRaiseTypeError("exceptions must derive from BaseException");
        }

        throw this.runtime.newRaiseException(evalExc);
    }

    private PyObject evalTry(PyObject context, PyObject node) {
        PyObject body = this.runtime.getattr(node, "body");
        PyObject handlers = this.runtime.getattr(node, "handlers");
        PyObject orelse = this.runtime.getattr(node, "orelse");
        PyObject finalbody = this.runtime.getattr(node, "finalbody");

        PyObject result;
        RaiseException elseException = null;

        try {
            result = eval(context, body);
            if (!orelse.isNone()) {
                try {
                    result = eval(context, orelse);

                } catch (RaiseException e) {
                    elseException = e;
                }
            }

        } catch (RaiseException e) {
            PyObject exception = e.getException();

            List<PyObject> handlerList = new ArrayList<>();
            this.runtime.iter(handlers, handlerList::add);

            for (int i = 0; i < handlerList.size(); i++) {
                PyObject handler = handlerList.get(i);

                PyObject type = this.runtime.getattr(handler, "type");
                PyObject evalType = eval(context, type);

                if (this.runtime.isInstance(exception, evalType)) {
                    PyObject name = this.runtime.getattr(handler, "name");

                    PyObject exceptBody = this.runtime.getattr(handler, "body");
                    try {
                        if (!name.isNone()) {
                            context.getScope().put(name, exception);
                        }
                        return eval(context, exceptBody);

                    } finally {
                        if (!name.isNone()) {
                            context.getScope().remove(name);
                        }
                    }
                }
            }

            throw e;

        } finally {
            if (!finalbody.isNone()) {
                return eval(context, finalbody);
            }
        }

        if (elseException != null) {
            throw elseException;
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private PyObject evalWith(PyObject context, PyObject node) {
        PyObject items = this.runtime.getattr(node, "items");
        PyObject body = this.runtime.getattr(node, "body");

        List<PyObject> itemList = new ArrayList<>();
        this.runtime.iter(items, itemList::add);

        PyObject lexicalContext = new PyLexicalScopeProxyObject(context);

        List<PyObject> evalContextExprList = new ArrayList<>(itemList.size());

        for (int i = 0; i < itemList.size(); i++) {
            PyObject item = itemList.get(i);
            PyObject contextExpr = this.runtime.getattr(item, "context_expr");
            PyObject optionalVars = this.runtime.getattr(item, "optional_vars");

            PyObject evalContextExpr = eval(context, contextExpr);

            if (!this.runtime.hasattr(evalContextExpr, __exit__)) {
                throw this.runtime.newRaiseException("AttributeError", __exit__);
            }

            if (!this.runtime.hasattr(evalContextExpr, __enter__)) {
                throw this.runtime.newRaiseException("AttributeError", __enter__);
            }

            if (!optionalVars.isNone()) {
                assign(lexicalContext, optionalVars, evalContextExpr);
            }

            evalContextExprList.add(evalContextExpr);
        }

        for (int i = 0; i < evalContextExprList.size(); i++) {
            PyObject evalContextExpr = evalContextExprList.get(i);
            PyObject enter = this.runtime.getattr(evalContextExpr, __enter__);

            enter.call();
        }

        PyObject result = null;
        RaiseException raiseException = null;
        try {
            result = eval(lexicalContext, body);

        } catch (RaiseException e) {
            raiseException = e;

        } finally {
            PyObject exception = raiseException != null ? raiseException.getException() : this.runtime.None();

            for (int i = evalContextExprList.size() - 1; i >= 0; i--) {
                PyObject evalContextExpr = evalContextExprList.get(i);
                PyObject enter = this.runtime.getattr(evalContextExpr, __exit__);

                PyObject exceptionValue = this.runtime.None();
                if (!exception.isNone()) {
                    exceptionValue = this.runtime.getattr(exception, "args");
                    List<PyObject> argList = exceptionValue.toJava(List.class);
                    if (argList.size() == 1) {
                        exceptionValue = argList.get(0);
                    }
                }

                enter.call(exception.getType(), exceptionValue, this.runtime.None()); // FIXME None
            }

            if (result != null) {
                return result;
            }
            if (raiseException != null) {
                throw raiseException;
            }

            throw new CafeBabePyException("Fail evalWith");
        }
    }

    private PyObject evalFor(PyObject context, PyObject node) {
        PyObject target = this.runtime.getattr(node, "target");
        PyObject iter = this.runtime.getattr(node, "iter");
        PyObject body = this.runtime.getattr(node, "body");
        PyObject orelse = this.runtime.getattr(node, "orelse");

        PyObject evalIter = eval(context, iter);

        try {
            this.runtime.iter(evalIter, next -> {
                assign(context, target, next);
                try {
                    eval(context, body);

                } catch (InterpretContinue ignore) {
                }
            });

            eval(context, orelse);

        } catch (InterpretBreak ignore) {
        }

        return this.runtime.None();
    }

    private PyObject evalList(PyObject context, PyObject node) {
        PyObject elts = this.runtime.getattr(node, "elts");

        List<PyObject> elements = new ArrayList<>();
        this.runtime.iter(elts, elt -> {
            PyObject type = elt.getType();
            if (type instanceof PyStarredType) {
                PyObject value = this.runtime.getattr(elt, "value");
                PyObject evalValue = eval(context, value);

                this.runtime.iter(evalValue, elements::add);

            } else {
                PyObject evalValue = eval(context, elt);

                elements.add(evalValue);
            }
        });

        return this.runtime.list(elements);
    }

    private PyObject evalListComp(PyObject context, PyObject node) {
        List<PyObject> result = evalComp(context, node);

        return this.runtime.list(result);
    }

    private PyObject evalGeneratorExp(PyObject context, PyObject node) {
        List<PyObject> result = evalComp(context, node);

        return this.runtime.tuple(result);
    }

    @SuppressWarnings("unchecked")
    private List<PyObject> evalComp(PyObject context, PyObject node) {
        PyObject elt = this.runtime.getattr(node, "elt");
        PyObject generators = this.runtime.getattr(node, "generators");

        List<PyObject> generatorList = (List<PyObject>) generators.toJava(List.class);
        List<PyObject> resultList = new ArrayList<>();

        PyLexicalScopeProxyObject lexicalContext = new PyLexicalScopeProxyObject(context);
        evalGenerators(lexicalContext, elt, generatorList, resultList);

        return resultList;
    }

    @SuppressWarnings("unchecked")
    private void evalGenerators(PyObject context, PyObject elt, List<PyObject> generators, List<PyObject> resultList) {
        PyObject generator = generators.get(0);
        PyObject target = this.runtime.getattr(generator, "target");
        PyObject iter = this.runtime.getattr(generator, "iter");
        PyObject ifs = this.runtime.getattr(generator, "ifs");
        PyObject is_async = this.runtime.getattr(generator, "is_async");

        PyObject evalIter = eval(context, iter);

        List<PyObject> ifList;
        if (ifs.isNone()) {
            ifList = Collections.emptyList();

        } else {
            ifList = (List<PyObject>) ifs.toJava(List.class);
        }
        this.runtime.iter(evalIter, next -> {
            assign(context, target, next);
            for (int i = 0; i < ifList.size(); i++) {
                PyObject result = eval(context, ifList.get(i));
                if (result.isFalse()) {
                    return;
                }
            }

            if (generators.size() == 1) {
                resultList.add(eval(context, elt));

            } else {
                List<PyObject> gs = generators.subList(1, generators.size());

                evalGenerators(context, elt, gs, resultList);
            }
        });
    }

    private PyObject evalExpr(PyObject context, PyObject node) {
        PyObject value = this.runtime.getattr(node, "value");

        return eval(context, value);
    }

    private PyObject evalPass(PyObject context, PyObject node) {
        return this.runtime.None();
    }

    private PyObject evalBreak(PyObject context, PyObject node) {
        throw new InterpretBreak();
    }

    private PyObject evalContinue(PyObject context, PyObject node) {
        throw new InterpretContinue();
    }

    private PyObject evalAssign(PyObject context, PyObject node) {
        PyObject targets = this.runtime.getattr(node, "targets");
        PyObject value = this.runtime.getattr(node, "value");
        PyObject evalValue = eval(context, value);

        this.runtime.iter(targets, target -> assign(context, target, evalValue));

        return this.runtime.None();
    }

    void assign(PyObject context, PyObject target, PyObject evalValue) {
        if (target instanceof PyNameType) {
            PyObject id = this.runtime.getattr(target, "id");
            context.getScope().put(id, evalValue);

        } else {
            unpack(context, target, evalValue);
        }
    }

    @SuppressWarnings("unchecked")
    private void unpack(PyObject context, PyObject target, PyObject evalValue) {
        PyObject targetType = target.getType();

        PyObject targets;

        if (targetType instanceof PyNameType) {
            PyObject id = this.runtime.getattr(target, "id");
            context.getScope().put(id, evalValue);
            return;

        } else if (targetType instanceof PyAttributeType) {
            PyObject attr = this.runtime.getattr(target, "attr");
            PyObject attributeContext = eval(context, target);
            attributeContext.getScope().put(attr, evalValue);
            return;

        } else if (targetType instanceof PyStarredType) {
            PyObject value = this.runtime.getattr(target, "value");
            unpack(context, value, evalValue);
            return;

        } else if (targetType instanceof PyListType) {
            targets = this.runtime.getattr(target, "elts");

        } else if (targetType instanceof PyTupleType) {
            targets = this.runtime.getattr(target, "elts");

        } else if (targetType instanceof PySubscriptType) {
            PyObject value = this.runtime.getattr(target, "value");
            PyObject slice = this.runtime.getattr(target, "slice");

            PyObject evalTarget = eval(context, value);

            Optional<PyObject> setattrOpt = this.runtime.getattrOptional(evalTarget, __setitem__);
            if (!setattrOpt.isPresent()) {
                throw this.runtime.newRaiseTypeError("'" + evalTarget.getFullName() + "' object does not support item assignment");
            }

            PyObject evalKey = eval(context, slice);

            setattrOpt.get().call(evalKey, evalValue);
            return;

        } else if (this.runtime.isInstance(target, "function", false)) {
            throw this.runtime.newRaiseTypeError("can't assign to function");

        } else {
            throw this.runtime.newRaiseTypeError("can't assign to literal");
        }

        List<PyObject> targetPyList = (List<PyObject>) targets.toJava(List.class);
        List<PyObject> evalValuePyList = (List<PyObject>) evalValue.toJava(List.class);

        int notStarredFirstCount = 0;
        int notStarredLastCount = 0;

        boolean throughStar;

        throughStar = false;
        for (int i = 0; i < targetPyList.size(); i++) {
            PyObject childTargetType = targetPyList.get(i).getType();
            if (childTargetType instanceof PyStarredType) {
                if (throughStar) {
                    throw this.runtime.newRaiseException("builtins.SyntaxError",
                            "two starred expressions in assignment");
                }
                throughStar = true;

            } else {
                if (throughStar) {
                    notStarredLastCount++;

                } else {
                    notStarredFirstCount++;
                }

            }
        }

        int targetMinCount = notStarredFirstCount + notStarredLastCount;
        if (targetMinCount > evalValuePyList.size()) {
            throw this.runtime.newRaiseException("builtins.ValueError",
                    "not enough values to unpack (expected "
                            + targetMinCount + ", got " + evalValuePyList.size() + ")");
        }

        int valueIndex = 0;

        PyObject starredTarget = null;
        List<PyObject> starredValueList = new ArrayList<>();
        for (int i = 0; i < targetPyList.size(); i++) {
            PyObject t = targetPyList.get(i);
            PyObject tt = t.getType();

            if (tt instanceof PyStarredType) {
                starredTarget = t;
                int count = evalValuePyList.size() - notStarredLastCount;
                while (valueIndex < count) {
                    starredValueList.add(evalValuePyList.get(valueIndex));
                    valueIndex++;
                }

            } else {
                if (starredTarget != null) {
                    PyObject value = this.runtime.list(starredValueList);
                    unpack(context, starredTarget, value);

                    starredTarget = null;
                    starredValueList = null;
                }
                unpack(context, t, evalValuePyList.get(valueIndex));
                valueIndex++;
            }
        }

        if (starredTarget != null) {
            PyObject value = this.runtime.list(starredValueList);
            unpack(context, starredTarget, value);
        }

        if (valueIndex < evalValuePyList.size()) {
            throw this.runtime.newRaiseException("builtins.ValueError",
                    "too many values to unpack (expected " + targetPyList.size() + ")");
        }
    }

    private PyObject evalAnnassign(PyObject context, PyObject node) {
        PyObject target = this.runtime.getattr(node, "target");
        PyObject value = this.runtime.getattr(node, "value");
        if (!value.isNone()) {
            PyObject id = this.runtime.getattr(target, "id");
            PyObject evalValue = eval(context, value);

            context.getScope().put(id, evalValue);
        }

        return this.runtime.None();
    }

    @SuppressWarnings("unchecked")
    private PyObject evalCall(PyObject context, PyObject node) {
        PyObject func = this.runtime.getattr(node, "func");
        PyObject funcEval = eval(context, func);

        PyObject args = this.runtime.getattr(node, "args");
        PyObject keywords = this.runtime.getattr(node, "keywords");

        PyObject[] argsArray;
        LinkedHashMap<String, PyObject> keywordsMap = new LinkedHashMap<>();

        List<PyObject> argList = new ArrayList<>();
        this.runtime.iter(args, arg -> {
            if (this.runtime.isInstance(arg, "_ast.Starred")) {
                PyObject value = this.runtime.getattr(arg, "value");
                PyObject evalValue = eval(context, value);

                if (!this.runtime.isIterable(evalValue)) {
                    String name = this.runtime.getattr(func, "id").toJava(String.class);

                    throw this.runtime.newRaiseTypeError(name + "() argument after * must be an iterable, not " + evalValue.getFullName());
                }

                this.runtime.iter(evalValue, argList::add);

            } else {
                PyObject evalArg = eval(context, arg);

                argList.add(evalArg);
            }
        });

        LinkedHashMap<String, PyObject> doubleStarKeywordsMap = new LinkedHashMap<>();
        this.runtime.iter(keywords, keyword -> {
            PyObject arg = this.runtime.getattr(keyword, "arg");
            PyObject value = this.runtime.getattr(keyword, "value");
            PyObject evalValue = eval(context, value);

            if (arg.isNone()) {
                LinkedHashMap<PyObject, PyObject> dictMap = evalValue.toJava(LinkedHashMap.class);
                for (PyObject key : dictMap.keySet()) {
                    doubleStarKeywordsMap.put(key.toJava(String.class), dictMap.get(key));
                }

            } else {
                keywordsMap.put(arg.toJava(String.class), evalValue);
            }
        });

        keywordsMap.putAll(doubleStarKeywordsMap);

        argsArray = new PyObject[argList.size()];
        argList.toArray(argsArray);

        return funcEval.call(argsArray, keywordsMap);
    }

    private PyObject evalSubscript(PyObject context, PyObject node) {
        PyObject value = this.runtime.getattr(node, "value");
        PyObject slice = this.runtime.getattr(node, "slice");
        PyObject ctx = this.runtime.getattr(node, "ctx");

        if (this.runtime.isInstance(slice, "_ast.Index")) {
            PyObject evalValue = eval(context, value);

            Optional<PyObject> getattrOpt = this.runtime.getattrOptional(evalValue, __getitem__);
            if (!getattrOpt.isPresent()) {
                throw this.runtime.newRaiseTypeError("'" + evalValue.getFullName() + "' object is not subscriptable");
            }

            PyObject evalKey = eval(context, slice);
            return getattrOpt.get().call(evalKey);
        }

        // TODO あとで実装
        return this.runtime.NotImplemented();
    }

    private PyObject evalIndex(PyObject context, PyObject node) {
        PyObject value = this.runtime.getattr(node, "value");

        return eval(context, value);
    }

    private PyObject evalSlice(PyObject context, PyObject node) {
        PyObject lower = this.runtime.getattr(node, "lower");
        PyObject upper = this.runtime.getattr(node, "upper");
        PyObject step = this.runtime.getattr(node, "step");

        PyObject evalLower = eval(context, lower);
        PyObject evalUpper = eval(context, upper);
        PyObject evalStep = eval(context, step);

        return this.runtime.newPyObject("builtins.slice", evalLower, evalUpper, evalStep);
    }

    private PyObject evalReturn(PyObject context, PyObject node) {
        PyObject value = this.runtime.getattr(node, "value");
        PyObject evalValue = eval(context, value);

        throw new InterpretReturn(evalValue);
    }

    private PyObject evalCompare(PyObject context, PyObject node) {

        // 1 < 2 < 3 < 4 => 1 < 2 && 2 < 3 && 3 < 4
        // 1 < 2 < 3 => 1 < 2 && 2 < 3
        // 1 < 2 => 1 < 2
        PyObject comparators = this.runtime.getattr(node, "comparators");

        List<PyObject> comparatorList = getLinkedList(comparators);
        PyObject left = this.runtime.getattr(node, "left");
        comparatorList.add(0, left);

        PyObject ops = this.runtime.getattr(node, "ops");
        List<PyObject> opList = getLinkedList(ops);

        PyObject ltType = this.runtime.typeOrThrow("_ast.Lt");
        PyObject gtType = this.runtime.typeOrThrow("_ast.Gt");
        PyObject eqType = this.runtime.typeOrThrow("_ast.Eq");
        PyObject gteType = this.runtime.typeOrThrow("_ast.GtE");
        PyObject lteType = this.runtime.typeOrThrow("_ast.LtE");
        PyObject notEqType = this.runtime.typeOrThrow("_ast.NotEq");
        PyObject inType = this.runtime.typeOrThrow("_ast.In");
        PyObject notInType = this.runtime.typeOrThrow("_ast.NotIn");
        PyObject isType = this.runtime.typeOrThrow("_ast.Is");
        PyObject isNotType = this.runtime.typeOrThrow("_ast.IsNot");

        boolean evalResult = true;
        for (int i = 0; i < comparatorList.size() - 1; i++) {
            PyObject evalLeft = eval(context, comparatorList.get(i));
            PyObject evalRight = eval(context, comparatorList.get(i + 1));
            PyObject op = opList.get(i);

            if (this.runtime.isInstance(op, ltType)) {
                evalResult &= this.runtime.lt(evalLeft, evalRight).isTrue();

            } else if (this.runtime.isInstance(op, gtType)) {
                evalResult &= this.runtime.gt(evalLeft, evalRight).isTrue();

            } else if (this.runtime.isInstance(op, eqType)) {
                evalResult &= this.runtime.eq(evalLeft, evalRight).isTrue();

            } else if (this.runtime.isInstance(op, gteType)) {
                evalResult &= this.runtime.ge(evalLeft, evalRight).isTrue();

            } else if (this.runtime.isInstance(op, lteType)) {
                evalResult &= this.runtime.le(evalLeft, evalRight).isTrue();

            } else if (this.runtime.isInstance(op, notEqType)) {
                evalResult &= this.runtime.ne(evalLeft, evalRight).isTrue();

            } else if (this.runtime.isInstance(op, inType)) {
                evalResult &= this.runtime.contains(evalRight, evalLeft).isTrue();

            } else if (this.runtime.isInstance(op, notInType)) {
                evalResult &= !this.runtime.contains(evalRight, evalLeft).isTrue();

            } else if (this.runtime.isInstance(op, isType)) {
                evalResult &= (evalLeft == evalRight);

            } else if (this.runtime.isInstance(op, isNotType)) {
                evalResult &= !(evalLeft == evalRight);

            } else {
                throw this.runtime.newRaiseTypeError("Unknown AST '" + op.getType().getFullName() + "'");
            }
            if (!evalResult) {
                break;
            }
        }

        return this.runtime.bool(evalResult);
    }

    private LinkedList<PyObject> getLinkedList(PyObject object) {
        LinkedList<PyObject> list = new LinkedList<>();

        this.runtime.iter(object, (comparator) -> {
            list.add(comparator);
        });

        return list;
    }

    private PyObject evalBinOp(PyObject context, PyObject node) {
        PyObject left = this.runtime.getattr(node, "left");
        PyObject evalLeft = eval(context, left);

        PyObject right = this.runtime.getattr(node, "right");
        PyObject evalRight = eval(context, right);

        PyObject addType = this.runtime.typeOrThrow("_ast.Add");
        PyObject subType = this.runtime.typeOrThrow("_ast.Sub");
        PyObject modType = this.runtime.typeOrThrow("_ast.Mod");
        PyObject multType = this.runtime.typeOrThrow("_ast.Mult");

        PyObject op = this.runtime.getattr(node, "op");
        if (this.runtime.isInstance(op, addType)) {
            return this.runtime.add(evalLeft, evalRight);

        } else if (this.runtime.isInstance(op, subType)) {
            return this.runtime.sub(evalLeft, evalRight);

        } else if (this.runtime.isInstance(op, modType)) {
            return this.runtime.mod(evalLeft, evalRight);

        } else if (this.runtime.isInstance(op, multType)) {
            return this.runtime.mul(evalLeft, evalRight);
        }

        throw new CafeBabePyException("operator '" + op.getName() + "' not found");
    }

    private PyObject evalUnaryOp(PyObject context, PyObject node) {
        PyObject op = this.runtime.getattr(node, "op");
        PyObject operand = this.runtime.getattr(node, "operand");

        PyObject evalOperand = eval(context, operand);

        PyObject uAddType = this.runtime.typeOrThrow("_ast.UAdd");
        PyObject uSubType = this.runtime.typeOrThrow("_ast.USub");
        PyObject invertType = this.runtime.typeOrThrow("_ast.Invert");
        PyObject notType = this.runtime.typeOrThrow("_ast.Not");

        if (this.runtime.isInstance(op, uAddType)) {
            PyObject pos = this.runtime.getattr(evalOperand, __pos__);
            return pos.call();

        } else if (this.runtime.isInstance(op, uSubType)) {
            PyObject pos = this.runtime.getattr(evalOperand, __neg__);
            return pos.call();

        } else if (this.runtime.isInstance(op, invertType)) {
            PyObject pos = this.runtime.getattr(evalOperand, __invert__);
            return pos.call();

        } else if (this.runtime.isInstance(op, notType)) {
            PyObject bool = this.runtime.getattr(evalOperand, __bool__);
            PyObject result = bool.call();
            if (result.isTrue()) {
                return this.runtime.False();

            } else {
                return this.runtime.True();
            }

        } else {
            throw this.runtime.newRaiseTypeError("Unknown op");
        }
    }

    private PyObject evalLambda(PyObject context, PyObject node) {
        PyObject args = this.runtime.getattr(node, "args");
        PyObject body = this.runtime.getattr(node, "body");

        PyObject function = new PyInterpretFunctionObject(this.runtime, "<lambda>", context, args, body);
        function.initialize();

        return function;
    }

    private PyObject evalName(PyObject context, PyObject node) {
        PyObject ctx = this.runtime.getattr(node, "ctx");

        PyObject ctxType = ctx.getType();
        if (ctxType instanceof PyLoadType) {
            PyObject id = this.runtime.getattr(node, "id");
            String name = id.toJava(String.class);

            return Python.lookup(context, id).orElseThrow(() ->
                    this.runtime.newRaiseException("builtins.NameError",
                            "name '" + name + "' is not defined")
            );

            /*
            return this.runtime.getattrOptional(context, name).orElseThrow(() ->
                    this.runtime.newRaiseException("builtins.NameError",
                            "name '" + name + "' is not defined")
            );
            */

        } else if (ctxType instanceof PyStoreType) {
            return context;
        }

        // TODO どうする？
        return node;
    }

    private PyObject evalNum(PyObject context, PyObject node) {
        return this.runtime.getattr(node, "n");
    }

    private PyObject evalStr(PyObject context, PyObject node) {
        return this.runtime.getattr(node, "s");
    }

    private PyObject evalFormattedValue(PyObject context, PyObject node) {
        PyObject value = this.runtime.getattr(node, "value");
        PyObject conversion = this.runtime.getattr(node, "conversion");
        PyObject format_spec = this.runtime.getattr(node, "format_spec");

        PyObject evalValue = eval(context, value);
        // FIXME conversion

        return this.runtime.getattr(evalValue, __format__).call(format_spec);
    }

    private PyObject evalJoinedStr(PyObject context, PyObject node) {
        PyObject values = this.runtime.getattr(node, "values");

        StringBuilder builder = new StringBuilder();
        this.runtime.iter(values, value -> builder.append(eval(context, value).toJava(String.class)));

        return this.runtime.str(builder.toString());
    }

    private PyObject evalAttribute(PyObject context, PyObject node) {
        PyObject value = this.runtime.getattr(node, "value");
        PyObject attr = this.runtime.getattr(node, "attr");
        PyObject ctx = this.runtime.getattr(node, "ctx");

        PyObject evalValue = eval(context, value);

        PyObject ctxType = ctx.getType();
        if (ctxType instanceof PyLoadType) {
            return this.runtime.getattr(evalValue, attr.toJava(String.class));

        } else if (ctxType instanceof PyStoreType) {
            return evalValue;
        }

        //　TODO どうする？
        return node;
    }

    @SuppressWarnings("unchecked")
    private PyObject evalDict(PyObject context, PyObject node) {
        PyObject keys = this.runtime.getattr(node, "keys");
        PyObject values = this.runtime.getattr(node, "values");

        List<PyObject> keyList = new ArrayList<>();
        List<PyObject> valueList = new ArrayList<>();

        this.runtime.iter(keys, keyList::add);
        this.runtime.iter(values, valueList::add);

        LinkedHashMap<PyObject, PyObject> map = new LinkedHashMap<>();

        for (int i = 0; i < keyList.size(); i++) {
            PyObject key = eval(context, keyList.get(i));
            PyObject value = eval(context, valueList.get(i));

            if (key.isNone()) {
                Map<PyObject, PyObject> vs = value.toJava(Map.class);
                // TODO Error処理
                map.putAll(vs);

            } else {
                map.put(key, value);
            }
        }

        return this.runtime.dict(map);
    }

    private PyObject evalTuple(PyObject context, PyObject node) {
        PyObject elts = this.runtime.getattr(node, "elts");

        List<PyObject> elements = new ArrayList<>();
        this.runtime.iter(elts, elt -> {
            PyObject type = elt.getType();
            if (type instanceof PyStarredType) {
                PyObject value = this.runtime.getattr(elt, "value");
                PyObject evalValue = eval(context, value);

                this.runtime.iter(evalValue, elements::add);

            } else {
                PyObject evalValue = eval(context, elt);

                elements.add(evalValue);
            }
        });

        return this.runtime.tuple(elements);
    }
}