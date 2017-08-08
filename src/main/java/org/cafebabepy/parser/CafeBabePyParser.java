package org.cafebabepy.parser;

import org.antlr.v4.runtime.TokenStream;
import org.cafebabepy.parser.antlr.PythonParser;

/**
 * Created by yotchang4s on 2017/05/28.
 */
class CafeBabePyParser extends PythonParser {

    CafeBabePyParser(TokenStream input) {
        super(input);
    }
}
