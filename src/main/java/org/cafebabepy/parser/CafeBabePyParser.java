package org.cafebabepy.parser;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.RaiseException;

/**
 * Created by yotchang4s on 2017/05/28.
 */
public class CafeBabePyParser {

    private final Python runtime;

    public CafeBabePyParser(Python runtime) {
        this.runtime = runtime;
    }

    public PyObject parse(String input) throws RaiseException {
        this.runtime.initializeBuiltins("org.cafebabepy.runtime.module.ast");
        this.runtime.initializeBuiltins("org.cafebabepy.runtime.module._ast");

        CodePointCharStream stream = CharStreams.fromString(input);
        PythonLexer lexer = new PythonLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        PythonParser parser = new PythonParser(tokens);

        ParserRuleContext rootContext = parser.file_input();

        CafeBabePyAstCreateVisitor creator = new CafeBabePyAstCreateVisitor(this.runtime);
        return creator.visit(rootContext);
    }
}
