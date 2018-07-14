package org.cafebabepy.runtime;

import java.util.List;

/**
 * Created by yotchang4s on 2017/05/13.
 */
public class RaiseException extends RuntimeException {
    private final PyObject exception;

    public RaiseException(PyObject exception) {
        super(createMessage(exception));

        this.exception = exception;
    }

    @SuppressWarnings("unchecked")
    private static String createMessage(PyObject exception) {
        List<PyObject> argList = exception.getRuntime().getattr(exception, "args").toJava(List.class);

        StringBuilder builder = new StringBuilder();
        builder.append(exception.getFullName());
        builder.append("(");

        for (int i = 0; i < argList.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(argList.get(i));
        }

        builder.append(")");

        return builder.toString();
    }

    public PyObject getException() {
        return this.exception;
    }
}
