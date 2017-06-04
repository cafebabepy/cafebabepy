package org.cafebabepy.runtime;

import java.util.Optional;

import static org.cafebabepy.util.ProtocolNames.__call__;

/**
 * Created by yotchang4s on 2017/05/30.
 */
public interface PyObject {

    PyObject getType();

    Python getRuntime();

    PyObjectScope getScope();

    Optional<String> getModuleName();

    void putJavaObject(String name, Object object);

    Optional<Object> getJavaObject(String name);

    default String getName() {
        return getType().getName();
    }

    default String getFullName() {
        return getModuleName() + getName();
    }

    boolean isType();

    boolean isModule();

    boolean isAppear();

    boolean isException();

    boolean isNone();

    PyObject getStr();

    String asJavaString();

    default Optional<PyObject> getObject(String name) {
        return getObject(name, true);
    }

    default Optional<PyObject> getObject(String name, boolean appear) {
        try {
            return Optional.of(getObjectOrThrow(name));

        } catch (RaiseException ignore) {
            return Optional.empty();
        }
    }

    default PyObject getObjectOrThrow(String name) {
        return getObjectOrThrow(name, true);
    }

    default PyObject getObjectOrThrow(String name, boolean appear) {
        String[] names = name.split("\\.");

        PyObject parentObject = null;
        PyObject currentObject = this;

        for (int i = 0; i < names.length; i++) {
            Optional<PyObject> currentObjectOpt = currentObject.getScope().get(names[i], appear);
            if (!currentObjectOpt.isPresent()) {
                if (parentObject == null || parentObject.isModule()) {
                    throw getRuntime().newRaiseException("builtins.NameErrorType",
                            "name '"
                                    + names[i]
                                    + "' is not defined");

                } else if (parentObject.isModule()) {
                    throw getRuntime().newRaiseException("builtins.AttributeError",
                            "module '"
                                    + parentObject.getName()
                                    + "' has no attribute '"
                                    + names[i]
                                    + "'");

                } else if (parentObject.isType()) {
                    throw getRuntime().newRaiseException("builtins.AttributeError",
                            "type object '"
                                    + parentObject.getFullName()
                                    + "' has no attribute '"
                                    + names[i]
                                    + "'");

                } else {
                    throw getRuntime().newRaiseException("builtins.AttributeError",
                            "'"
                                    + parentObject.getFullName()
                                    + "' object has no attribute '"
                                    + names[i]
                                    + "'");
                }
            }

            parentObject = currentObject;
            currentObject = currentObjectOpt.get();
        }

        return currentObject;
    }

    default PyObject call(PyObject... args) {
        Optional<PyObject> callableObjectOpt = getScope().get(__call__);
        if (!callableObjectOpt.isPresent()) {
            throw getRuntime().newRaiseException("builtins.TypeError",
                    "'" + getName() + "' object is not callable");
        }
        PyObject callableObject = callableObjectOpt.get();

        return callableObject.call(args);
    }

    static PyObject callStatic(PyObject self, PyObject... args) {
        return self.call(args);
    }
}
