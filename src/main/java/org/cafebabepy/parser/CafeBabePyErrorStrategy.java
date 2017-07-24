package org.cafebabepy.parser;

import org.antlr.v4.runtime.*;
import org.cafebabepy.runtime.Python;

/**
 * Created by yotchang4s on 2017/07/16.
 */
public class CafeBabePyErrorStrategy extends DefaultErrorStrategy {

    protected final Python runtime;

    public CafeBabePyErrorStrategy(Python runtime) {
        this.runtime = runtime;
    }

    @Override
    protected void reportMissingToken(Parser recognizer) {
        throw this.runtime.newRaiseException("builtins.SyntaxError",
                "invalid syntax");
    }

    @Override
    protected void reportInputMismatch(Parser recognizer, InputMismatchException e) {
        throw this.runtime.newRaiseException("builtins.SyntaxError",
                "invalid syntax");
    }

    @Override
    protected void reportUnwantedToken(Parser recognizer) {
        throw this.runtime.newRaiseException("builtins.SyntaxError",
                "invalid syntax");
    }

    @Override
    protected void reportFailedPredicate(Parser recognizer, FailedPredicateException e) {
        throw this.runtime.newRaiseException("builtins.SyntaxError",
                "invalid syntax");
    }

    @Override
    protected void reportNoViableAlternative(Parser recognizer, NoViableAltException e) {
        throw this.runtime.newRaiseException("builtins.SyntaxError",
                "invalid syntax");
    }
}
