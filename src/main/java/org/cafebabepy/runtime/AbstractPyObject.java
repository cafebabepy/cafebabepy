package org.cafebabepy.runtime;

import org.cafebabepy.runtime.module.builtins.PyNoneTypeType;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.cafebabepy.util.ProtocolNames.__call__;

/**
 * Created by yotchang4s on 2017/06/08.
 */
public abstract class AbstractPyObject implements PyObject {
    protected final Python runtime;

    protected final PyObjectScope scope;

    protected final boolean appear;

    private Map<String, Object> javaObjectMap;

    protected AbstractPyObject(Python runtime) {
        this(runtime, true);
    }

    protected AbstractPyObject(Python runtime, PyObjectScope parentScope) {
        this(runtime, true, parentScope);
    }

    protected AbstractPyObject(Python runtime, boolean appear) {
        this.runtime = runtime;
        this.scope = new PyObjectScope();
        this.appear = appear;
    }

    protected AbstractPyObject(Python runtime, boolean appear, PyObjectScope parentScope) {
        this.runtime = runtime;
        this.scope = new PyObjectScope(parentScope);
        this.appear = appear;
    }

    @Override
    public final Python getRuntime() {
        return this.runtime;
    }

    @Override
    public final PyObjectScope getScope() {
        return this.scope;
    }

    @Override
    public final void putJavaObject(String name, Object object) {
        if (this.javaObjectMap == null) {
            synchronized (this) {
                if (this.javaObjectMap == null) {
                    this.javaObjectMap = new ConcurrentHashMap<>();
                }
            }
        }

        this.javaObjectMap.put(name, object);
    }

    @Override
    public final Optional<Object> getJavaObject(String name) {
        if (this.javaObjectMap == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(this.javaObjectMap.get(name));
    }

    @Override
    public String getName() {
        return getType().getName();
    }

    @Override
    public final String getFullName() {
        return getModuleName() + getName();
    }

    @Override
    public boolean isAppear() {
        return this.appear;
    }

    @Override
    public final PyObject getStr() {
        return this.runtime.str(asJavaString());
    }

    @Override
    public final boolean isException() {
        // FIXME 親がBaseExceptionかどうかを判定する
        PyObject module = getRuntime().getBuiltinsModule();
        PyObject call = module.getObjectOrThrow("issubclass");

        PyObject baseExceptionType = module.getObjectOrThrow("BaseException");

        return call.callThis(getType(), baseExceptionType).isTrue();
    }

    @Override
    public final boolean isTrue() {
        return !isFalse();
    }

    @Override
    public boolean isFalse() {
        return getRuntime()
                .getBuiltinsModule()
                .getObjectOrThrow("bool").call(this) == getRuntime().False();
    }

    @Override
    public final Map<String, PyObject> getObjects() {
        return getObjects(true);
    }

    @Override
    public final Map<String, PyObject> getObjects(boolean appear) {
        return getScope().gets(appear);
    }

    @Override
    public final Optional<PyObject> getObject(String name) {
        return getObject(name, true);
    }

    @Override
    public final Optional<PyObject> getObject(String name, boolean appear) {
        return getScope().get(name, appear);
    }

    @Override
    public final PyObject getObjectOrThrow(String name) {
        return getObjectOrThrow(name, true);
    }

    @Override
    public final PyObject getObjectOrThrow(String name, boolean appear) {
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

    @Override
    public final PyObject getCallable() {
        return getObject(__call__).orElseThrow(
                () -> this.runtime.newRaiseException("builtins.TypeError",
                        "'" + getType().getName() + "' object is not callable"));
    }

    @Override
    public PyObject call(PyObject self) {
        PyObject[] objects = new PyObject[1];
        objects[0] = self;

        return call(objects);
    }

    @Override
    public PyObject call(PyObject self,
                         PyObject arg1) {
        PyObject[] objects = new PyObject[2];
        objects[0] = self;
        objects[1] = arg1;

        return call(objects);
    }

    @Override
    public PyObject call(PyObject self,
                         PyObject arg1,
                         PyObject arg2) {
        PyObject[] objects = new PyObject[3];
        objects[0] = self;
        objects[1] = arg1;
        objects[2] = arg2;

        return call(objects);
    }

    @Override
    public PyObject call(PyObject self,
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

    @Override
    public PyObject call(PyObject self,
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

    @Override
    public PyObject call(PyObject self,
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

    @Override
    public PyObject call(PyObject self, PyObject... args) {
        PyObject[] objects = new PyObject[args.length + 1];
        System.arraycopy(args, 0, objects, 1, args.length);
        objects[0] = self;

        return call(objects);
    }

    @Override
    public PyObject callThis() {
        PyObject[] objects = new PyObject[0];
        objects[0] = this;

        return call(objects);
    }

    @Override
    public PyObject callThis(PyObject arg1) {
        PyObject[] objects = new PyObject[2];
        objects[0] = this;
        objects[1] = arg1;

        return call(objects);
    }

    @Override
    public PyObject callThis(PyObject arg1,
                             PyObject arg2) {
        PyObject[] objects = new PyObject[3];
        objects[0] = this;
        objects[1] = arg1;
        objects[2] = arg2;

        return call(objects);
    }

    @Override
    public PyObject callThis(PyObject arg1,
                             PyObject arg2,
                             PyObject arg3) {
        PyObject[] objects = new PyObject[4];
        objects[0] = this;
        objects[1] = arg1;
        objects[2] = arg2;
        objects[3] = arg3;

        return call(objects);
    }

    @Override
    public PyObject callThis(PyObject arg1,
                             PyObject arg2,
                             PyObject arg3,
                             PyObject arg4) {
        PyObject[] objects = new PyObject[5];
        objects[0] = this;
        objects[1] = arg1;
        objects[2] = arg2;
        objects[3] = arg3;
        objects[4] = arg4;

        return call(objects);
    }

    @Override
    public PyObject callThis(PyObject arg1,
                             PyObject arg2,
                             PyObject arg3,
                             PyObject arg4,
                             PyObject arg5) {
        PyObject[] objects = new PyObject[6];
        objects[0] = this;
        objects[1] = arg1;
        objects[2] = arg2;
        objects[3] = arg3;
        objects[4] = arg4;
        objects[5] = arg5;

        return call(objects);
    }

    @Override
    public PyObject callThis(PyObject... args) {
        PyObject[] objects = new PyObject[args.length + 1];
        System.arraycopy(args, 0, objects, 1, args.length);
        objects[0] = this;

        return call(objects);
    }
}
