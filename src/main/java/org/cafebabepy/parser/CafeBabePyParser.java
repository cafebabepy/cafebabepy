package org.cafebabepy.parser;

import org.antlr.v4.runtime.*;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.RaiseException;

import java.util.Optional;

/**
 * Created by yotchang4s on 2017/05/28.
 */
public class CafeBabePyParser {

    protected final Python runtime;

    public CafeBabePyParser(Python runtime) {
        this.runtime = runtime;
    }

    public PyObject parse(String input) throws RaiseException {
        CodePointCharStream stream = CharStreams.fromString(input);

        CafeBabePyLexer lexer = new CafeBabePyLexer(stream);
        TokenStream tokens = new CommonTokenStream(lexer);
        PythonParser parser = new PythonParser(tokens);

        ANTLRErrorStrategy errorHandler = new CafeBabePyErrorStrategy(this.runtime);
        parser.setErrorHandler(errorHandler);

        Optional<ParserRuleContext> rootContext = parse(input, parser);
        if (!rootContext.isPresent()) {
            return this.runtime.None();
        }

        CafeBabePyAstCreateVisitor creator = new CafeBabePyAstCreateVisitor(this.runtime);
        return creator.visit(rootContext.get());
    }

    protected Optional<ParserRuleContext> parse(String input, PythonParser parser) {
        return Optional.of(parser.file_input());
    }
}
