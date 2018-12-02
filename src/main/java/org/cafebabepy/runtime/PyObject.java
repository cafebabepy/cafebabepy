package org.cafebabepy.runtime;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by yotchang4s on 2017/05/30.
 */
public interface PyObject {

    PyObject getType();

    List<PyObject> getBases();

    List<PyObject> getTypes();

    Python getRuntime();

    Frame getFrame();

    PyObject getModule();

    String getName();

    String getFullName();

    void initialize();

    boolean isType();

    boolean isModule();

    boolean isCallable();

    boolean isNone();

    boolean isNotImplemented();

    boolean isEllipsis();

    boolean isException();

    boolean isTrue();

    boolean isFalse();

    boolean existsDict();

    <T> T toJava(Class<T> clazz);

    PyObject call(PyObject... args);

    PyObject call(PyObject[] args, LinkedHashMap<String, PyObject> keywords);
}