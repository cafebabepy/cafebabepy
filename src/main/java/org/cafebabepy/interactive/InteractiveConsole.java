package org.cafebabepy.interactive;

import org.cafebabepy.evaluter.Interpret.InterpretEvaluator;
import org.cafebabepy.parser.InteractiveParser;
import org.cafebabepy.runtime.CafeBabePyException;
import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.RaiseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by yotchang4s on 2017/07/15.
 */
public class InteractiveConsole {

    private final static String PROMPT_READLINE = ">>> ";
    private final static String PROMPT_INCOMPLETE = "... ";

    private Status status = Status.READLINE;

    private Python runtime;

    public InteractiveConsole(Python runtime) {
        this.runtime = runtime;
    }

    public void interact() {
        InteractiveParser parser = new InteractiveParser(this.runtime);
        InterpretEvaluator evaluator = new InterpretEvaluator(this.runtime);

        printBanner();

        this.runtime.pushContext();
        try {
            // not close
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            StringBuilder buffer = new StringBuilder();
            while (status != Status.QUIT) {
                String line = readLine(reader);
                if (line == null) {
                    break;
                }

                if (buffer.length() > 0) {
                    buffer.append(System.lineSeparator());
                }
                buffer.append(line);

                PyObject ast;
                try {
                    ast = parser.parse("<stdin>", buffer.toString());
                    if (ast.isNone()) {
                        this.status = Status.INCOMPLETE;
                        continue;
                    }

                } catch (RaiseException e) {
                    e.printStackTrace();
                    println(e.getException().toJava(String.class));
                    this.status = Status.READLINE;
                    buffer.setLength(0);
                    continue;
                }

                this.status = Status.READLINE;
                buffer.setLength(0);

                PyObject result;

                try {
                    result = evaluator.eval(ast);
                    if (!result.isNone()) {
                        println(result.toJava(String.class));
                    }

                } catch (RaiseException e) {
                    PyObject exception = e.getException();
                    println(exception.toJava(String.class));
                    println(e.getMessage());
                }
            }

        } catch (CafeBabePyException e) {
            // FIXME
            e.printStackTrace();

        } catch (Throwable e) {
            e.printStackTrace();

        } finally {
            this.runtime.popContext();
        }
    }

    private void printBanner() {
        String banner1 = String.format("%s %s",
                Python.APPLICATION_NAME, Python.VERSION);

        String banner2 = String.format("[%s (%s)] on Java %s",
                System.getProperty("java.vm.name"),
                System.getProperty("java.vm.specification.vendor"),
                System.getProperty("java.runtime.version"));

        String banner3 = "Type :help for help, :quit for quit";

        println(banner1);
        println(banner2);
        println(banner3);
    }

    private String readLine(BufferedReader reader) throws IOException {
        String prompt = status == Status.READLINE ? PROMPT_READLINE : PROMPT_INCOMPLETE;

        System.out.print(prompt);
        return reader.readLine();
    }

    private void println(String s) {
        System.out.println(s);
    }

    private enum Status {
        READLINE,
        INCOMPLETE,
        QUIT
    }
}
