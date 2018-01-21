package org.cafebabepy.evaluter.Interpret;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InterpretEvaluatorTest {

    @Nested
    class Literal {
        @Test
        void integer() {
            PyObject result = Python.eval("99");

            assertEquals((int) result.toJava(Integer.class), 99);
        }

        @Test
        void stringSingle() {
            PyObject result = Python.eval("'abc'");

            assertEquals(result.toJava(String.class), "abc");
        }

        @Test
        void stringDouble() {
            PyObject result = Python.eval("\"abc\"");

            assertEquals(result.toJava(String.class), "abc");
        }
    }

    @Nested
    class If {
        @Test
        void ifTrue() throws IOException {
            evalStdOutToResult(""
                    + "if 1 == 1:\n"
                    + "  print('cafe')", result -> {
                assertEquals(result, "cafe" + System.lineSeparator());
            });
        }

        @Test
        void ifFalse() throws IOException {
            evalStdOutToResult(""
                    + "if 1 != 1:\n"
                    + "  print('cafe')\n"
                    + "print('babe')", result -> {
                assertEquals(result, "babe" + System.lineSeparator());
            });
        }

        @Test
        void ifTrueElse() throws IOException {
            evalStdOutToResult(""
                    + "if 1 == 1:\n"
                    + "  print('cafe')\n"
                    + "else:\n"
                    + "  print('babe')", result -> {
                assertEquals(result, "cafe" + System.lineSeparator());
            });
        }

        @Test
        void ifFalseElse() throws IOException {
            evalStdOutToResult(""
                    + "if 1 != 1:\n"
                    + "  print('cafe')\n"
                    + "else:\n"
                    + "  print('babe')", result -> {
                assertEquals(result, "babe" + System.lineSeparator());
            });
        }
    }

    @Nested
    class Class {
        @Test
        void defineClass() {
            PyObject result = Python.eval(""
                    + "class T:\n"
                    + "  pass\n"
                    + "T");

            assertEquals(result.getName(), "T");

            Python runtime = result.getRuntime();
            assertEquals(result.getType(), runtime.typeOrThrow("builtins.type"));
        }

        @Test
        void defineMethodClass() {
            PyObject result = Python.eval(""
                    + "class T:\n"
                    + "  def a(self):\n"
                    + "    pass\n"
                    + "T.a");

            assertEquals(result.getName(), "function");

            Python runtime = result.getRuntime();
            assertEquals(result.getType(), runtime.typeOrThrow("builtins.FunctionType"));
        }

        @Test
        void defineMethodObject() {
            PyObject result = Python.eval(""
                    + "class T:\n"
                    + "  def a(self):\n"
                    + "    pass\n"
                    + "t = T()\n"
                    + "t.a");

            assertEquals(result.getName(), "method");

            Python runtime = result.getRuntime();
            assertEquals(result.getType(), runtime.typeOrThrow("types.MethodType"));
        }
    }

    private void evalStdOutToResult(String input, Consumer<String> consumer) throws IOException {
        try (
                ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
                PrintStream out = new PrintStream(byteArrayOut)) {

            PrintStream defaultOut = System.out;
            System.setOut(out);
            try {
                Python.eval(input);

                String result = new String(byteArrayOut.toByteArray(), "UTF-8");

                consumer.accept(result);

            } finally {
                System.setOut(defaultOut);
            }
        }
    }
}
