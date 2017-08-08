package org.cafebabepy.parser;

import org.antlr.v4.runtime.*;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.RaiseException;

import java.util.Optional;

abstract class AbstractParser implements Parser {
    protected final Python runtime;

    AbstractParser(Python runtime) {
        this.runtime = runtime;
    }

    @Override
    public PyObject parse(String input) throws RaiseException {
        CodePointCharStream stream = CharStreams.fromString(input);

        CafeBabePyLexer lexer = new CafeBabePyLexer(stream);
        TokenStream tokens = new CommonTokenStream(lexer);
        CafeBabePyParser parser = new CafeBabePyParser(tokens);

        ANTLRErrorStrategy errorHandler = new CafeBabePyErrorStrategy(this.runtime);
        parser.setErrorHandler(errorHandler);

        Optional<ParserRuleContext> rootContext = parse(input, lexer, parser);
        if (!rootContext.isPresent()) {
            return this.runtime.None();
        }

        CafeBabePyAstCreateVisitor creator = new CafeBabePyAstCreateVisitor(this.runtime);
        return creator.visit(rootContext.get());
    }

    abstract Optional<ParserRuleContext> parse(
            String input, CafeBabePyLexer lexer, CafeBabePyParser parser);
}
