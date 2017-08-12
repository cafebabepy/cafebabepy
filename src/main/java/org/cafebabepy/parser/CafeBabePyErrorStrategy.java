package org.cafebabepy.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.Parser;
import org.cafebabepy.runtime.Python;

/**
 * Created by yotchang4s on 2017/07/16.
 */
class CafeBabePyErrorStrategy extends DefaultErrorStrategy {

    final Python runtime;

    CafeBabePyErrorStrategy(Python runtime) {
        this.runtime = runtime;
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
        throw this.runtime.newRaiseException("builtins.SyntaxError",
                "invalid syntax");
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
