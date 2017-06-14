package org.cafebabepy.runtime;

import org.cafebabepy.util.LazyHashMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Created by yotchang4s on 2017/05/30.
 */
public interface PyObject {

    PyObject getType();

    List<PyObject> getBases();

    List<PyObject> getTypes();

    Python getRuntime();

    PyObjectScope getScope();

    Optional<String> getModuleName();

    // FIXME Remove
    void putJavaObject(String name, Object object);

    // FIXME Remove
    Optional<Object> getJavaObject(String name);

    String getName();

    String getFullName();

    void preInitialize();

    void postInitialize();

    boolean isType();

    boolean isModule();

    boolean isAppear();

    boolean isNone();

    PyObject getStr();

    String asJavaString();

    boolean isException();

    boolean isTrue();

    boolean isFalse();

    LazyHashMap<String, Supplier<PyObject>> getLazyObjects();

    LazyHashMap<String, Supplier<PyObject>> getLazyObjects(boolean appear);

    Map<String, PyObject> getObjects();

    Map<String, PyObject> getObjects(boolean appear);

    Supplier<Optional<PyObject>> getLazyObject(String name);

    Supplier<Optional<PyObject>> getLazyObject(String name, boolean appear);

    Optional<PyObject> getObject(String name);

    Optional<PyObject> getObject(String name, boolean appear);

    Supplier<PyObject> getLazyObjectOrThrow(String name);

    Supplier<PyObject> getLazyObjectOrThrow(String name, boolean appear);

    PyObject getObjectOrThrow(String name);

    PyObject getObjectOrThrow(String name, boolean appear);

    PyObject getCallable();

    PyObject call();

    PyObject call(PyObject arg1);

    PyObject call(PyObject arg1,
                  PyObject arg2);

    PyObject call(PyObject arg1,
                  PyObject arg2,
                  PyObject arg3);

    PyObject call(PyObject arg1,
                  PyObject arg2,
                  PyObject arg3,
                  PyObject arg4);

    PyObject call(PyObject arg1,
                  PyObject arg2,
                  PyObject arg3,
                  PyObject arg4,
                  PyObject arg5);

    PyObject call(PyObject... args);

    static PyObject callStatic(PyObject self) {
        return self.call(self);
    }

    static PyObject callStatic(PyObject target,
                               PyObject arg1) {
        return target.call(arg1);
    }

    static PyObject callStatic(PyObject target,
                               PyObject arg1,
                               PyObject arg2) {
        return target.call(arg1, arg2);
    }


    static PyObject callStatic(PyObject target,
                               PyObject arg1,
                               PyObject arg2,
                               PyObject arg3) {
        return target.call(arg1, arg2, arg3);
    }

    static PyObject callStatic(PyObject target,
                               PyObject arg1,
                               PyObject arg2,
                               PyObject arg3,
                               PyObject arg4) {
        return target.call(arg1, arg2, arg3, arg4);
    }

    static PyObject callStatic(PyObject target,
                               PyObject arg1,
                               PyObject arg2,
                               PyObject arg3,
                               PyObject arg4,
                               PyObject arg5) {
        return target.call(arg1, arg2, arg3, arg4, arg5);
    }

    static PyObject callStatic(PyObject target, PyObject... args) {
        return target.call(args);
    }
}