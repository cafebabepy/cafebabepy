package org.cafebabepy.evaluter.Interpret;

import org.cafebabepy.runtime.*;
import org.cafebabepy.runtime.module._ast.*;
import org.cafebabepy.runtime.object.proxy.PyLexicalScopeProxyObject;
import org.cafebabepy.util.StringUtils;

import java.util.*;

import static org.cafebabepy.util.ProtocolNames.*;

/**
 * Created by yotchang4s on 2017/06/09.
 */
public class InterpretEvaluator {
    final Map<PyObject, Yielder<PyObject>> yielderMap = Collections.synchronizedMap(new HashMap<>());
    private Python runtime;
    private ImportManager importManager;

    private ThreadLocal<List<PyObject>> contexts = ThreadLocal.withInitial(LinkedList::new);

    private ThreadLocal<Boolean> attribute = ThreadLocal.withInitial(() -> false);

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

            case "AsyncFunctionDef":
                return evalAsyncFunctionDef(context, node);

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

            case "While":
                return evalWhile(context, node);

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

            case "Yield":
                return evalYield(context, node);

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

            case "Bytes":
                return evalBytes(context, node);

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

    public PyObject createPyFrame(Frame frame) {
        PyObject pyFrame = this.runtime.newPyObject("builtins.frame", false);

        PyObject f_back = frame.getBack().map(this::createPyFrame).orElse(this.runtime.None());
        PyObject f_locals = this.runtime.dict(frame.getLocalsPyObjectMap());
        PyObject f_globals = this.runtime.dict(frame.getGlobalsPyObjectMap());

        pyFrame.getFrame().getLocals().put("f_back", f_back);
        pyFrame.getFrame().getLocals().put("f_locals", f_locals);
        pyFrame.getFrame().getLocals().put("f_globals", f_globals);

        return pyFrame;
    }

    public Frame getFrame() {
        List<PyObject> contexts = this.contexts.get();
        return contexts.get(contexts.size() - 1).getFrame();
    }

    public List<PyObject> getContexts() {
        return this.contexts.get();
    }

    public boolean isNowAttributeAccess() {
        return this.attribute.get();
    }

    public PyObject loadModule(String moduleName) {
        return this.importManager.loadModule(moduleName);
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

        if (this.runtime.isIterable(body)) {
            PyObject[] result = new PyObject[1];
            result[0] = this.runtime.None();

            this.runtime.iter(body, b -> {
                result[0] = eval(context, b);
            });

            return result[0];

        } else {
            PyObject result = eval(context, body);

            return result;
        }
    }

    private PyObject evalImport(PyObject context, PyObject node) {
        PyObject names = this.runtime.getattr(node, "names");

        this.runtime.iter(names, n -> {
            PyObject name = this.runtime.getattr(n, "name");
            PyObject asname = this.runtime.getattr(n, "asname");

            PyObject mangleName = this.runtime.str(toMangleName(name.toJava(String.class)));
            PyObject mangleAsName = this.runtime.None();
            if (!asname.isNone()) {
                mangleAsName = this.runtime.str(toMangleName(asname.toJava(String.class)));
            }

            this.importManager.importAsName(context, mangleName, mangleAsName);
        });

        return this.runtime.None();
    }

    private String toMangleName(String name) {
        if (name.startsWith("__") && !name.endsWith("__")) {
            List<PyObject> contexts = this.contexts.get();
            for (int i = contexts.size() - 1; i >= 0; i--) {
                PyObject c = contexts.get(i);
                if (c.isType() || c.isFromClass()) {
                    String className = c.getName();
                    int index;
                    if ((index = className.lastIndexOf('.')) != -1) {
                        // Un mangling
                        className = className.substring(index + 1);
                        if (className.startsWith("__")) {
                            className = className.substring(2);
                        }
                    }

                    return "_" + className + name;
                }
            }
        }

        return name;
    }

    private PyObject evalImportFrom(PyObject context, PyObject node) {
        PyObject module = this.runtime.getattr(node, "module");
        PyObject names = this.runtime.getattr(node, "names");
        PyObject level = this.runtime.getattr(node, "level");

        String javaModule = module.toJava(String.class);
        String javaMangleModule = toMangleName(javaModule);
        PyObject mangleModule = this.runtime.str(javaMangleModule);

        Map<PyObject, PyObject> namesMap = new LinkedHashMap<>();

        this.runtime.iter(names, n -> {
            PyObject name = this.runtime.getattr(n, "name");
            PyObject asName = this.runtime.getattr(n, "asname");

            PyObject mangleName = this.runtime.str(toMangleName(name.toJava(String.class)));
            PyObject mangleAsName;
            if (!asName.isNone()) {
                mangleAsName = this.runtime.str(toMangleName(asName.toJava(String.class)));

            } else {
                mangleAsName = this.runtime.None();
            }

            namesMap.put(mangleName, mangleAsName);
        });

        this.importManager.importFrom(context, mangleModule, namesMap, level);

        return this.runtime.None();
    }

    private PyObject evalAsyncFunctionDef(PyObject context, PyObject node) {
        return evalFunctionDefImpl(context, node, true);
    }

    private PyObject evalFunctionDef(PyObject context, PyObject node) {
        return evalFunctionDefImpl(context, node, false);
    }

    private PyObject evalFunctionDefImpl(PyObject context, PyObject node, boolean async) {
        PyObject name = this.runtime.getattr(node, "name");
        PyObject args = this.runtime.getattr(node, "args");
        PyObject body = this.runtime.getattr(node, "body");
        PyObject decorator_list = this.runtime.getattr(node, "decorator_list");
        PyObject returns = this.runtime.getattr(node, "returns"); // FIXME ???

        PyObject function = new PyInterpretFunctionObject(this.runtime, context, name.toJava(String.class), args, body);
        function.initialize();
        function.getFrame().getNotAppearLocals().put("_async", this.runtime.bool(async));

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

        this.runtime.setattr(context, toMangleName(name.toJava(String.class)), decoratorEvalValue);

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

            PyObject evalBase = eval(context, base);
            baseList.add(evalBase);
        });

        LinkedHashMap<String, PyObject> keywordsMap = new LinkedHashMap<>();
        this.runtime.iter(keywords, keyword -> {
            PyObject arg = this.runtime.getattr(keyword, "arg");
            PyObject value = this.runtime.getattr(keyword, "value");
            PyObject evalValue = eval(context, value);

            keywordsMap.put(arg.toJava(String.class), evalValue);
        });

        String javaName = name.toJava(String.class);

        List<PyObject> contexts = this.contexts.get();
        for (int i = contexts.size() - 1; i >= 0; i--) {
            PyObject c = contexts.get(i);
            if (c.isType()) {
                javaName = c.getName() + '.' + javaName;
                break;
            }
        }

        String[] splitDotStrs = StringUtils.splitDot(javaName);
        String mangleJavaName = toMangleName(splitDotStrs[splitDotStrs.length - 1]);

        PyObject clazz = newClass(javaName, baseList, keywordsMap);
        contexts.add(clazz);
        try {
            // FIXME special
            if (clazz instanceof PyInterpretClassObject) {
                ((PyInterpretClassObject) clazz).setContext(context);
            }

            this.runtime.setattr(context, mangleJavaName, clazz);
            eval(clazz, body);

            return this.runtime.None();

        } finally {
            contexts.remove(contexts.size() - 1);
        }
    }

    private PyObject newClass(String name, List<PyObject> bases, LinkedHashMap<String, PyObject> kwds) {
        PrepareClassReturn result = prepareClass(name, bases, kwds);

        PyObject[] args = new PyObject[3];
        args[0] = this.runtime.str(name);
        args[1] = this.runtime.tuple(bases);
        args[2] = result.ns;

        List<PyObject> contexts = this.contexts.get();
        try {
            contexts.add(result.meta);

            return result.meta.call(args, result.kwds);

        } finally {
            contexts.remove(result.meta);
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
                    String javaName = name.toJava(String.class);

                    PyObject exceptBody = this.runtime.getattr(handler, "body");
                    try {
                        if (!name.isNone()) {
                            this.runtime.setattr(context, javaName, exception);
                        }
                        return eval(context, exceptBody);

                    } finally {
                        if (!name.isNone()) {
                            this.runtime.delattr(context, javaName);
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
        List<PyObject> contexts = this.contexts.get();
        contexts.add(lexicalContext);
        try {
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
        } finally {
            contexts.remove(contexts.size() - 1);
        }
    }

    private PyObject evalWhile(PyObject context, PyObject node) {
        PyObject test = this.runtime.getattr(node, "test");
        PyObject body = this.runtime.getattr(node, "body");
        PyObject orelse = this.runtime.getattr(node, "orelse");

        try {
            while (true) {
                PyObject evalTest = eval(context, test);
                if (evalTest.isFalse()) {
                    break;
                }
                try {
                    eval(context, body);

                } catch (InterpretContinue ignore) {
                }
            }

            eval(context, orelse);

        } catch (InterpretBreak ignore) {
        }

        return this.runtime.None();
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

    private PyObject evalYield(PyObject context, PyObject node) {
        PyObject value = this.runtime.getattr(node, "value");
        PyObject evalValue = eval(context, value);

        Yielder<PyObject> yielder = this.yielderMap.get(node);
        if (yielder == null) {
            throw new CafeBabePyException("yielder is not found");
        }

        yielder.yield(evalValue);
        return this.runtime.None();
    }

    @SuppressWarnings("unchecked")
    private List<PyObject> evalComp(PyObject context, PyObject node) {
        PyObject elt = this.runtime.getattr(node, "elt");
        PyObject generators = this.runtime.getattr(node, "generators");

        List<PyObject> generatorList = (List<PyObject>) generators.toJava(List.class);
        List<PyObject> resultList = new ArrayList<>();

        PyLexicalScopeProxyObject lexicalContext = new PyLexicalScopeProxyObject(context);
        List<PyObject> contexts = this.contexts.get();
        contexts.add(lexicalContext);
        try {
            evalGenerators(lexicalContext, elt, generatorList, resultList);

        } finally {
            contexts.remove(contexts.size() - 1);
        }

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

    private void assign(PyObject context, PyObject target, PyObject evalValue) {
        if (target instanceof PyNameType) {
            PyObject id = this.runtime.getattr(target, "id");
            String javaId = id.toJava(String.class);
            String javaMangleId = toMangleName(javaId);

            this.runtime.setattr(context, javaMangleId, evalValue);

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
            this.runtime.setattr(context, toMangleName(id.toJava(String.class)), evalValue);
            return;

        } else if (targetType instanceof PyAttributeType) {
            PyObject attr = this.runtime.getattr(target, "attr");
            PyObject attributeContext = eval(context, target);
            this.runtime.setattr(attributeContext, toMangleName(attr.toJava(String.class)), evalValue);
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

            this.runtime.setattr(context, toMangleName(id.toJava(String.class)), evalValue);
        }

        return this.runtime.None();
    }

    @SuppressWarnings("unchecked")
    private PyObject evalCall(PyObject context, PyObject node) {
        PyObject func = this.runtime.getattr(node, "func");
        PyObject funcEval;
        List<PyObject> attributes;
        if (func.getType().equals(this.runtime.typeOrThrow("_ast.Attribute"))) {
            attributes = new ArrayList<>();
            evalAttribute(attributes, context, func);
            funcEval = attributes.get(attributes.size() - 1);

        } else {
            attributes = Collections.emptyList();
            funcEval = eval(context, func);
        }

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

        if (argList.isEmpty() && keywordsMap.isEmpty()
                && funcEval.equals(this.runtime.typeOrThrow("builtins.super"))) {
            List<PyObject> contexts = this.contexts.get();

            PyObject type = null;
            PyFunctionObject function = null;
            PyObject self = null;

            for (int i = contexts.size() - 1; i >= 0; i--) {
                PyObject c = contexts.get(i);
                PyObject t = c.getType();
                if (t.equals(this.runtime.typeOrThrow("builtins.function", false))
                        || t.equals(this.runtime.typeOrThrow("builtins.wrapper_descriptor", false))) {
                    if (function != null) {
                        continue;
                    }

                    function = (PyFunctionObject) c;

                } else if (function != null && c.isFromClass()) {
                    self = c;
                    break;
                }
            }

            if (self == null) {
                throw this.runtime.newRaiseException("builtins.RuntimeError", "super(): no arguments");

            } else {
                List<String> arguments = function.getArguments();
                Map<String, PyObject> frameMap = getFrame().getLocals();
                if (arguments.isEmpty() || frameMap.isEmpty()) {
                    throw this.runtime.newRaiseException("builtins.RuntimeError", "super(): no arguments");
                }
                argList.add(self.getType());
                argList.add(self);
            }
        }

        argsArray = new PyObject[argList.size()];
        argList.toArray(argsArray);


        List<PyObject> contexts = this.contexts.get();
        contexts.addAll(attributes);

        try {
            return funcEval.call(argsArray, keywordsMap);

        } finally {
            contexts.remove(funcEval);
            contexts.removeAll(attributes);
        }
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

        PyObject function = new PyInterpretFunctionObject(this.runtime, context, "<lambda>", args, body);
        function.initialize();
        function.getFrame().getNotAppearLocals().put("_async", this.runtime.False());

        return function;
    }

    private PyObject evalName(PyObject context, PyObject node) {
        PyObject ctx = this.runtime.getattr(node, "ctx");

        PyObject ctxType = ctx.getType();
        if (ctxType instanceof PyLoadType) {
            PyObject id = this.runtime.getattr(node, "id");
            String javaId = toMangleName(id.toJava(String.class));

            PyObject result = lookup(context, javaId);
            if (result != null) {
                return result;
            }

            if ("NotImplemented".equals(javaId)) {
                return this.runtime.NotImplemented();
            }

            PyObject builtins = context.getFrame().getGlobals().get(__builtins__);
            if (builtins != null) {
                PyObject object = builtins.getFrame().getLocals().get(javaId);
                if (object != null) {
                    return object;
                }
            }

            throw this.runtime.newRaiseException("NameError", "name '" + javaId + "' is not defined");

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

    private PyObject lookup(PyObject object, String name) {
        PyObject attr = object.getFrame().lookup(name);
        if (attr != null) {
            return attr;
        }

        return lookupType(object, name);
    }

    private PyObject lookupType(PyObject object, String name) {
        for (PyObject type : object.getTypes()) {
            PyObject typeObject = type.getFrame().lookup(name);
            if (typeObject != null) {
                return typeObject;
            }
        }

        return null;
    }

    private PyObject evalNum(PyObject context, PyObject node) {
        return this.runtime.getattr(node, "n");
    }

    private PyObject evalStr(PyObject context, PyObject node) {
        return this.runtime.getattr(node, "s");
    }

    private PyObject evalBytes(PyObject context, PyObject node) {
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
        this.attribute.set(true);
        try {
            PyObject value = this.runtime.getattr(node, "value");
            PyObject attr = this.runtime.getattr(node, "attr");
            PyObject ctx = this.runtime.getattr(node, "ctx");

            PyObject evalValue = eval(context, value);

            PyObject ctxType = ctx.getType();
            if (ctxType instanceof PyLoadType) {

                return this.runtime.getattr(evalValue, toMangleName(attr.toJava(String.class)));

            } else if (ctxType instanceof PyStoreType) {
                return evalValue;
            }

            //　TODO どうする？
            return node;

        } finally {
            this.attribute.set(false);
        }
    }

    private void evalAttribute(List<PyObject> list, PyObject context, PyObject node) {
        PyObject value = this.runtime.getattr(node, "value");
        PyObject attr = this.runtime.getattr(node, "attr");
        PyObject ctx = this.runtime.getattr(node, "ctx");

        PyObject evalValue = eval(context, value);
        list.add(evalValue);

        PyObject ctxType = ctx.getType();
        if (ctxType instanceof PyLoadType) {
            list.add(this.runtime.getattr(evalValue, toMangleName(attr.toJava(String.class))));

        } else if (ctxType instanceof PyStoreType) {
            list.add(evalValue);
        }
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

    class PrepareClassReturn {
        PyObject meta;
        PyObject ns;
        LinkedHashMap<String, PyObject> kwds;
    }
}