package org.cafebabepy.runtime;

import java.util.Optional;

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
        PyObject parentObject = null;
        PyObject currentObject = this;

        Optional<PyObject> currentObjectOpt = currentObject.getScope().get(name, appear);
        if (!currentObjectOpt.isPresent()) {
            if (parentObject == null || parentObject.isModule()) {
                throw getRuntime().newRaiseException("builtins.NameError",
                        "name '"
                                + name
                                + "' is not defined");

            } else if (parentObject.isModule()) {
                throw getRuntime().newRaiseException("builtins.AttributeError",
                        "module '"
                                + parentObject.getName()
                                + "' has no attribute '"
                                + name
                                + "'");

            } else if (parentObject.isType()) {
                throw getRuntime().newRaiseException("builtins.AttributeError",
                        "type object '"
                                + parentObject.getFullName()
                                + "' has no attribute '"
                                + name
                                + "'");

            } else {
                throw getRuntime().newRaiseException("builtins.AttributeError",
                        "'"
                                + parentObject.getFullName()
                                + "' object has no attribute '"
                                + name
                                + "'");
            }
        }

        return currentObjectOpt.get();
    }

    default PyObject call(PyObject self) {
        PyObject[] objects = new PyObject[1];
        objects[0] = self;

        return call(objects);
    }

    default PyObject call(PyObject self,
                          PyObject arg1) {
        PyObject[] objects = new PyObject[2];
        objects[0] = self;
        objects[1] = arg1;

        return call(objects);
    }

    default PyObject call(PyObject self,
                          PyObject arg1,
                          PyObject arg2) {
        PyObject[] objects = new PyObject[3];
        objects[0] = self;
        objects[1] = arg1;
        objects[2] = arg2;

        return call(objects);
    }

    default PyObject call(PyObject self,
                          PyObject arg1,
                          PyObject arg2,
                          PyObject arg3) {
        PyObject[] objects = new PyObject[4];
        objects[0] = self;
        objects[1] = arg1;
        objects[2] = arg2;
        objects[3] = arg3;

        return call(objects);
    }

    default PyObject call(PyObject self,
                          PyObject arg1,
                          PyObject arg2,
                          PyObject arg3,
                          PyObject arg4) {
        PyObject[] objects = new PyObject[5];
        objects[0] = self;
        objects[1] = arg1;
        objects[2] = arg2;
        objects[3] = arg3;
        objects[4] = arg4;

        return call(objects);
    }

    default PyObject call(PyObject self,
                          PyObject arg1,
                          PyObject arg2,
                          PyObject arg3,
                          PyObject arg4,
                          PyObject arg5) {
        PyObject[] objects = new PyObject[6];
        objects[0] = self;
        objects[1] = arg1;
        objects[2] = arg2;
        objects[3] = arg3;
        objects[4] = arg4;
        objects[5] = arg5;

        return call(objects);
    }

    default PyObject call(PyObject self, PyObject... args) {
        PyObject[] objects = new PyObject[args.length + 1];
        System.arraycopy(args, 0, objects, 1, args.length);
        objects[0] = self;

        return call(objects);
    }

    PyObject call(PyObject... args);

    static PyObject callStatic(PyObject self, PyObject... args) {
        return self.call(args);
    }
}
