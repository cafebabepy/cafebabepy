package org.cafebabepy.parser;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.RaiseException;

public interface Parser {
    PyObject parse(String file, String input) throws RaiseException;
}
