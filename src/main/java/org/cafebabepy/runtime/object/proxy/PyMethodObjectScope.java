package org.cafebabepy.runtime.object.proxy;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.PyObjectScope;
import org.cafebabepy.runtime.object.proxy.PyMethodTypeObject;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by yotchang4s on 2017/05/24.
 */
public class PyMethodObjectScope extends PyObjectScope {

    private volatile Map<String, PyObject> methodMap;
    private volatile Map<String, PyObject> notAppearMethodMap;

    public PyMethodObjectScope(PyObject source) {
        super(source);
    }

    @Override
    public Optional<PyObject> get(String name, boolean appear) {
        Optional<PyObject> objectOpt = super.get(name, appear);
        if (!objectOpt.isPresent()) {
            return Optional.empty();
        }
        PyObject object = objectOpt.get();
        if (object.isCallable()) {
            synchronized (this) {
                if (appear) {
                    if (this.methodMap == null) {
                        this.methodMap = new LinkedHashMap<>();
                    }

                    PyObject method = this.methodMap.get(name);
                    if (method == null) {
                        method = new PyMethodTypeObject(getSource().getRuntime(), getSource(), object);
                        this.methodMap.put(name, method);
                    }

                    return Optional.of(method);

                } else {
                    if (this.notAppearMethodMap == null) {
                        this.notAppearMethodMap = new LinkedHashMap<>();
                    }

                    PyObject method = this.notAppearMethodMap.get(name);
                    if (method == null) {
                        method = new PyMethodTypeObject(getSource().getRuntime(), getSource(), object);
                        this.notAppearMethodMap.put(name, method);
                    }

                    return Optional.of(method);
                }
            }
        }

        return objectOpt;
    }
}
