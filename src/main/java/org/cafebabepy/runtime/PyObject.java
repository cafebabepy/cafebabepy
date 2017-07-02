package org.cafebabepy.runtime;

import org.cafebabepy.util.LazyMap;

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

    PyObject getStr();

    String asJavaString();

    boolean isException();

    boolean isTrue();

    boolean isFalse();

    LazyMap<String, Supplier<PyObject>> getLazyObjects();

    LazyMap<String, Supplier<PyObject>> getLazyObjects(boolean appear);

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

    PyObject call(PyObject self);

    PyObject call(PyObject self,
                  PyObject arg1);

    PyObject call(PyObject self,
                  PyObject arg1,
                  PyObject arg2);

    PyObject call(PyObject self,
                  PyObject arg1,
                  PyObject arg2,
                  PyObject arg3);

    PyObject call(PyObject self,
                  PyObject arg1,
                  PyObject arg2,
                  PyObject arg3,
                  PyObject arg4);

    PyObject call(PyObject self,
                  PyObject arg1,
                  PyObject arg2,
                  PyObject arg3,
                  PyObject arg4,
                  PyObject arg5);

    PyObject call(PyObject self,
                  PyObject arg1,
                  PyObject... args);

    PyObject call(PyObject self,
                  PyObject arg1,
                  PyObject arg2,
                  PyObject... args);

    PyObject call(PyObject self,
                  PyObject arg1,
                  PyObject arg2,
                  PyObject arg3,
                  PyObject... args);

    PyObject call(PyObject self,
                  PyObject arg1,
                  PyObject arg2,
                  PyObject arg3,
                  PyObject arg4,
                  PyObject... args);

    PyObject call(PyObject self,
                  PyObject arg1,
                  PyObject arg2,
                  PyObject arg3,
                  PyObject arg4,
                  PyObject arg5,
                  PyObject... args);

    PyObject call(PyObject self,
                  PyObject... args);

    PyObject callSelf();

    PyObject callSelf(PyObject arg1);

    PyObject callSelf(PyObject arg1,
                      PyObject arg2);

    PyObject callSelf(PyObject arg1,
                      PyObject arg2,
                      PyObject arg3);

    PyObject callSelf(PyObject arg1,
                      PyObject arg2,
                      PyObject arg3,
                      PyObject arg4);

    PyObject callSelf(PyObject arg1,
                      PyObject arg2,
                      PyObject arg3,
                      PyObject arg4,
                      PyObject arg5);

    PyObject callSelf(PyObject arg1,
                      PyObject... args);

    PyObject callSelf(PyObject arg1,
                      PyObject arg2,
                      PyObject... args);

    PyObject callSelf(PyObject arg1,
                      PyObject arg2,
                      PyObject arg3,
                      PyObject... args);

    PyObject callSelf(PyObject arg1,
                      PyObject arg2,
                      PyObject arg3,
                      PyObject arg4,
                      PyObject... args);

    PyObject callSelf(PyObject arg1,
                      PyObject arg2,
                      PyObject arg3,
                      PyObject arg4,
                      PyObject arg5,
                      PyObject... args);

    PyObject callSelf(PyObject... args);
}