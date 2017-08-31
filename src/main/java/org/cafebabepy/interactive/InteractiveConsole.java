package org.cafebabepy.interactive;

import jline.Terminal;
import jline.TerminalFactory;
import jline.console.ConsoleReader;
import org.cafebabepy.evaluter.Interpret.InterpretEvaluator;
import org.cafebabepy.parser.InteractiveParser;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.RaiseException;

import java.io.IOException;

/**
 * Created by yotchang4s on 2017/07/15.
 */
public class InteractiveConsole {

    private final static String PROMPT_READLINE = ">>> ";
    private final static String PROMPT_INCOMPLETE = "... ";

    private ConsoleReader consoleReader;

    private Status status = Status.READLINE;

    private Python runtime;

    private enum Status {
        READLINE,
        INCOMPLETE,
        QUIT
    }

    public InteractiveConsole(Python runtime) {
        this.runtime = runtime;
    }

    private void printBanner() throws IOException {
        String banner1 = String.format("%s %s",
                Python.APPLICATION_NAME, Python.VERSION);

        String banner2 = String.format("[%s (%s)] on Java %s",
                System.getProperty("java.vm.name"),
                System.getProperty("java.vm.specification.vendor"),
                System.getProperty("java.runtime.version"));

        String banner3 = "Type :help for help, :quit for quit";

        this.consoleReader.println(banner1);
        this.consoleReader.println(banner2);
        this.consoleReader.println(banner3);
    }

    private String readLine() throws IOException {
        String prompt = status == Status.READLINE ? PROMPT_READLINE : PROMPT_INCOMPLETE;

        return this.consoleReader.readLine(prompt);
    }

    public void interact() {
        InteractiveParser parser = new InteractiveParser(this.runtime);
        InterpretEvaluator evaluator = new InterpretEvaluator(this.runtime);

        Terminal terminal = TerminalFactory.get();
        terminal.setEchoEnabled(false);

        try {
            terminal.init();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        try (ConsoleReader consoleReader = new ConsoleReader(System.in, System.out, terminal)) {
            this.consoleReader = consoleReader;

            printBanner();

            StringBuilder buffer = new StringBuilder();
            while (status != Status.QUIT) {
                consoleReader.flush();
                String line = readLine();
                if (line == null) {
                    break;
                }

                if (buffer.length() > 0) {
                    buffer.append(System.lineSeparator());
                }
                buffer.append(line);

                PyObject ast;
                try {
                    ast = parser.parse(buffer.toString());
                    if (ast.isNone()) {
                        this.status = Status.INCOMPLETE;
                        continue;
                    }

                } catch (RaiseException e) {
                    e.printStackTrace();
                    this.consoleReader.println(e.getException().asJavaString());
                    this.status = Status.READLINE;
                    buffer.setLength(0);
                    continue;
                }

                this.status = Status.READLINE;
                buffer.setLength(0);

                PyObject result;

                try {
                    result = evaluator.evalMainModule(ast);
                    if (!result.isNone()) {
                        this.consoleReader.println(result.asJavaString());
                    }

                } catch (RaiseException e) {
                    PyObject exception = e.getException();
                    this.consoleReader.println(exception.asJavaString());
                    this.consoleReader.println(e.getMessage());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
