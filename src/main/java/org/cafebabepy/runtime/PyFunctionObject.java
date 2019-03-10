package org.cafebabepy.runtime;

import java.util.List;

public interface PyFunctionObject extends PyObject {
    List<String> getArguments();
}
