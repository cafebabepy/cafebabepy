package org.cafebabepy.parser;

import org.antlr.v4.runtime.Token;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.RaiseException;

final class ParserUtils {
    private ParserUtils() {
    }

    static RaiseException syntaxError(Python runtime, String message, Token token) {
        return runtime.newRaiseException("builtins.SyntaxError",
                runtime.str(message),
                runtime.str("FIXME FILE"),
                runtime.number(token.getLine()),
                runtime.number(token.getCharPositionInLine()),
                runtime.str("FIXME INPUT"));
    }
}
