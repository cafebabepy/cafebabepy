package org.cafebabepy.parser;

import org.antlr.v4.runtime.*;
import org.cafebabepy.parser.antlr.PythonInteractiveValidateParser;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.RaiseException;

import java.util.Optional;

/**
 * Created by yotchang4s on 2017/07/16.
 */
public class InteractiveParser extends AbstractParser {

    public InteractiveParser(Python runtime) {
        super(runtime);
    }

    @Override
    Optional<ParserRuleContext> parse(
            String input, CafeBabePyLexer lexer, CafeBabePyParser parser) {
        try {
            Optional<ParserRuleContext> result = Optional.of(parser.single_input());
            if (lexer.isLineJoining()) {
                return Optional.empty();
            }

            return result;

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

        try {
            parser.single_input();
            return true;

        } catch (Throwable ignore) {
            while (lexer.nextToken().getType() != Lexer.EOF) ;

            return lexer.isOpened() || lexer.isLineJoining();
        }
    }
}
