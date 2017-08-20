package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.annotation.DefineCafeBabePyFunction;
import org.cafebabepy.annotation.DefineCafeBabePyType;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.module.AbstractCafeBabePyType;

import static org.cafebabepy.util.ProtocolNames.__bool__;

/**
 * Created by yotchang4s on 2017/05/13.
 */
@DefineCafeBabePyType(name = "builtins.Ellipsis")
public class PyEllipsisType extends AbstractCafeBabePyType {

    public PyEllipsisType(Python runtime) {
        super(runtime);
    }
}
