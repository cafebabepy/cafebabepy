package org.cafebabepy.evaluter.Interpret;

import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module._ast.*;
import org.cafebabepy.runtime.object.PyLexicalScopeProxyObject;

import java.util.*;

import static org.cafebabepy.util.ProtocolNames.*;

/**
 * Created by yotchang4s on 2017/06/09.
 */
public class InterpretEvaluator {

    private Python runtime;

    public InterpretEvaluator(Python runtime) {
        this.runtime = runtime;
    }

    public PyObject eval(PyObject node) {
        PyObject context = this.runtime.moduleOrThrow("__main__");
        return eval(context, node);

        /*
        System.out.println(node.asJavaString());
        PyObject superType = typeOrThrow("builtins.super");
        PyObject superObject = superType.call(self);

        PyObject getattribute = superType.getObjectOrThrow(__getattribute__);
        PyObject visit = getattribute.call(superObject, this.runtime.str("visit"));

        visit.call(self, node);
        */
    }

    public PyObject eval(PyObject context, PyObject node) {
        if (node.isNone()) {
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

            case "FunctionDef":
                return evalFunctionDef(context, node);

            case "ClassDef":
                return evalClassDef(context, node);

            case "If":
                return evalIfAndIfExp(context, node);

            case "For":
                return evalFor(context, node);

            case "Expr":
                return evalExpr(context, node);

            case "Pass":
                return evalPass(context, node);

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

            case "Starred":
                return evalStarred(context, node);

            case "BinOp":
                return evalBinOp(context, node);

            case "UnaryOp":
                return evalUnaryOp(context, node);

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

            case "Attribute":
                return evalAttribute(context, node);
        }

        throw new CafeBabePyException("Unknown AST '" + node.getName() + "'");
    }

    private PyObject evalModule(PyObject context, PyObject node) {
        PyObject body = node.getScope().getOrThrow("body");
        return eval(context, body);
    }

    private PyObject evalInteractive(PyObject context, PyObject node) {
        PyObject body = node.getScope().getOrThrow("body");
        return eval(context, body);
    }

    private PyObject evalSuite(PyObject context, PyObject node) {
        PyObject body = node.getScope().getOrThrow("body");

        PyObject[] result = new PyObject[1];
        result[0] = this.runtime.None();
        this.runtime.iter(body, b -> {
            result[0] = eval(body);
        });

        return result[0];
    }

    private PyObject evalFunctionDef(PyObject context, PyObject node) {
        PyObject name = node.getScope().getOrThrow("name");
        PyObject args = node.getScope().getOrThrow("args");
        PyObject body = node.getScope().getOrThrow("body");
        PyObject decorator_list = node.getScope().getOrThrow("decorator_list");
        PyObject returns = node.getScope().getOrThrow("returns");

        PyObject function = new PyInterpretFunctionObject(
                this.runtime, this, context, args, body);

        context.getScope().put(name.asJavaString(), function);

        return this.runtime.None();
    }

    private PyObject evalClassDef(PyObject context, PyObject node) {
        PyObject name = node.getScope().getOrThrow("name");
        PyObject bases = node.getScope().getOrThrow("bases");
        PyObject keywords = node.getScope().getOrThrow("keywords");
        PyObject body = node.getScope().getOrThrow("body");
        PyObject decorator_list = node.getScope().getOrThrow("decorator_list");

        List<PyObject> baseList = new ArrayList<>();
        Set<String> duplicateCheckSet = new LinkedHashSet<>();
        this.runtime.iter(bases, base -> {
            boolean exists = !duplicateCheckSet.add(base.asJavaString());
            if (exists) {
                throw this.runtime.newRaiseTypeError("duplicate base class " + base.getName());
            }
            baseList.add(base);
        });

        PyObject clazz = new PyInterpretClassObject(
                this.runtime, context, name.asJavaString(), baseList);

        context.getScope().put(name.asJavaString(), clazz);

        eval(clazz, body);

        return this.runtime.None();
    }

    private PyObject evalIfAndIfExp(PyObject context, PyObject node) {
        PyObject test = node.getScope().getOrThrow("test");
        PyObject body = node.getScope().getOrThrow("body");
        PyObject orElse = node.getScope().getOrThrow("orelse");

        PyObject evalTest = eval(context, test);
        if (evalTest.isTrue()) {
            return eval(context, body);

        } else {
            return eval(context, orElse);
        }
    }

    private PyObject evalFor(PyObject context, PyObject node) {
        PyObject target = node.getScope().getOrThrow("target");
        PyObject iter = node.getScope().getOrThrow("iter");
        PyObject body = node.getScope().getOrThrow("body");
        PyObject orelse = node.getScope().getOrThrow("orelse");

        PyObject evalIter = eval(context, iter);
        this.runtime.iter(evalIter, next -> {
            assign(context, target, next);
            eval(context, body);
        });

        eval(context, orelse);

        return this.runtime.None();
    }

    private PyObject evalList(PyObject context, PyObject node) {
        PyObject elts = node.getScope().getOrThrow("elts");

        List<PyObject> elements = new ArrayList<>();
        this.runtime.iter(elts, elt -> {
            PyObject evalElt = eval(context, elt);

            PyObject type = elt.getType();
            if (type instanceof PyStarredType) {
                this.runtime.iter(evalElt, extendEvalElt -> {
                    elements.add(extendEvalElt);
                });

            } else {
                elements.add(evalElt);
            }
        });

        return this.runtime.list(elements);
    }

    private PyObject evalListComp(PyObject context, PyObject node) {
        PyObject elt = node.getScope().getOrThrow("elt");
        PyObject generators = node.getScope().getOrThrow("generators");

        List<PyObject> generatorList = this.runtime.toList(generators);
        List<PyObject> resultList = new ArrayList<>();

        PyLexicalScopeProxyObject lexicalContext = new PyLexicalScopeProxyObject(context);
        evalGenerators(lexicalContext, elt, generatorList, resultList);

        return this.runtime.list(resultList);
    }

    private void evalGenerators(PyObject context, PyObject elt, List<PyObject> generators, List<PyObject> resultList) {
        PyObject generator = generators.get(0);
        PyObject target = generator.getScope().getOrThrow("target");
        PyObject iter = generator.getScope().getOrThrow("iter");
        PyObject ifs = generator.getScope().getOrThrow("ifs");
        PyObject is_async = generator.getScope().getOrThrow("is_async");

        PyObject evalIter = eval(context, iter);

        List<PyObject> ifList;
        if (ifs.isNone()) {
            ifList = Collections.emptyList();

        } else {
            ifList = this.runtime.toList(ifs);
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

    private PyObject evalStarred(PyObject context, PyObject node) {
        PyObject value = node.getScope().getOrThrow("value");

        return eval(context, value);
    }

    private PyObject evalExpr(PyObject context, PyObject node) {
        PyObject value = node.getScope().getOrThrow("value");

        return eval(context, value);
    }

    private PyObject evalPass(PyObject context, PyObject node) {
        return this.runtime.None();
    }

    private PyObject evalAssign(PyObject context, PyObject node) {
        PyObject targets = node.getScope().getOrThrow("targets");
        PyObject value = node.getScope().getOrThrow("value");
        PyObject evalValue = eval(context, value);

        this.runtime.iter(targets, target -> assign(context, target, evalValue));

        return this.runtime.None();
    }

    private void assign(PyObject context, PyObject target, PyObject evalValue) {
        if (target instanceof PyNameType) {
            PyObject id = target.getScope().getOrThrow("id");
            context.getScope().put(id.asJavaString(), evalValue);

        } else {
            LinkedHashMap<String, PyObject> assignMap = new LinkedHashMap<>();

            unpack(context, target, evalValue, assignMap);

            for (Map.Entry<String, PyObject> e : assignMap.entrySet()) {
                context.getScope().put(e.getKey(), e.getValue());
            }
        }
    }

    private void unpack(PyObject context, PyObject target, PyObject evalValue, Map<String, PyObject> assignMap) {
        PyObject targetType = target.getType();

        PyObject targets;

        if (targetType instanceof PyNameType) {
            PyObject id = target.getScope().getOrThrow("id");
            assignMap.put(id.asJavaString(), evalValue);
            return;

        } else if (targetType instanceof PyAttributeType) {
            PyObject attr = target.getScope().getOrThrow("attr");
            PyObject attributeContext = eval(context, target);
            attributeContext.getScope().put(attr.asJavaString(), evalValue);
            return;

        } else if (targetType instanceof PyStarredType) {
            PyObject value = target.getScope().getOrThrow("value");
            unpack(context, value, evalValue, assignMap);
            return;

        } else if (targetType instanceof PyListType) {
            targets = target.getScope().getOrThrow("elts");

        } else {
            throw this.runtime.newRaiseTypeError("Invalid '" + targetType.getFullName() + "' type");
        }

        List<PyObject> targetPyList = this.runtime.toList(targets);
        List<PyObject> evalValuePyList = this.runtime.toList(evalValue);

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
                    unpack(context, starredTarget, value, assignMap);

                    starredTarget = null;
                    starredValueList = null;
                }
                unpack(context, t, evalValuePyList.get(valueIndex), assignMap);
                valueIndex++;
            }
        }

        if (starredTarget != null) {
            PyObject value = this.runtime.list(starredValueList);
            unpack(context, starredTarget, value, assignMap);
        }

        if (valueIndex < evalValuePyList.size()) {
            throw this.runtime.newRaiseException("builtins.ValueError",
                    "too many values to unpack (expected " + targetPyList.size() + ")");
        }
    }

    private PyObject evalAnnassign(PyObject context, PyObject node) {
        PyObject target = node.getScope().getOrThrow("target");
        PyObject value = node.getScope().getOrThrow("value");
        if (!value.isNone()) {
            PyObject id = target.getScope().getOrThrow("id");
            PyObject evalValue = eval(context, value);

            context.getScope().put(id.asJavaString(), evalValue);
        }

        return this.runtime.None();
    }

    private PyObject evalCall(PyObject context, PyObject node) {
        PyObject func = node.getScope().getOrThrow("func");
        PyObject funcEval = eval(context, func);

        PyObject args = node.getScope().getOrThrow("args");

        PyObject[] argArray;
        if (args.isNone()) {
            argArray = new PyObject[0];

        } else {
            // FIXME array direct
            List<PyObject> argList = new ArrayList<>();
            this.runtime.iter(args, arg -> {
                PyObject evalArg = eval(context, arg);

                argList.add(evalArg);
            });

            argArray = new PyObject[argList.size()];
            argList.toArray(argArray);
        }

        PyObject result;
        try {
            result = funcEval.call(argArray);

        } catch (InterpretReturn re) {
            result = re.getValue();
        }

        return result;
    }

    private PyObject evalReturn(PyObject context, PyObject node) {
        PyObject value = node.getScope().getOrThrow("value");
        PyObject evalValue = eval(context, value);

        throw new InterpretReturn(evalValue);
    }

    private PyObject evalCompare(PyObject context, PyObject node) {

        // 1 < 2 < 3 < 4 => 1 < 2 && 2 < 3 && 3 < 4
        // 1 < 2 < 3 => 1 < 2 && 2 < 3
        // 1 < 2 => 1 < 2
        PyObject comparators = node.getScope().getOrThrow("comparators");

        List<PyObject> comparatorList = getLinkedList(comparators);
        PyObject left = node.getScope().getOrThrow("left");
        comparatorList.add(0, left);

        PyObject ops = node.getScope().getOrThrow("ops");
        List<PyObject> opList = getLinkedList(ops);

        PyObject eqType = this.runtime.typeOrThrow("_ast.Eq");
        PyObject notEqType = this.runtime.typeOrThrow("_ast.NotEq");
        PyObject ltType = this.runtime.typeOrThrow("_ast.Lt");
        PyObject lteType = this.runtime.typeOrThrow("_ast.LtE");
        PyObject gtType = this.runtime.typeOrThrow("_ast.Gt");
        PyObject gteType = this.runtime.typeOrThrow("_ast.GtE");

        boolean evalResult = true;
        for (int i = 0; i < comparatorList.size() - 1; i++) {
            PyObject evalLeft = eval(context, comparatorList.get(i));
            PyObject evalRight = eval(context, comparatorList.get(i + 1));
            PyObject op = opList.get(i);

            if (this.runtime.isInstance(op, eqType)) {
                evalResult &= this.runtime.eq(evalLeft, evalRight).isTrue();

            } else if (this.runtime.isInstance(op, notEqType)) {
                evalResult &= this.runtime.ne(evalLeft, evalRight).isTrue();

            } else if (this.runtime.isInstance(op, ltType)) {
                evalResult &= this.runtime.lt(evalLeft, evalRight).isTrue();

            } else if (this.runtime.isInstance(op, lteType)) {
                evalResult &= this.runtime.le(evalLeft, evalRight).isTrue();

            } else if (this.runtime.isInstance(op, gtType)) {
                evalResult &= this.runtime.gt(evalLeft, evalRight).isTrue();

            } else if (this.runtime.isInstance(op, gteType)) {
                evalResult &= this.runtime.ge(evalLeft, evalRight).isTrue();

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
        PyObject left = node.getScope().getOrThrow("left");
        PyObject evalLeft = eval(context, left);

        PyObject right = node.getScope().getOrThrow("right");
        PyObject evalRight = eval(context, right);

        PyObject addType = this.runtime.typeOrThrow("_ast.Add");
        PyObject subType = this.runtime.typeOrThrow("_ast.Sub");
        PyObject modType = this.runtime.typeOrThrow("_ast.Mod");
        PyObject multType = this.runtime.typeOrThrow("_ast.Mult");

        PyObject op = node.getScope().getOrThrow("op");
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
        PyObject op = node.getScope().getOrThrow("op");
        PyObject operand = node.getScope().getOrThrow("operand");

        PyObject evalOperand = eval(context, operand);

        PyObject uAddType = this.runtime.typeOrThrow("_ast.UAdd");
        PyObject uSubType = this.runtime.typeOrThrow("_ast.USub");
        PyObject invertType = this.runtime.typeOrThrow("_ast.Invert");
        PyObject notType = this.runtime.typeOrThrow("_ast.Not");

        if (this.runtime.isInstance(op, uAddType)) {
            PyObject pos = evalOperand.getScope().getOrThrow(__pos__);
            return pos.call(evalOperand);

        } else if (this.runtime.isInstance(op, uSubType)) {
            PyObject pos = evalOperand.getScope().getOrThrow(__neg__);
            return pos.call(evalOperand);

        } else if (this.runtime.isInstance(op, invertType)) {
            PyObject pos = evalOperand.getScope().getOrThrow(__invert__);
            return pos.call(evalOperand);

        } else if (this.runtime.isInstance(op, notType)) {
            PyObject bool = evalOperand.getScope().getOrThrow(__bool__);
            PyObject result = bool.call(evalOperand);
            if (result.isTrue()) {
                return this.runtime.False();

            } else {
                return this.runtime.True();
            }

        } else {
            throw this.runtime.newRaiseTypeError("Unknown op");
        }
    }

    private PyObject evalName(PyObject context, PyObject node) {
        PyObject ctx = node.getScope().getOrThrow("ctx");

        PyObject ctxType = ctx.getType();
        if (ctxType instanceof PyLoadType) {
            PyObject id = node.getScope().getOrThrow("id");
            return context.getScope().getOrThrow(id.asJavaString());

        } else if (ctxType instanceof PyStoreType) {
            return context;
        }

        // TODO どうする？
        return node;
    }

    private PyObject evalNum(PyObject context, PyObject node) {
        return node.getScope().getOrThrow("n");
    }

    private PyObject evalStr(PyObject context, PyObject node) {
        return node.getScope().getOrThrow("s");
    }

    private PyObject evalAttribute(PyObject context, PyObject node) {
        PyObject value = node.getScope().getOrThrow("value");
        PyObject attr = node.getScope().getOrThrow("attr");
        PyObject ctx = node.getScope().getOrThrow("ctx");

        PyObject evalValue = eval(context, value);

        PyObject ctxType = ctx.getType();
        if (ctxType instanceof PyLoadType) {
            return evalValue.getScope().getOrThrow(attr.asJavaString());

        } else if (ctxType instanceof PyStoreType) {
            return evalValue;
        }

        //　TODO どうする？
        return node;
    }
}