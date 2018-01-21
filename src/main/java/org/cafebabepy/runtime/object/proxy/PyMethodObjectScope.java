package org.cafebabepy.runtime.object.proxy;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.PyObjectScope;

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
        Optional<PyObject> thisOnlyOpt = super.getThisOnly(name, appear);
        if (thisOnlyOpt.isPresent()) {
            return thisOnlyOpt;
        }

        Optional<PyObject> objectOpt;

        objectOpt = super.getFromTypes(name, appear);
        if (!objectOpt.isPresent()) {
            objectOpt = super.getFromType(name, appear);
            if (!objectOpt.isPresent()) {
                objectOpt = super.getFromParent(name, appear);
                if (!objectOpt.isPresent()) {
                    return Optional.empty();
                }
            }
        }

        PyObject object = objectOpt.get();
        if (!object.isCallable()) {
            return objectOpt;

        } else {
            synchronized (this) {
                if (object.getRuntime().isInstance(object, "types.MethodType")) {
                    if (object instanceof PyMethodTypeObject) {
                        object = ((PyMethodTypeObject) object).getFunction();

                    } else {
                        // Direct add types.MethodType to scope
                    }

                }
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
    }
}
