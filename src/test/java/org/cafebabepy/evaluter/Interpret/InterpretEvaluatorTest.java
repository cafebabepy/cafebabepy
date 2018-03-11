package org.cafebabepy.evaluter.Interpret;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InterpretEvaluatorTest {

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

        @Test
        void dict() {
            PyObject result = Python.eval("{\"test1\":1, \"test2\":2}");

            LinkedHashMap<PyObject, PyObject> map = new LinkedHashMap<>();
            map.put(result.getRuntime().str("test1"), result.getRuntime().number(1));
            map.put(result.getRuntime().str("test2"), result.getRuntime().number(2));

            assertEquals(result.toJava(Map.class), map);
        }

        @Test
        void dictStar1() {
            PyObject result = Python.eval("{\"test1\":1, \"test2\":2, **{\"test3\": 3}}");

            LinkedHashMap<PyObject, PyObject> map = new LinkedHashMap<>();
            map.put(result.getRuntime().str("test1"), result.getRuntime().number(1));
            map.put(result.getRuntime().str("test2"), result.getRuntime().number(2));
            map.put(result.getRuntime().str("test3"), result.getRuntime().number(3));

            assertEquals(result.toJava(Map.class), map);
        }

        @Test
        void dictStar2() {
            PyObject result = Python.eval("{**{\"test3\": 3},\"test1\":1, \"test2\":2}");

            LinkedHashMap<PyObject, PyObject> map = new LinkedHashMap<>();
            map.put(result.getRuntime().str("test3"), result.getRuntime().number(3));
            map.put(result.getRuntime().str("test1"), result.getRuntime().number(1));
            map.put(result.getRuntime().str("test2"), result.getRuntime().number(2));

            assertEquals(result.toJava(Map.class), map);
        }

        @Test
        void dictStar3() {
            PyObject result = Python.eval("{**{\"test1\": 1}, **{\"test2\": 2}}");

            LinkedHashMap<PyObject, PyObject> map = new LinkedHashMap<>();
            map.put(result.getRuntime().str("test1"), result.getRuntime().number(1));
            map.put(result.getRuntime().str("test2"), result.getRuntime().number(2));

            assertEquals(result.toJava(Map.class), map);
        }

        @Test
        void dictStar4() {
            PyObject result = Python.eval("{**{\"test1\": 1}, **{\"test2\": 2}, **{\"test2\": 3}}");

            LinkedHashMap<PyObject, PyObject> map = new LinkedHashMap<>();
            map.put(result.getRuntime().str("test1"), result.getRuntime().number(1));
            map.put(result.getRuntime().str("test2"), result.getRuntime().number(3));

            assertEquals(result.toJava(Map.class), map);
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
            assertEquals(result.getType(), runtime.typeOrThrow("types.FunctionType"));
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

        @Test
        void callMethod() throws IOException {
            evalStdOutToResult(""
                    + "class T:\n"
                    + "  def a(self, arg):\n"
                    + "    print(arg)\n"
                    + "t = T()\n"
                    + "t.a('cafebabepy')", result -> {
                assertEquals(result, "cafebabepy" + System.lineSeparator());
            });
        }
    }
}
