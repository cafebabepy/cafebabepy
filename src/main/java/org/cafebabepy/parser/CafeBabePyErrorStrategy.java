package org.cafebabepy.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.Parser;
import org.cafebabepy.runtime.Python;

/**
 * Created by yotchang4s on 2017/07/16.
 */
class CafeBabePyErrorStrategy extends DefaultErrorStrategy {

    final Python runtime;
    final String input;
    final String file;

    CafeBabePyErrorStrategy(Python runtime, String file, String input) {
        this.runtime = runtime;
        this.input = input;
        this.file = file;
    }

    @Override
    protected void reportMissingToken(Parser recognizer) {
        throw this.runtime.newRaiseException("builtins.SyntaxError",
                "invalid syntax");
    }

    @Override
    protected void reportInputMismatch(org.antlr.v4.runtime.Parser recognizer, InputMismatchException e) {
        throw this.runtime.newRaiseException("builtins.SyntaxError",
                "invalid syntax");
    }

    @Override
    protected void reportUnwantedToken(org.antlr.v4.runtime.Parser recognizer) {
        Token currentToken = recognizer.getCurrentToken();
        int line = currentToken.getLine();
        int position = currentToken.getCharPositionInLine();

        String lineInput = this.input.split("(\r\n|\r|\n)")[line - 1];

        throw this.runtime.newRaiseException("builtins.SyntaxError",
                this.runtime.str("invalid syntax"),
                this.runtime.str(this.file),
                this.runtime.number(line),
                this.runtime.number(position),
                this.runtime.str(lineInput));
    }

    @Override
    protected void reportFailedPredicate(org.antlr.v4.runtime.Parser recognizer, FailedPredicateException e) {
        throw this.runtime.newRaiseException("builtins.SyntaxError",
                "invalid syntax");
    }

    @Override
    protected void reportNoViableAlternative(org.antlr.v4.runtime.Parser recognizer, NoViableAltException e) {
        throw this.runtime.newRaiseException("builtins.SyntaxError",
                "invalid syntax");
    }
}
