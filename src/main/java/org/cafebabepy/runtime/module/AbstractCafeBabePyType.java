package org.cafebabepy.runtime.module;

import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.PyObjectScope;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.object.proxy.PyMethodObjectScope;
import org.cafebabepy.util.StringUtils;

/**
 * Created by yotchang4s on 2017/05/30.
 */
public abstract class AbstractCafeBabePyType extends AbstractAbstractCafeBabePyAny {

    private PyObject module;

    private String name;

    private String[] baseNames;

    private volatile PyObjectScope scope;

    protected AbstractCafeBabePyType(Python runtime) {
        super(runtime);
    }

    @Override
    public PyObjectScope getScope() {
        if (this.scope == null) {
            synchronized (this) {
                if(this.scope == null) {
                    this.scope = new PyMethodObjectScope(this);
                }
            }
        }

        return this.scope;
    }

    @Override
    final String[] getBaseNames() {
        return this.baseNames;
    }

    @Override
    public void defineClass() {
        Class<?> clazz = getClass();

        DefinePyType definePyType = clazz.getAnnotation(DefinePyType.class);
        if (definePyType == null) {
            throw new CafeBabePyException(
                    "DefinePyModule annotation is not defined " + clazz.getName());
        }

        this.baseNames = definePyType.parent();

        String[] splitStr = StringUtils.splitLastDot(definePyType.name());

        if (StringUtils.isEmpty(splitStr[0])) {
            throw new CafeBabePyException("name '"
                    + definePyType.name()
                    + "' is not found module");
        }

        this.module = this.runtime.moduleOrThrow(splitStr[0]);
        this.name = splitStr[1];
        this.module.getScope().put(this.name, this, definePyType.appear());
    }

    @Override
    public final PyObject getType() {
        return this.runtime.typeOrThrow("builtins.type");
    }

    @Override
    public final PyObject getModule() {
        return this.module;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public final boolean isType() {
        return true;
    }

    @Override
    public final boolean isModule() {
        return false;
    }

    @Override
    public PyObject call(PyObject... args) {
        return getCallable().call(args);
    }
}
