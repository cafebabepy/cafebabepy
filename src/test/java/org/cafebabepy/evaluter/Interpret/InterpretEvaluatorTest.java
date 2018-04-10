package org.cafebabepy.evaluter.Interpret;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
    class Assign {
        @Test
        void unpackAssign1() throws IOException {
            evalStdOutToResult("" +
                            "[[a], *[b], c, [d]] = [[1], [[2], [3], [4]], 5, [6]]\n" +
                            "print(a)\n" +
                            "print(b)\n" +
                            "print(c)\n" +
                            "print(d)",
                    result -> {
                        assertEquals(result, ""
                                + "1" + System.lineSeparator()
                                + "[[2], [3], [4]]" + System.lineSeparator()
                                + "5" + System.lineSeparator()
                                + "6" + System.lineSeparator()
                        );
                    });
        }
    }

    @Nested
    class Call {
        @Test
        void dict() throws IOException {
            evalStdOutToResult(""
                    + "a = {1: 2}\n"
                    + "print(a[1])", result -> {
                assertEquals(result, ""
                        + "2" + System.lineSeparator()
                );
            });
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
        void list() {
            PyObject result = Python.eval("[1, 2]");

            List<PyObject> list = new ArrayList<>();
            list.add(result.getRuntime().number(1));
            list.add(result.getRuntime().number(2));

            assertEquals(result.toJava(List.class), list);
            assertEquals(result.toJava(String.class), "[1, 2]");
        }

        @Test
        void tuple1() {
            PyObject result = Python.eval("(1, 2)");

            List<PyObject> list = new ArrayList<>();
            list.add(result.getRuntime().number(1));
            list.add(result.getRuntime().number(2));

            assertEquals(result.toJava(List.class), list);
            assertEquals(result.toJava(String.class), "(1, 2)");
        }

        @Test
        void tuple2() {
            PyObject result = Python.eval("1,");

            List<PyObject> list = new ArrayList<>();
            list.add(result.getRuntime().number(1));

            assertEquals(result.toJava(List.class), list);
            assertEquals(result.toJava(String.class), "(1,)");
        }

        @Test
        void tuple3() {
            PyObject result = Python.eval("(1,)");

            List<PyObject> list = new ArrayList<>();
            list.add(result.getRuntime().number(1));

            assertEquals(result.toJava(List.class), list);
            assertEquals(result.toJava(String.class), "(1,)");
        }

        @Test
        void dict() {
            PyObject result = Python.eval("{\"test1\":1 , \"test2\": 2}");

            LinkedHashMap<PyObject, PyObject> map = new LinkedHashMap<>();
            map.put(result.getRuntime().str("test1"), result.getRuntime().number(1));
            map.put(result.getRuntime().str("test2"), result.getRuntime().number(2));

            assertEquals(result.toJava(Map.class), map);
            assertEquals(result.toJava(String.class), "{'test1': 1, 'test2': 2}");
        }

        @Test
        void dictStar1() {
            PyObject result = Python.eval("{\"test1\": 1, \"test2\": 2, **{\"test3\": 3}}");

            LinkedHashMap<PyObject, PyObject> map = new LinkedHashMap<>();
            map.put(result.getRuntime().str("test1"), result.getRuntime().number(1));
            map.put(result.getRuntime().str("test2"), result.getRuntime().number(2));
            map.put(result.getRuntime().str("test3"), result.getRuntime().number(3));

            assertEquals(result.toJava(Map.class), map);
            assertEquals(result.toJava(String.class), "{'test1': 1, 'test2': 2, 'test3': 3}");
        }

        @Test
        void dictStar2() {
            PyObject result = Python.eval("{**{\"test3\": 3}, \"test1\": 1, \"test2\": 2}");

            LinkedHashMap<PyObject, PyObject> map = new LinkedHashMap<>();
            map.put(result.getRuntime().str("test3"), result.getRuntime().number(3));
            map.put(result.getRuntime().str("test1"), result.getRuntime().number(1));
            map.put(result.getRuntime().str("test2"), result.getRuntime().number(2));

            assertEquals(result.toJava(Map.class), map);
            assertEquals(result.toJava(String.class), "{'test3': 3, 'test1': 1, 'test2': 2}");
        }

        @Test
        void dictStar3() {
            PyObject result = Python.eval("{**{\"test1\": 1}, **{\"test2\": 2}}");

            LinkedHashMap<PyObject, PyObject> map = new LinkedHashMap<>();
            map.put(result.getRuntime().str("test1"), result.getRuntime().number(1));
            map.put(result.getRuntime().str("test2"), result.getRuntime().number(2));

            assertEquals(result.toJava(Map.class), map);
            assertEquals(result.toJava(String.class), "{'test1': 1, 'test2': 2}");
        }

        @Test
        void dictStar4() {
            PyObject result = Python.eval("{**{\"test1\": 1}, **{\"test2\": 2}, **{\"test2\": 3}}");

            LinkedHashMap<PyObject, PyObject> map = new LinkedHashMap<>();
            map.put(result.getRuntime().str("test1"), result.getRuntime().number(1));
            map.put(result.getRuntime().str("test2"), result.getRuntime().number(3));

            assertEquals(result.toJava(Map.class), map);
            assertEquals(result.toJava(String.class), "{'test1': 1, 'test2': 3}");
        }
    }

    @Nested
    class For {
        @Test
        void forStmt() throws IOException {
            evalStdOutToResult(""
                    + "for x in range(5):\n"
                    + "  print(x)", result -> {
                assertEquals(result, ""
                        + "0" + System.lineSeparator()
                        + "1" + System.lineSeparator()
                        + "2" + System.lineSeparator()
                        + "3" + System.lineSeparator()
                        + "4" + System.lineSeparator()
                );
            });
        }

        @Test
        void forStmtUnpack1() throws IOException {
            evalStdOutToResult(""
                            + "for x, y in [(1, 2), (3, 4)]:\n"
                            + "  print(x)\n"
                            + "  print(y)"
                    , result -> {
                        assertEquals(result, ""
                                + "1" + System.lineSeparator()
                                + "2" + System.lineSeparator()
                                + "3" + System.lineSeparator()
                                + "4" + System.lineSeparator()
                        );
                    });
        }

        @Test
        void forStmtIndex() throws IOException {
            PyObject result = Python.eval(""
                    + "d = {}\n"
                    + "for i, *j, d[tuple(j)] in [(1, 2, 3, 4), (5, 6, 7, 8)]:\n"
                    + "  pass\n"
                    + "d");

            Python runtime = result.getRuntime();

            LinkedHashMap<PyObject, PyObject> map = new LinkedHashMap<>();
            map.put(runtime.tuple(runtime.number(2), runtime.number(3)), runtime.number(4));
            map.put(runtime.tuple(runtime.number(6), runtime.number(7)), runtime.number(8));

            assertEquals(result.toJava(Map.class), map);
        }

        @Test
        void forStmtStarUnpack() throws IOException {
            evalStdOutToResult(""
                            + "for x, *y in [(1, 2, 3), (4, 5, 6)]:\n"
                            + "  print(x)\n"
                            + "  print(y)"
                    , result -> {
                        assertEquals(result, ""
                                + "1" + System.lineSeparator()
                                + "[2, 3]" + System.lineSeparator()
                                + "4" + System.lineSeparator()
                                + "[5, 6]" + System.lineSeparator()
                        );
                    });
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
