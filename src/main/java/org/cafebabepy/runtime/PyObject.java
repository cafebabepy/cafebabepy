package org.cafebabepy.runtime;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by yotchang4s on 2017/05/30.
 */
public interface PyObject {

    PyObject getType();

    List<PyObject> getSuperTypes();

    Python getRuntime();

    PyObjectScope getScope();

    Optional<String> getModuleName();

    // FIXME Remove
    void putJavaObject(String name, Object object);

    // FIXME Remove
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

    default Map<String, PyObject> getObjects() {
        return getObjects(true);
    }

    default Map<String, PyObject> getObjects(boolean appear) {
        return getScope().gets(appear);
    }

    default Optional<PyObject> getObject(String name) {
        return getObject(name, true);
    }

    default Optional<PyObject> getObject(String name, boolean appear) {
        return getScope().get(name, appear);
    }

    default PyObject getObjectOrThrow(String name) {
        return getObjectOrThrow(name, true);
    }

    default PyObject getObjectOrThrow(String name, boolean appear) {
        Optional<PyObject> objectOpt = getObject(name, appear);
        if (!objectOpt.isPresent()) {
            if (isModule()) {
                throw getRuntime().newRaiseException("builtins.NameError",
                        "name '"
                                + name
                                + "' is not defined");

            } else if (isType()) {
                throw getRuntime().newRaiseException("builtins.AttributeError",
                        "type object '"
                                + getFullName()
                                + "' has no attribute '"
                                + name
                                + "'");

            } else {
                throw getRuntime().newRaiseException("builtins.AttributeError",
                        "'"
                                + getFullName()
                                + "' object has no attribute '"
                                + name
                                + "'");
            }
        }

        return objectOpt.get();
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
