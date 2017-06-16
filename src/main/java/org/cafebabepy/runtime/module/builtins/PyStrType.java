package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefineCafeBabePyType(name = "builtins.str")
public class PyStrType extends AbstractCafeBabePyType {

    public static final String JAVA_STRING_NAME = "string";

    public PyStrType(Python runtime) {
        super(runtime);
    }

    public static PyObject newStr(Python runtime, String value) {
        PyObject object = runtime.newPyObject("builtins.str");

        object.putJavaObject(JAVA_STRING_NAME, value);

        return object;
    }
}
