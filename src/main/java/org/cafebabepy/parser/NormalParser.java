package org.cafebabepy.parser;

import org.antlr.v4.runtime.ParserRuleContext;
import org.cafebabepy.runtime.Python;

import java.util.Optional;

public class NormalParser extends AbstractParser {

    public NormalParser(Python runtime) {
        super(runtime);
    }

    @Override
    Optional<ParserRuleContext> parse(
            String input, CafeBabePyLexer lexer, CafeBabePyParser parser) {
        return Optional.of(parser.file_input());
    }
}
