package org.cafebabepy.interactive;

import org.antlr.v4.runtime.*;
import org.cafebabepy.parser.*;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.RaiseException;

import java.util.Optional;

/**
 * Created by yotchang4s on 2017/07/16.
 */
public class InteractiveParser extends CafeBabePyParser {

    public InteractiveParser(Python runtime) {
        super(runtime);
    }

    @Override
    protected Optional<ParserRuleContext> parse(String input, PythonParser parser) {
        try {
            return Optional.of(parser.single_input());

        } catch (Throwable e) {
            if (validate(input)) {
                return Optional.empty();
            }

            throw e;
        }
    }

    private boolean validate(String input) throws RaiseException {
        CodePointCharStream stream = CharStreams.fromString(input);

        CafeBabePyLexer lexer = new CafeBabePyLexer(stream);
        TokenStream tokens = new CommonTokenStream(lexer);
        PythonInteractiveValidateParser parser = new PythonInteractiveValidateParser(tokens);

        ANTLRErrorStrategy errorHandler = new CafeBabePyErrorStrategy(this.runtime);
        parser.setErrorHandler(errorHandler);

        try {
            parser.single_input();
            return true;

        } catch (Throwable ignore) {
            while (lexer.nextToken().getType() != Lexer.EOF) ;

            return lexer.isOpened();
        }
    }
}
