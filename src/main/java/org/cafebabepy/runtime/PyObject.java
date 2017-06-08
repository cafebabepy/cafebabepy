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

    String getName();

    String getFullName();

    boolean isType();

    boolean isModule();

    boolean isAppear();

    boolean isNone();

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
                  PyObject... args);

    PyObject callThis();

    PyObject callThis(PyObject arg1);

    PyObject callThis(PyObject arg1,
                      PyObject arg2);

    PyObject callThis(PyObject arg1,
                      PyObject arg2,
                      PyObject arg3);

    PyObject callThis(PyObject arg1,
                      PyObject arg2,
                      PyObject arg3,
                      PyObject arg4);

    PyObject callThis(PyObject arg1,
                      PyObject arg2,
                      PyObject arg3,
                      PyObject arg4,
                      PyObject arg5);

    PyObject callThis(PyObject... args);

    PyObject call(PyObject... args);

    static PyObject callStatic(PyObject self, PyObject... args) {
        return self.call(args);
    }

    static PyObject callStatic(PyObject self) {
        return self.callThis(self);
    }

    static PyObject callStatic(PyObject self,
                               PyObject arg1) {
        return self.callThis(arg1);
    }

    static PyObject callStatic(PyObject self,
                               PyObject arg1,
                               PyObject arg2) {
        return self.callThis(arg1, arg2);
    }


    static PyObject callStatic(PyObject self,
                               PyObject arg1,
                               PyObject arg2,
                               PyObject arg3) {
        return self.callThis(arg1, arg2, arg3);
    }

    static PyObject callStatic(PyObject self,
                               PyObject arg1,
                               PyObject arg2,
                               PyObject arg3,
                               PyObject arg4) {
        return self.callThis(arg1, arg2, arg3, arg4);
    }

    static PyObject callStatic(PyObject self,
                               PyObject arg1,
                               PyObject arg2,
                               PyObject arg3,
                               PyObject arg4,
                               PyObject arg5) {
        return self.callThis(arg1, arg2, arg3, arg4, arg5);
    }
}