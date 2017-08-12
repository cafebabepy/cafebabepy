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

    PyObject getModule();

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