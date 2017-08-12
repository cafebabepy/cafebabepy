package org.cafebabepy.runtime.module;

import org.cafebabepy.annotation.DefineCafeBabePyFunction;
import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.util.StringUtils;

import static org.cafebabepy.util.ProtocolNames.*;

/**
 * Created by yotchang4s on 2017/05/30.
 */
public abstract class AbstractCafeBabePyType extends AbstractAbstractCafeBabePyAny {

    private PyObject module;

    private String name;

    private String[] baseNames;

    protected AbstractCafeBabePyType(Python runtime) {
        super(runtime);
    }

    @Override
    String[] getBaseNames() {
        return this.baseNames;
    }

    @Override
    public void defineClass() {
        Class<?> clazz = getClass();

        DefineCafeBabePyType defineCafeBabePyType = clazz.getAnnotation(DefineCafeBabePyType.class);
        if (defineCafeBabePyType == null) {
            throw new CafeBabePyException(
                    "DefineCafeBabePyModule annotation is not defined " + clazz.getName());
        }

        this.baseNames = defineCafeBabePyType.parent();

        String[] splitStr = StringUtils.splitLastDot(defineCafeBabePyType.name());

        if (StringUtils.isEmpty(splitStr[0])) {
            throw new CafeBabePyException("name '"
                    + defineCafeBabePyType.name()
                    + "' is not found module");
        }

        this.module = this.runtime.moduleOrThrow(splitStr[0]);
        this.name = splitStr[1];
        this.module.getScope().put(this.name, this, defineCafeBabePyType.appear());
    }

    @Override
    public String asJavaString() {
        return "<class '" + getFullName() + "'>";
    }

    @Override
    public PyObject getType() {
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
    public boolean isType() {
        return true;
    }

    @Override
    public boolean isModule() {
        return false;
    }

    @Override
    public PyObject call(PyObject... args) {
        return getCallable().call(args);
    }

    @DefineCafeBabePyFunction(name = __call__)
    public PyObject __call__(PyObject... args) {
        PyObject object = getScope().getOrThrow(__new__).call(this);
        object.getScope().getOrThrow(__init__).call(object, args);

        return object;
    }
}
