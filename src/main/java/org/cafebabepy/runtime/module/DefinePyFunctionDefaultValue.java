package org.cafebabepy.runtime.module;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by yotchang4s on 2017/07/08.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DefinePyFunctionDefaultValue {

    String methodName();

    String parameterName();
}
