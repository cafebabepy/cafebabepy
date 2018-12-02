package org.cafebabepy.evaluter.Interpret;

import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.RaiseException;
import org.cafebabepy.runtime.module._ast.*;

import java.util.*;

import static org.cafebabepy.util.ProtocolNames.*;

/**
 * Created by yotchang4s on 2017/06/09.
 */
public class InterpretEvaluator {
    final Map<PyObject, Yielder<PyObject>> yielderMap = Collections.synchronizedMap(new HashMap<>());
    private Python runtime;
    private ImportManager importManager;

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

    public InterpretEvaluator(Python runtime) {
        this.runtime = runtime;
        this.importManager = new ImportManager(runtime);
    }

    private static Optional<PyObject> lookupType(PyObject object, String name) {
        for (PyObject type : object.getTypes()) {
            Optional<PyObject> typeObject = type.getFrame().getFromGlobals(name);
            if (typeObject.isPresent()) {
                return typeObject;
            }
        }

        return Optional.empty();
    }

    public PyObject eval(PyObject node) {
        if (node.isNone()) {
            return node;

        } else if (node.isEllipsis()) {
            return node;

        } else if (this.runtime.isInstance(node, "builtins.bool")) {
            return node;
        }

        if (this.runtime.isInstance(node, "builtins.list")) {
            PyObject[] end = new PyObject[1];
            this.runtime.iter(node, n -> end[0] = eval(n));
            if (end[0] == null) {
                return this.runtime.None();

            } else {
                return end[0];
            }
        }

        switch (node.getName()) {
            case "Module":
                return evalModule(node);

            case "Interactive":
                return evalInteractive(node);

            case "Suite":
                return evalSuite(node);

            case "Import":
                return evalImport(node);

            case "ImportFrom":
                return evalImportFrom(node);

            case "AsyncFunctionDef":
                return evalAsyncFunctionDef(node);

            case "FunctionDef":
                return evalFunctionDef(node);

            case "ClassDef":
                return evalClassDef(node);

            case "If":
                return evalIfAndIfExp(node);

            case "Raise":
                return evalRaise(node);

            case "Try":
                return evalTry(node);

            case "With":
                return evalWith(node);

            case "While":
                return evalWhile(node);

            case "For":
                return evalFor(node);

            case "Expr":
                return evalExpr(node);

            case "Pass":
                return evalPass(node);

            case "Break":
                return evalBreak(node);

            case "Continue":
                return evalContinue(node);

            case "Assign":
                return evalAssign(node);

            case "Annassign":
                return evalAnnassign(node);

            case "IfExp":
                return evalIfAndIfExp(node);

            case "List":
                return evalList(node);

            case "ListComp":
                return evalListComp(node);

            case "GeneratorExp":
                return evalGeneratorExp(node);

            case "Yield":
                return evalYield(node);

            case "BinOp":
                return evalBinOp(node);

            case "UnaryOp":
                return evalUnaryOp(node);

            case "Lambda":
                return evalLambda(node);

            case "Call":
                return evalCall(node);

            case "Return":
                return evalReturn(node);

            case "Compare":
                return evalCompare(node);

            case "Name":
                return evalName(node);

            case "Num":
                return evalNum(node);

            case "Str":
                return evalStr(node);

            case "Bytes":
                return evalBytes(node);

            case "FormattedValue":
                return evalFormattedValue(node);

            case "JoinedStr":
                return evalJoinedStr(node);

            case "Attribute":
                return evalAttribute(node);

            case "Dict":
                return evalDict(node);

            case "Tuple":
                return evalTuple(node);

            case "Subscript":
                return evalSubscript(node);

            case "Index":
                return evalIndex(node);

            case "Slice":
                return evalSlice(node);
        }

        throw new CafeBabePyException("Unknown AST '" + node.getName() + "'");
    }

    public PyObject loadModule(String moduleName) {
        return this.importManager.loadModule(moduleName);
    }

    private PyObject evalModule(PyObject node) {
        PyObject body = this.runtime.getattr(node, "body");
        return eval(body);
    }

    private PyObject evalInteractive(PyObject node) {
        PyObject body = this.runtime.getattr(node, "body");
        return eval(body);
    }

    private PyObject evalSuite(PyObject node) {
        PyObject body = this.runtime.getattr(node, "body");

        if (this.runtime.isIterable(body)) {
            PyObject[] result = new PyObject[1];
            result[0] = this.runtime.None();

            this.runtime.iter(body, b -> {
                result[0] = eval(b);
            });

            return result[0];

        } else {
            PyObject result = eval(body);

            return result;
        }
    }

    private PyObject evalImport(PyObject node) {
        PyObject names = this.runtime.getattr(node, "names");

        this.runtime.iter(names, n -> {
            PyObject name = this.runtime.getattr(n, "name");
            PyObject asname = this.runtime.getattr(n, "asname");

            this.importManager.importAsName(name, asname);
        });

        return this.runtime.None();
    }

    private PyObject evalImportFrom(PyObject node) {
        PyObject module = this.runtime.getattr(node, "module");
        PyObject names = this.runtime.getattr(node, "names");
        PyObject level = this.runtime.getattr(node, "level");

        this.importManager.importFrom(module, names, level);

        return this.runtime.None();
    }

    private PyObject evalAsyncFunctionDef(PyObject node) {
        return evalFunctionDefImpl(node, true);
    }

    private PyObject evalFunctionDef(PyObject node) {
        return evalFunctionDefImpl(node, false);
    }

    private PyObject evalFunctionDefImpl(PyObject node, boolean async) {
        PyObject name = this.runtime.getattr(node, "name");
        PyObject args = this.runtime.getattr(node, "args");
        PyObject body = this.runtime.getattr(node, "body");
        PyObject decorator_list = this.runtime.getattr(node, "decorator_list");
        PyObject returns = this.runtime.getattr(node, "returns"); // FIXME ???

        PyObject function = new PyInterpretFunctionObject(this.runtime, name.toJava(String.class), args, body);
        function.initialize();
        function.getFrame().putToNotAppearLocals("_async", this.runtime.bool(async));

        List<PyObject> decorators = new ArrayList<>();
        this.runtime.iter(decorator_list, decorators::add);
        Collections.reverse(decorators);

        PyObject decoratorEvalValue = function;

        int decoratorCount = decorators.size();
        for (int i = 0; i < decoratorCount; i++) {
            PyObject decorator = decorators.get(i);

            PyObject decoratorEvalFunction = eval(decorator);
            decoratorEvalValue = decoratorEvalFunction.call(decoratorEvalValue);
        }

        this.runtime.setattr(this.runtime.getCurrentContext(), name.toJava(String.class), decoratorEvalValue);

        return this.runtime.None();
    }

    private PyObject evalClassDef(PyObject node) {
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

            PyObject evalBase = eval(base);
            baseList.add(evalBase);
        });

        String jname = name.toJava(String.class);
        String n = jname;

        PyObject c = this.runtime.getCurrentContext();
        if (!c.isModule()) {
            n = c.getName() + "." + n;
        }

        LinkedHashMap<String, PyObject> keywordsMap = new LinkedHashMap<>();
        this.runtime.iter(keywords, keyword -> {
            PyObject arg = this.runtime.getattr(keyword, "arg");
            PyObject value = this.runtime.getattr(keyword, "value");
            PyObject evalValue = eval(value);

            keywordsMap.put(arg.toJava(String.class), evalValue);
        });

        PyObject clazz = newClass(jname, baseList, keywordsMap);
        this.runtime.pushContext(clazz);
        try {
            eval(body);

        } finally {
            this.runtime.popContext();
        }

        this.runtime.setattr(c, jname, clazz);

        return this.runtime.None();
    }

    private PyObject newClass(String name, List<PyObject> bases, LinkedHashMap<String, PyObject> kwds) {
        this.runtime.pushNewContext();
        try {
            PrepareClassReturn result = prepareClass(name, bases, kwds);

            PyObject[] args = new PyObject[3];
            args[0] = this.runtime.str(name);
            args[1] = this.runtime.tuple(bases);
            args[2] = result.ns;

            PyObject newClass = result.meta.call(args, result.kwds);
            if (newClass instanceof PyInterpretClassObject) {
                ((PyInterpretClassObject) newClass).setContext(this.runtime.getCurrentContext());
            }

            return newClass;

        } finally {
            this.runtime.popContext();
        }
    }

    private PrepareClassReturn prepareClass(String name, List<PyObject> bases, LinkedHashMap<String, PyObject> kwds) {
        PyObject type = this.runtime.typeOrThrow("type");

        PyObject meta = kwds.get("metaclass");
        if (meta != null) {
            kwds.remove("metaclass");

        } else {
            if (!bases.isEmpty()) {
                meta = bases.get(0).getType();

            } else {
                meta = type;
            }
        }

        if (this.runtime.isInstance(meta, type)) {
            meta = calculateMeta(meta, bases);
        }

        LinkedHashMap<String, PyObject> finalKwds = kwds;

        PyObject ns = this.runtime.getattrOptional(meta, __prepare__)
                .map(prepare -> {
                    PyObject[] args = new PyObject[2];
                    args[0] = this.runtime.str(name);
                    args[1] = this.runtime.tuple(bases);

                    return prepare.call(args, finalKwds);
                })
                .orElseGet(() -> this.runtime.dict());

        PrepareClassReturn result = new PrepareClassReturn();
        result.meta = meta;
        result.ns = ns;
        result.kwds = kwds;

        return result;
    }

    private PyObject calculateMeta(PyObject meta, List<PyObject> bases) {
        PyObject winner = meta;

        for (int i = 0; i < bases.size(); i++) {
            PyObject base = bases.get(i);

            PyObject baseMeta = base.getType();
            if (this.runtime.isSubClass(winner, baseMeta)) {
                continue;
            }
            if (this.runtime.isSubClass(baseMeta, winner)) {
                winner = baseMeta;
                continue;
            }

            throw this.runtime.newRaiseTypeError(
                    "metaclass conflict: the metaclass of a derived class must be a (non-strict) subclass of the metaclasses of all its bases");
        }

        return winner;
    }

    private PyObject evalIfAndIfExp(PyObject node) {
        PyObject test = this.runtime.getattr(node, "test");
        PyObject body = this.runtime.getattr(node, "body");
        PyObject orElse = this.runtime.getattr(node, "orelse");

        PyObject evalTest = eval(test);
        if (evalTest.isTrue()) {
            return eval(body);

        } else {
            return eval(orElse);
        }
    }

    private PyObject evalRaise(PyObject node) {
        PyObject exc = this.runtime.getattr(node, "exc");

        // FIXME raise Exception from xxx
        PyObject cause = this.runtime.getattr(node, "cause");

        PyObject evalExc = eval(exc);
        if (!this.runtime.isInstance(evalExc, "BaseException")) {
            throw this.runtime.newRaiseTypeError("exceptions must derive from BaseException");
        }

        throw this.runtime.newRaiseException(evalExc);
    }

    private PyObject evalTry(PyObject node) {
        PyObject body = this.runtime.getattr(node, "body");
        PyObject handlers = this.runtime.getattr(node, "handlers");
        PyObject orelse = this.runtime.getattr(node, "orelse");
        PyObject finalbody = this.runtime.getattr(node, "finalbody");

        PyObject result;
        RaiseException elseException = null;

        try {
            result = eval(body);
            if (!orelse.isNone()) {
                try {
                    result = eval(orelse);

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
                PyObject evalType = eval(type);

                if (this.runtime.isInstance(exception, evalType)) {
                    PyObject name = this.runtime.getattr(handler, "name");
                    String javaName = name.toJava(String.class);

                    PyObject exceptBody = this.runtime.getattr(handler, "body");
                    try {
                        if (!name.isNone()) {
                            this.runtime.setattr(this.runtime.getCurrentContext(), javaName, exception);
                        }
                        return eval(exceptBody);

                    } finally {
                        if (!name.isNone()) {
                            this.runtime.getCurrentContext().getFrame().removeToLocals(javaName);
                        }
                    }
                }
            }

            throw e;

        } finally {
            if (!finalbody.isNone()) {
                return eval(finalbody);
            }
        }

        if (elseException != null) {
            throw elseException;
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private PyObject evalWith(PyObject node) {
        PyObject items = this.runtime.getattr(node, "items");
        PyObject body = this.runtime.getattr(node, "body");

        List<PyObject> itemList = new ArrayList<>();
        this.runtime.iter(items, itemList::add);

        this.runtime.pushNewContext();
        try {
            List<PyObject> evalContextExprList = new ArrayList<>(itemList.size());

            for (int i = 0; i < itemList.size(); i++) {
                PyObject item = itemList.get(i);
                PyObject contextExpr = this.runtime.getattr(item, "context_expr");
                PyObject optionalVars = this.runtime.getattr(item, "optional_vars");

                PyObject evalContextExpr = eval(contextExpr);

                if (!this.runtime.hasattr(evalContextExpr, __exit__)) {
                    throw this.runtime.newRaiseException("AttributeError", __exit__);
                }

                if (!this.runtime.hasattr(evalContextExpr, __enter__)) {
                    throw this.runtime.newRaiseException("AttributeError", __enter__);
                }

                if (!optionalVars.isNone()) {
                    assign(optionalVars, evalContextExpr);
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
                result = eval(body);

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

        } finally {
            this.runtime.popContext();
        }
    }

    private PyObject evalWhile(PyObject node) {
        PyObject test = this.runtime.getattr(node, "test");
        PyObject body = this.runtime.getattr(node, "body");
        PyObject orelse = this.runtime.getattr(node, "orelse");

        try {
            while (true) {
                PyObject evalTest = eval(test);
                if (evalTest.isFalse()) {
                    break;
                }
                try {
                    eval(body);

                } catch (InterpretContinue ignore) {
                }
            }

            eval(orelse);

        } catch (InterpretBreak ignore) {
        }

        return this.runtime.None();
    }

    private PyObject evalFor(PyObject node) {
        PyObject target = this.runtime.getattr(node, "target");
        PyObject iter = this.runtime.getattr(node, "iter");
        PyObject body = this.runtime.getattr(node, "body");
        PyObject orelse = this.runtime.getattr(node, "orelse");

        PyObject evalIter = eval(iter);

        try {
            this.runtime.iter(evalIter, next -> {
                assign(target, next);
                try {
                    eval(body);

                } catch (InterpretContinue ignore) {
                }
            });

            eval(orelse);

        } catch (InterpretBreak ignore) {
        }

        return this.runtime.None();
    }

    private PyObject evalList(PyObject node) {
        PyObject elts = this.runtime.getattr(node, "elts");

        List<PyObject> elements = new ArrayList<>();
        this.runtime.iter(elts, elt -> {
            PyObject type = elt.getType();
            if (type instanceof PyStarredType) {
                PyObject value = this.runtime.getattr(elt, "value");
                PyObject evalValue = eval(value);

                this.runtime.iter(evalValue, elements::add);

            } else {
                PyObject evalValue = eval(elt);

                elements.add(evalValue);
            }
        });

        return this.runtime.list(elements);
    }

    private PyObject evalListComp(PyObject node) {
        List<PyObject> result = evalComp(node);

        return this.runtime.list(result);
    }

    private PyObject evalGeneratorExp(PyObject node) {
        List<PyObject> result = evalComp(node);

        return this.runtime.tuple(result);
    }

    private PyObject evalYield(PyObject node) {
        PyObject value = this.runtime.getattr(node, "value");
        PyObject evalValue = eval(value);

        Yielder<PyObject> yielder = this.yielderMap.get(node);
        if (yielder == null) {
            throw new CafeBabePyException("yielder is not found");
        }

        yielder.yield(evalValue);
        return this.runtime.None();
    }

    @SuppressWarnings("unchecked")
    private List<PyObject> evalComp(PyObject node) {
        PyObject elt = this.runtime.getattr(node, "elt");
        PyObject generators = this.runtime.getattr(node, "generators");

        List<PyObject> generatorList = (List<PyObject>) generators.toJava(List.class);
        List<PyObject> resultList = new ArrayList<>();

        this.runtime.pushNewContext();
        try {
            evalGenerators(elt, generatorList, resultList);

        } finally {
            this.runtime.popContext();
        }

        return resultList;
    }

    @SuppressWarnings("unchecked")
    private void evalGenerators(PyObject elt, List<PyObject> generators, List<PyObject> resultList) {
        PyObject generator = generators.get(0);
        PyObject target = this.runtime.getattr(generator, "target");
        PyObject iter = this.runtime.getattr(generator, "iter");
        PyObject ifs = this.runtime.getattr(generator, "ifs");
        PyObject is_async = this.runtime.getattr(generator, "is_async");

        PyObject evalIter = eval(iter);

        List<PyObject> ifList;
        if (ifs.isNone()) {
            ifList = Collections.emptyList();

        } else {
            ifList = (List<PyObject>) ifs.toJava(List.class);
        }
        this.runtime.iter(evalIter, next -> {
            assign(target, next);
            for (int i = 0; i < ifList.size(); i++) {
                PyObject result = eval(ifList.get(i));
                if (result.isFalse()) {
                    return;
                }
            }

            if (generators.size() == 1) {
                resultList.add(eval(elt));

            } else {
                List<PyObject> gs = generators.subList(1, generators.size());

                evalGenerators(elt, gs, resultList);
            }
        });
    }

    private PyObject evalExpr(PyObject node) {
        PyObject value = this.runtime.getattr(node, "value");

        return eval(value);
    }

    private PyObject evalPass(PyObject node) {
        return this.runtime.None();
    }

    private PyObject evalBreak(PyObject node) {
        throw new InterpretBreak();
    }

    private PyObject evalContinue(PyObject node) {
        throw new InterpretContinue();
    }

    private PyObject evalAssign(PyObject node) {
        PyObject targets = this.runtime.getattr(node, "targets");
        PyObject value = this.runtime.getattr(node, "value");
        PyObject evalValue = eval(value);

        this.runtime.iter(targets, target -> assign(target, evalValue));

        return this.runtime.None();
    }

    void assign(PyObject target, PyObject evalValue) {
        if (target instanceof PyNameType) {
            PyObject id = this.runtime.getattr(target, "id");
            this.runtime.setattr(this.runtime.getCurrentContext(), id.toJava(String.class), evalValue);

        } else {
            unpack(target, evalValue);
        }
    }

    @SuppressWarnings("unchecked")
    private void unpack(PyObject target, PyObject evalValue) {
        PyObject targetType = target.getType();

        PyObject targets;

        if (targetType instanceof PyNameType) {
            PyObject id = this.runtime.getattr(target, "id");
            this.runtime.setattr(this.runtime.getCurrentContext(), id.toJava(String.class), evalValue);
            return;

        } else if (targetType instanceof PyAttributeType) {
            PyObject attr = this.runtime.getattr(target, "attr");
            PyObject attributeContext = eval(target);
            this.runtime.setattr(attributeContext, attr.toJava(String.class), evalValue);
            return;

        } else if (targetType instanceof PyStarredType) {
            PyObject value = this.runtime.getattr(target, "value");
            unpack(value, evalValue);
            return;

        } else if (targetType instanceof PyListType) {
            targets = this.runtime.getattr(target, "elts");

        } else if (targetType instanceof PyTupleType) {
            targets = this.runtime.getattr(target, "elts");

        } else if (targetType instanceof PySubscriptType) {
            PyObject value = this.runtime.getattr(target, "value");
            PyObject slice = this.runtime.getattr(target, "slice");

            PyObject evalTarget = eval(value);

            Optional<PyObject> setattrOpt = this.runtime.getattrOptional(evalTarget, __setitem__);
            if (!setattrOpt.isPresent()) {
                throw this.runtime.newRaiseTypeError("'" + evalTarget.getFullName() + "' object does not support item assignment");
            }

            PyObject evalKey = eval(slice);

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
                    unpack(starredTarget, value);

                    starredTarget = null;
                    starredValueList = null;
                }
                unpack(t, evalValuePyList.get(valueIndex));
                valueIndex++;
            }
        }

        if (starredTarget != null) {
            PyObject value = this.runtime.list(starredValueList);
            unpack(starredTarget, value);
        }

        if (valueIndex < evalValuePyList.size()) {
            throw this.runtime.newRaiseException("builtins.ValueError",
                    "too many values to unpack (expected " + targetPyList.size() + ")");
        }
    }

    private PyObject evalAnnassign(PyObject node) {
        PyObject target = this.runtime.getattr(node, "target");
        PyObject value = this.runtime.getattr(node, "value");
        if (!value.isNone()) {
            PyObject id = this.runtime.getattr(target, "id");
            PyObject evalValue = eval(value);

            this.runtime.setattr(this.runtime.getCurrentContext(), id.toJava(String.class), evalValue);
        }

        return this.runtime.None();
    }

    @SuppressWarnings("unchecked")
    private PyObject evalCall(PyObject node) {
        PyObject func = this.runtime.getattr(node, "func");
        PyObject funcEval = eval(func);

        PyObject args = this.runtime.getattr(node, "args");
        PyObject keywords = this.runtime.getattr(node, "keywords");

        PyObject[] argsArray;
        LinkedHashMap<String, PyObject> keywordsMap = new LinkedHashMap<>();

        List<PyObject> argList = new ArrayList<>();
        this.runtime.iter(args, arg -> {
            if (this.runtime.isInstance(arg, "_ast.Starred")) {
                PyObject value = this.runtime.getattr(arg, "value");
                PyObject evalValue = eval(value);

                if (!this.runtime.isIterable(evalValue)) {
                    String name = this.runtime.getattr(func, "id").toJava(String.class);

                    throw this.runtime.newRaiseTypeError(name + "() argument after * must be an iterable, not " + evalValue.getFullName());
                }

                this.runtime.iter(evalValue, argList::add);

            } else {
                PyObject evalArg = eval(arg);

                argList.add(evalArg);
            }
        });

        LinkedHashMap<String, PyObject> doubleStarKeywordsMap = new LinkedHashMap<>();
        this.runtime.iter(keywords, keyword -> {
            PyObject arg = this.runtime.getattr(keyword, "arg");
            PyObject value = this.runtime.getattr(keyword, "value");
            PyObject evalValue = eval(value);

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

    private PyObject evalSubscript(PyObject node) {
        PyObject value = this.runtime.getattr(node, "value");
        PyObject slice = this.runtime.getattr(node, "slice");
        PyObject ctx = this.runtime.getattr(node, "ctx");

        if (this.runtime.isInstance(slice, "_ast.Index")) {
            PyObject evalValue = eval(value);

            Optional<PyObject> getattrOpt = this.runtime.getattrOptional(evalValue, __getitem__);
            if (!getattrOpt.isPresent()) {
                throw this.runtime.newRaiseTypeError("'" + evalValue.getFullName() + "' object is not subscriptable");
            }

            PyObject evalKey = eval(slice);
            return getattrOpt.get().call(evalKey);
        }

        // TODO あとで実装
        return this.runtime.NotImplemented();
    }

    private PyObject evalIndex(PyObject node) {
        PyObject value = this.runtime.getattr(node, "value");

        return eval(value);
    }

    private PyObject evalSlice(PyObject node) {
        PyObject lower = this.runtime.getattr(node, "lower");
        PyObject upper = this.runtime.getattr(node, "upper");
        PyObject step = this.runtime.getattr(node, "step");

        PyObject evalLower = eval(lower);
        PyObject evalUpper = eval(upper);
        PyObject evalStep = eval(step);

        return this.runtime.newPyObject("builtins.slice", evalLower, evalUpper, evalStep);
    }

    private PyObject evalReturn(PyObject node) {
        PyObject value = this.runtime.getattr(node, "value");
        PyObject evalValue = eval(value);

        throw new InterpretReturn(evalValue);
    }

    private PyObject evalCompare(PyObject node) {

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
            PyObject evalLeft = eval(comparatorList.get(i));
            PyObject evalRight = eval(comparatorList.get(i + 1));
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

    private PyObject evalBinOp(PyObject node) {
        PyObject left = this.runtime.getattr(node, "left");
        PyObject evalLeft = eval(left);

        PyObject right = this.runtime.getattr(node, "right");
        PyObject evalRight = eval(right);

        PyObject addType = this.runtime.typeOrThrow("_ast.Add");
        PyObject subType = this.runtime.typeOrThrow("_ast.Sub");
        PyObject modType = this.runtime.typeOrThrow("_ast.Mod");
        PyObject multType = this.runtime.typeOrThrow("_ast.Mult");
        PyObject floorDivType = this.runtime.typeOrThrow("_ast.FloorDiv");

        PyObject op = this.runtime.getattr(node, "op");
        if (this.runtime.isInstance(op, addType)) {
            return this.runtime.add(evalLeft, evalRight);

        } else if (this.runtime.isInstance(op, subType)) {
            return this.runtime.sub(evalLeft, evalRight);

        } else if (this.runtime.isInstance(op, modType)) {
            return this.runtime.mod(evalLeft, evalRight);

        } else if (this.runtime.isInstance(op, multType)) {
            return this.runtime.mul(evalLeft, evalRight);

        } else if (this.runtime.isInstance(op, floorDivType)) {
            return this.runtime.floorDiv(evalLeft, evalRight);
        }

        throw new CafeBabePyException("operator '" + op.getName() + "' not found");
    }

    private PyObject evalUnaryOp(PyObject node) {
        PyObject op = this.runtime.getattr(node, "op");
        PyObject operand = this.runtime.getattr(node, "operand");

        PyObject evalOperand = eval(operand);

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

    private PyObject evalLambda(PyObject node) {
        PyObject args = this.runtime.getattr(node, "args");
        PyObject body = this.runtime.getattr(node, "body");

        PyObject function = new PyInterpretFunctionObject(this.runtime, "<lambda>", args, body);
        function.initialize();
        function.getFrame().putToNotAppearLocals("_async", this.runtime.False());

        return function;
    }

    private PyObject evalName(PyObject node) {
        PyObject ctx = this.runtime.getattr(node, "ctx");

        PyObject ctxType = ctx.getType();
        if (ctxType instanceof PyLoadType) {
            PyObject id = this.runtime.getattr(node, "id");
            String javaId = id.toJava(String.class);

            Optional<PyObject> resultOpt = lookup(this.runtime.getCurrentContext(), javaId);
            if (resultOpt.isPresent()) {
                return resultOpt.get();
            }

            if ("NotImplemented".equals(javaId)) {
                return this.runtime.NotImplemented();
            }

            throw this.runtime.newRaiseException("NameError", "name '" + javaId + "' is not defined");

            /*
            return this.runtime.getattrOptional(context, name).orElseThrow(() ->
                    this.runtime.newRaiseException("builtins.NameError",
                            "name '" + name + "' is not defined")
            );
            */

        } else if (ctxType instanceof PyStoreType) {
            return this.runtime.getCurrentContext();
        }

        // TODO どうする？
        return node;
    }

    private Optional<PyObject> lookup(PyObject object, String name) {
        Optional<PyObject> attrOpt = object.getFrame().getFromGlobals(name);
        if (attrOpt.isPresent()) {
            return attrOpt;
        }

        return lookupType(object, name);
    }

    private PyObject evalNum(PyObject node) {
        return this.runtime.getattr(node, "n");
    }

    private PyObject evalStr(PyObject node) {
        return this.runtime.getattr(node, "s");
    }

    private PyObject evalBytes(PyObject node) {
        return this.runtime.getattr(node, "s");
    }

    private PyObject evalFormattedValue(PyObject node) {
        PyObject value = this.runtime.getattr(node, "value");
        PyObject conversion = this.runtime.getattr(node, "conversion");
        PyObject format_spec = this.runtime.getattr(node, "format_spec");

        PyObject evalValue = eval(value);
        // FIXME conversion

        return this.runtime.getattr(evalValue, __format__).call(format_spec);
    }

    private PyObject evalJoinedStr(PyObject node) {
        PyObject values = this.runtime.getattr(node, "values");

        StringBuilder builder = new StringBuilder();
        this.runtime.iter(values, value -> builder.append(eval(value).toJava(String.class)));

        return this.runtime.str(builder.toString());
    }

    private PyObject evalAttribute(PyObject node) {
        PyObject value = this.runtime.getattr(node, "value");
        PyObject attr = this.runtime.getattr(node, "attr");
        PyObject ctx = this.runtime.getattr(node, "ctx");

        PyObject evalValue = eval(value);

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
    private PyObject evalDict(PyObject node) {
        PyObject keys = this.runtime.getattr(node, "keys");
        PyObject values = this.runtime.getattr(node, "values");

        List<PyObject> keyList = new ArrayList<>();
        List<PyObject> valueList = new ArrayList<>();

        this.runtime.iter(keys, keyList::add);
        this.runtime.iter(values, valueList::add);

        LinkedHashMap<PyObject, PyObject> map = new LinkedHashMap<>();

        for (int i = 0; i < keyList.size(); i++) {
            PyObject key = eval(keyList.get(i));
            PyObject value = eval(valueList.get(i));

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

    private PyObject evalTuple(PyObject node) {
        PyObject elts = this.runtime.getattr(node, "elts");

        List<PyObject> elements = new ArrayList<>();
        this.runtime.iter(elts, elt -> {
            PyObject type = elt.getType();
            if (type instanceof PyStarredType) {
                PyObject value = this.runtime.getattr(elt, "value");
                PyObject evalValue = eval(value);

                this.runtime.iter(evalValue, elements::add);

            } else {
                PyObject evalValue = eval(elt);

                elements.add(evalValue);
            }
        });

        return this.runtime.tuple(elements);
    }

    class PrepareClassReturn {
        PyObject meta;
        PyObject ns;
        LinkedHashMap<String, PyObject> kwds;
    }
}