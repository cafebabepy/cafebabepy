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
    class Import {
        @Test
        void importName1() {
            PyObject result = Python.eval("" +
                    "import _ast\n" +
                    "_ast"
            );

            Python runtime = result.getRuntime();

            assertEquals(result, runtime.moduleOrThrow("_ast"));
        }

        @Test
        void importNameAs() {
            PyObject result = Python.eval("" +
                    "import _ast as a\n" +
                    "a"
            );

            Python runtime = result.getRuntime();

            assertEquals(result, runtime.moduleOrThrow("_ast"));
        }

        @Test
        void importName_ast() {
            PyObject result = Python.eval("" +
                    "import this\n" +
                    "this"
            );

            Python runtime = result.getRuntime();

            assertEquals(result, runtime.moduleOrThrow("this"));
        }

        @Test
        void importFrom() {
            PyObject result = Python.eval("" +
                    "from cafebabepy_test import func1\n" +
                    "func1('test')");

            Python runtime = result.getRuntime();

            assertEquals(result, runtime.str("test"));
        }

        @Test
        void importFromStar() {
            PyObject result = Python.eval("" +
                    "from cafebabepy_test import *\n" +
                    "func1('test')");

            Python runtime = result.getRuntime();

            assertEquals(result, runtime.str("test"));
        }

        @Test
        void importFromABCDE() {
            PyObject result = Python.eval("" +
                    "from a.b.c.d import *\n" +
                    "a11");

            Python runtime = result.getRuntime();

            assertEquals(result, runtime.number(99));
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
        void stringSingleQuote() {
            PyObject result = Python.eval("'abc'");

            assertEquals(result.toJava(String.class), "abc");
        }

        @Test
        void stringDoubleQuote() {
            PyObject result = Python.eval("\"abc\"");

            assertEquals(result.toJava(String.class), "abc");
        }

        @Test
        void stringTripleQuote() {
            PyObject result = Python.eval("\"\"\"abc\ndef\"\"\"");

            assertEquals(result.toJava(String.class), "abc\ndef");
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
    class Operator {
        @Test
        void priority() {
            PyObject result = Python.eval("2 * (3 + 4)");

            Python runtime = result.getRuntime();

            assertEquals(result, runtime.number(14));
        }

        @Test
        void lessThen1() {
            PyObject result = Python.eval("1 < 2");

            Python runtime = result.getRuntime();

            assertEquals(result, runtime.True());
        }

        @Test
        void lessThen2() {
            PyObject result = Python.eval("2 < 1");

            Python runtime = result.getRuntime();

            assertEquals(result, runtime.False());
        }

        @Test
        void greaterThen1() {
            PyObject result = Python.eval("1 > 2");

            Python runtime = result.getRuntime();

            assertEquals(result, runtime.False());
        }

        @Test
        void greaterThen2() {
            PyObject result = Python.eval("2 > 1");

            Python runtime = result.getRuntime();

            assertEquals(result, runtime.True());
        }

        @Test
        void equals1() {
            PyObject result = Python.eval("1 == 1");

            Python runtime = result.getRuntime();

            assertEquals(result, runtime.True());
        }

        @Test
        void equals() {
            PyObject result = Python.eval("1 == 2");

            Python runtime = result.getRuntime();

            assertEquals(result, runtime.False());
        }

        @Test
        void lessThenEquals1() {
            PyObject result = Python.eval("1 <= 2");

            Python runtime = result.getRuntime();

            assertEquals(result, runtime.True());
        }

        @Test
        void lessThenEquals2() {
            PyObject result = Python.eval("1 <= 1");

            Python runtime = result.getRuntime();

            assertEquals(result, runtime.True());
        }

        @Test
        void lessThenEquals3() {
            PyObject result = Python.eval("2 <= 1");

            Python runtime = result.getRuntime();

            assertEquals(result, runtime.False());
        }

        @Test
        void greaterThenEquals1() {
            PyObject result = Python.eval("2 >= 1");

            Python runtime = result.getRuntime();

            assertEquals(result, runtime.True());
        }

        @Test
        void greaterThenEquals2() {
            PyObject result = Python.eval("1 >= 1");

            Python runtime = result.getRuntime();

            assertEquals(result, runtime.True());
        }

        @Test
        void greaterThenEquals3() {
            PyObject result = Python.eval("1 >= 2");

            Python runtime = result.getRuntime();

            assertEquals(result, runtime.False());
        }

        @Test
        void notEq1() {
            PyObject result = Python.eval("1 != 1");

            Python runtime = result.getRuntime();

            assertEquals(result, runtime.False());
        }

        @Test
        void notEq2() {
            PyObject result = Python.eval("1 != 2");

            Python runtime = result.getRuntime();

            assertEquals(result, runtime.True());
        }

        @Test
        void notEq3() {
            PyObject result = Python.eval("1 <> 1");

            Python runtime = result.getRuntime();

            assertEquals(result, runtime.False());
        }

        @Test
        void notEq4() {
            PyObject result = Python.eval("1 <> 2");

            Python runtime = result.getRuntime();

            assertEquals(result, runtime.True());
        }

        @Test
        void in1() {
            PyObject result = Python.eval("2 in [1, 2, 3]");

            Python runtime = result.getRuntime();

            assertEquals(result, runtime.True());
        }

        @Test
        void in2() {
            PyObject result = Python.eval("4 in [1, 2, 3]");

            Python runtime = result.getRuntime();

            assertEquals(result, runtime.False());
        }

        @Test
        void notIn1() {
            PyObject result = Python.eval("2 not in [1, 2, 3]");

            Python runtime = result.getRuntime();

            assertEquals(result, runtime.False());
        }

        @Test
        void notIn2() {
            PyObject result = Python.eval("4 not in [1, 2, 3]");

            Python runtime = result.getRuntime();

            assertEquals(result, runtime.True());
        }

        @Test
        void is1() {
            PyObject result = Python.eval("" +
                    "a = [1, 2]\n" +
                    "b = a\n" +
                    "a is b");

            Python runtime = result.getRuntime();

            assertEquals(result, runtime.True());
        }

        @Test
        void is2() {
            PyObject result = Python.eval("[1, 2] is [1, 2]");

            Python runtime = result.getRuntime();

            assertEquals(result, runtime.False());
        }

        @Test
        void isNot1() {
            PyObject result = Python.eval("" +
                    "a = [1, 2]\n" +
                    "b = a\n" +
                    "a is not b");

            Python runtime = result.getRuntime();

            assertEquals(result, runtime.False());
        }

        @Test
        void isNot2() {
            PyObject result = Python.eval("[1, 2] is not [1, 2]");

            Python runtime = result.getRuntime();

            assertEquals(result, runtime.True());
        }
    }

    @Nested
    class Slice {
        @Test
        void slice1() throws IOException {
            evalStdOutToResult(""
                            + "a = [1, 2, 3, 4]\n"
                            + "print(a[0:3])",
                    result -> {
                        assertEquals(result, ""
                                + "[1, 2, 3]" + System.lineSeparator());
                    });
        }

        @Test
        void slice2() throws IOException {
            evalStdOutToResult(""
                            + "a = [1, 2, 3, 4]\n"
                            + "print(a[1:3])",
                    result -> {
                        assertEquals(result, ""
                                + "[2, 3]" + System.lineSeparator());
                    });
        }

        @Test
        void slice3() throws IOException {
            evalStdOutToResult(""
                            + "a = [1, 2, 3, 4]\n"
                            + "print(a[0:])",
                    result -> {
                        assertEquals(result, ""
                                + "[1, 2, 3, 4]" + System.lineSeparator());
                    });
        }

        @Test
        void slice4() throws IOException {
            evalStdOutToResult(""
                            + "a = [1, 2, 3, 4]\n"
                            + "print(a[-2:4])",
                    result -> {
                        assertEquals(result, ""
                                + "[3, 4]" + System.lineSeparator());
                    });
        }

        @Test
        void slice5() throws IOException {
            evalStdOutToResult(""
                            + "a = [1, 2, 3, 4]\n"
                            + "print(a[-2:5])",
                    result -> {
                        assertEquals(result, ""
                                + "[3, 4]" + System.lineSeparator());
                    });
        }

        @Test
        void slice6() throws IOException {
            evalStdOutToResult(""
                            + "a = [1, 2, 3, 4, 5]\n"
                            + "print(a[-3:6])",
                    result -> {
                        assertEquals(result, ""
                                + "[3, 4, 5]" + System.lineSeparator());
                    });
        }

        @Test
        void slice7() throws IOException {
            evalStdOutToResult(""
                            + "a = [1, 2, 3, 4]\n"
                            + "print(a[1:-1])",
                    result -> {
                        assertEquals(result, ""
                                + "[2, 3]" + System.lineSeparator());
                    });
        }

        @Test
        void slice8() throws IOException {
            evalStdOutToResult(""
                            + "a = [1, 2, 3, 4]\n"
                            + "print(a[-4:4:2])",
                    result -> {
                        assertEquals(result, ""
                                + "[1, 3]" + System.lineSeparator());
                    });
        }

        @Test
        void slice9() throws IOException {
            evalStdOutToResult(""
                            + "a = [1, 2, 3, 4]\n"
                            + "print(a[:])",
                    result -> {
                        assertEquals(result, ""
                                + "[1, 2, 3, 4]" + System.lineSeparator());
                    });
        }

        @Test
        void sliceAssign1() throws IOException {
            evalStdOutToResult(""
                            + "a = [1, 2, 3, 4, 5]\n"
                            + "a[-3:4] = [9]\n"
                            + "print(a)",
                    result -> {
                        assertEquals(result, ""
                                + "[1, 2, 9, 5]" + System.lineSeparator());
                    });
        }

        @Test
        void sliceAssign2() throws IOException {
            evalStdOutToResult(""
                            + "a = [1, 2, 3, 4, 5]\n"
                            + "a[-3:4] = [9, 9]\n"
                            + "print(a)",
                    result -> {
                        assertEquals(result, ""
                                + "[1, 2, 9, 9, 5]" + System.lineSeparator());
                    });
        }

        @Test
        void sliceAssign3() throws IOException {
            evalStdOutToResult(""
                            + "a = [1, 2, 3, 4, 5]\n"
                            + "a[:] = [9]\n"
                            + "print(a)",
                    result -> {
                        assertEquals(result, ""
                                + "[9]" + System.lineSeparator());
                    });
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
        void forStmtStarUnpack1() throws IOException {
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
        void forStmtStarUnpack2() throws IOException {
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

        @Test
        void forStmtStarUnpack3() throws IOException {
            evalStdOutToResult(""
                            + "L = []\n"
                            + "for i, *L[0:0] in [(1, 2, 3, 4), (5, 6, 7, 8)]: pass\n"
                            + "print(L)"
                    , result -> {
                        assertEquals(result, ""
                                + "[6, 7, 8, 2, 3, 4]" + System.lineSeparator()
                        );
                    });
        }

        @Test
        void forComprehension1() throws IOException {
            evalStdOutToResult(""
                            + "a = [i for i in range(10)]\n"
                            + "print(a)"
                    , result -> {
                        assertEquals(result, ""
                                + "[0, 1, 2, 3, 4, 5, 6, 7, 8, 9]" + System.lineSeparator());
                    });
        }

        @Test
        void forComprehension2() throws IOException {
            evalStdOutToResult(""
                            + "a = (i for i in range(10))\n"
                            + "print(a)"
                    , result -> {
                        assertEquals(result, ""
                                + "(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)" + System.lineSeparator());
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
