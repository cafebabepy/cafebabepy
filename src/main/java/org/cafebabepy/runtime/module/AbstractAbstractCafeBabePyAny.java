package org.cafebabepy.runtime.module;

import org.cafebabepy.runtime.AbstractPyObject;
import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.java.PyJavaFunctionObject;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by yotchang4s on 2017/05/30.
 */
abstract class AbstractAbstractCafeBabePyAny extends AbstractPyObject {

    private volatile List<PyObject> bases;

    AbstractAbstractCafeBabePyAny(Python runtime) {
        super(runtime);
    }

    AbstractAbstractCafeBabePyAny(Python runtime, boolean dict) {
        super(runtime, dict);
    }

    @Override
    public void initialize() {
        Class<?> clazz = getClass();

        Map<String, Method> functionMap = new HashMap<>();
        Map<String, Map<String, Method>> defaultArgumentMap = new HashMap<>();

        for (Method method : clazz.getMethods()) {
            // Same class method only
            if (clazz != method.getDeclaringClass()) {
                continue;
            }

            DefinePyFunction definePyFunction = method.getAnnotation(DefinePyFunction.class);
            if (definePyFunction != null) {
                String functionName = definePyFunction.name();
                if (functionMap.containsKey(functionName)) {
                    throw new CafeBabePyException(
                            "Duplicate function '" + functionName + "' method");
                }

                functionMap.put(definePyFunction.name(), method);
            }

            DefinePyFunctionDefaultValue definePyFunctionDefaultValue = method.getAnnotation(DefinePyFunctionDefaultValue.class);
            if (definePyFunctionDefaultValue != null) {
                String methodName = definePyFunctionDefaultValue.methodName();
                String argumentName = definePyFunctionDefaultValue.parameterName();

                Map<String, Method> defineArgumentMethodMap = defaultArgumentMap.get(methodName);
                if (defineArgumentMethodMap == null) {
                    defineArgumentMethodMap = new HashMap<>();
                    defaultArgumentMap.put(methodName, defineArgumentMethodMap);

                } else if (defineArgumentMethodMap.containsKey(argumentName)) {
                    throw new CafeBabePyException(
                            "Duplicate default value '" + methodName + "." + argumentName + "' method");
                }

                defineArgumentMethodMap.put(argumentName, method);
            }
        }

        this.runtime.pushNewContext(this);
        try {
            for (Map.Entry<String, Method> entry : functionMap.entrySet()) {
                String functionName = entry.getKey();
                Method functionMethod = entry.getValue();

                Map<String, Method> defaultValueMethodMap = defaultArgumentMap.get(functionName);
                if (defaultValueMethodMap == null) {
                    defaultValueMethodMap = new HashMap<>();
                }

                PyJavaFunctionObject f = new PyJavaFunctionObject(
                        getRuntime(),
                        functionName,
                        this,
                        functionMethod,
                        defaultValueMethodMap);

                f.initialize();

                getScope().put(this.runtime.str(f.getName()), f);
            }

        } finally {
            this.runtime.popContext();
        }
    }

    @Override
    public final boolean isNone() {
        return false;
    }

    @Override
    public final boolean isNotImplemented() {
        return false;
    }

    @Override
    public final boolean isEllipsis() {
        return false;
    }

    @Override
    public List<PyObject> getBases() {
        if (this.bases == null) {
            synchronized (this) {
                if (this.bases == null) {
                    String[] baseNames = getBaseNames();
                    List<PyObject> bases = new ArrayList<>(baseNames.length);

                    for (String baseName : getBaseNames()) {
                        PyObject base;

                        Optional<PyObject> typeOpt;
                        typeOpt = this.runtime.type(baseName, true);
                        if (typeOpt.isPresent()) {
                            base = typeOpt.get();

                        } else {
                            typeOpt = this.runtime.type(baseName, false);
                            if (typeOpt.isPresent()) {
                                base = typeOpt.get();

                            } else {
                                throw new CafeBabePyException(
                                        "type '" + getName() + "' parent '" + baseName + "' is not found");
                            }
                        }

                        bases.add(base);
                    }

                    this.bases = Collections.unmodifiableList(Collections.synchronizedList(bases));
                }
            }
        }

        return this.bases;
    }

    abstract String[] getBaseNames();
}
