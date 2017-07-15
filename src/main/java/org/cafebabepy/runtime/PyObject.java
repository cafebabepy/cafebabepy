package org.cafebabepy.runtime;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by yotchang4s on 2017/05/30.
 */
public interface PyObject {

    PyObject getType();

    List<PyObject> getBases();

    List<PyObject> getTypes();

    Python getRuntime();

    PyObjectScope getScope();

    void pushScope();

    PyObjectScope popScope();

    Optional<String> getModuleName();

    String getName();

    String getFullName();

    void preInitialize();

    void postInitialize();

    boolean isType();

    boolean isModule();

    boolean isAppear();

    boolean isNone();

    boolean isNotImplemented();

    PyObject getStr();

    String asJavaString();

    boolean isException();

    boolean isTrue();

    boolean isFalse();

    Map<String, PyObject> getObjects();

    Map<String, PyObject> getObjects(boolean appear);

    Optional<PyObject> getObject(String name);

    Optional<PyObject> getObject(String name, boolean appear);

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

    PyObject call(PyObject arg1,
                  PyObject... args);

    PyObject call(PyObject arg1,
                  PyObject arg2,
                  PyObject... args);

    PyObject call(PyObject arg1,
                  PyObject arg2,
                  PyObject arg3,
                  PyObject... args);

    PyObject call(PyObject arg1,
                  PyObject arg2,
                  PyObject arg3,
                  PyObject arg4,
                  PyObject... args);

    PyObject call(PyObject arg1,
                  PyObject arg2,
                  PyObject arg3,
                  PyObject arg4,
                  PyObject arg5,
                  PyObject... args);

    PyObject call(PyObject... args);
}