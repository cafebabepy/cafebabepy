package org.cafebabepy.evaluter.Interpret;

import org.cafebabepy.runtime.PyObject;
import org.cafebabepy.runtime.Python;
import org.cafebabepy.runtime.RaiseException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class InterpretEvaluatorTest {

    private void evalStdOutToResult(String input, Consumer<String> consumer) throws IOException {
        System.out.println("========================= Test code start =========================");
        System.out.print(input);
        System.out.println();
        System.out.println("========================= Test code end   =========================");
        System.out.println();
        System.out.println();

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
    class Try {
        @Test
        void tryExcept() {
            PyObject result = Python.eval("" +
                    "try:\n" +
                    "  raise Exception('arg1', 'arg2')\n" +
                    "except BaseException as e:\n" +
                    "  e"
            );

            Python runtime = result.getRuntime();

            assertTrue(runtime.isInstance(result, "builtins.Exception"));
            //assertEquals(runtime.getattr(result, "args"), runtime.tuple(runtime.str("arg1"), runtime.str("arg2")));
        }

        @Test
        void tryElse() throws IOException {
            evalStdOutToResult("" +
                            "try:\n" +
                            "  print(1)\n" +
                            "except Exception:\n" +
                            "  print(99)\n" +
                            "else:\n" +
                            "  print(2)",
                    result -> {
                        assertEquals(result, ""
                                + "1" + System.lineSeparator()
                                + "2" + System.lineSeparator()
                        );
                    });
        }

        @Test
        void tryElseFinally() throws IOException {
            evalStdOutToResult("" +
                            "try:\n" +
                            "  print(1)\n" +
                            "except Exception:\n" +
                            "  print(99)\n" +
                            "else:\n" +
                            "  print(2)\n" +
                            "finally:\n" +
                            "  print(3)",
                    result -> {
                        assertEquals(result, ""
                                + "1" + System.lineSeparator()
                                + "2" + System.lineSeparator()
                                + "3" + System.lineSeparator()
                        );
                    });
        }


        @Test
        void tryFinally() throws IOException {
            evalStdOutToResult("" +
                            "try:\n" +
                            "  raise Exception()\n" +
                            "except Exception:\n" +
                            "  print(1)\n" +
                            "else:\n" +
                            "  print(99)\n" +
                            "finally:\n" +
                            "  print(2)",
                    result -> {
                        assertEquals(result, ""
                                + "1" + System.lineSeparator()
                                + "2" + System.lineSeparator()
                        );
                    });
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
        void importName_this() {
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

        @Test
        void inmportInit() throws IOException {
            evalStdOutToResult(""
                            + "import init\n"
                            + "init.a()"
                    , result -> {
                        assertEquals(result, ""
                                + "__init__" + System.lineSeparator()
                                + "define a" + System.lineSeparator()
                        );
                    });
        }

        @Test
        void inmportModule() throws IOException {
            evalStdOutToResult(""
                            + "import a1\n"
                            + "a1.a()"
                    , result -> {
                        assertEquals(result, ""
                                + "define a" + System.lineSeparator()
                        );
                    });
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

            assertEquals((int) result.toJava(int.class), 99);
        }

        @Test
        void integer16() {
            PyObject result = Python.eval("0x99");

            assertEquals((int) result.toJava(int.class), 0x99);
        }

        @Test
        void float_() {
            PyObject result = Python.eval("10.999");

            assertEquals((float) result.toJava(float.class), 10.999f);
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
        void fstring() {
            PyObject result = Python.eval(""
                    + "abc = 'test'\n"
                    + "f'{abc}'\n");

            assertEquals(result.toJava(String.class), "test");
        }

        @Test
        void fstringSpace() {
            PyObject result = Python.eval(""
                    + "abc = 'test'\n"
                    + "f'{ abc }'\n");

            assertEquals(result.toJava(String.class), "test");
        }

        @Test
        void fstringStartEndString() {
            PyObject result = Python.eval(""
                    + "abc = 'test'\n"
                    + "f'yyy{ abc }zzz'\n");

            assertEquals(result.toJava(String.class), "yyytestzzz");
        }

        @Test
        void fstringConversionR() {
            PyObject result = Python.eval(""
                    + "abc = 'test'\n"
                    + "f'{ abc !r}'\n");

            assertEquals(result.toJava(String.class), "test");
        }

        @Test
        void fstringConversionS() {
            PyObject result = Python.eval(""
                    + "abc = 'test'\n"
                    + "f'{ abc !s}'\n");

            assertEquals(result.toJava(String.class), "test");
        }

        @Test
        void fstringDouble() {
            PyObject result = Python.eval(""
                    + "abc = 'test'\n"
                    + "xyz = 10\n"
                    + "f'{abc:<{xyz}}'\n");

            assertEquals(result.toJava(String.class), "test");
        }

        @Test
        void fstringConversionA() {
            PyObject result = Python.eval(""
                    + "abc = 'test'\n"
                    + "f'{ abc !a}'\n");

            assertEquals(result.toJava(String.class), "test");
        }

        @Test
        void fstringFormatSpec() {
            PyObject result = Python.eval(""
                    + "abc = 'test'\n"
                    + "f'{ abc !s:>10 }'\n");

            assertEquals(result.toJava(String.class), "test");
        }

        @Test
        void fstringException1() {
            try {
                Python.eval(""
                        + "abc = 'test'\n"
                        + "f'{abc!s :>}'\n");

            } catch (RaiseException e) {
                PyObject exception = e.getException();
                Python runtime = exception.getRuntime();

                assertEquals(exception.getType(), runtime.typeOrThrow("SyntaxError"));
                assertEquals(runtime.getattr(exception, "args"),
                        runtime.tuple(new PyObject[]{runtime.str("f-string: expecting '}'")}));
            }
        }

        @Test
        void fstringException2() {
            try {
                PyObject result = Python.eval(""
                        + "abc = 'test'\n"
                        + "f'{ abc !:>10 }'\n");

            } catch (RaiseException e) {
                PyObject exception = e.getException();
                Python runtime = exception.getRuntime();

                assertEquals(exception.getType(), runtime.typeOrThrow("SyntaxError"));
                assertEquals(runtime.getattr(exception, "args"),
                        runtime.tuple(new PyObject[]{runtime.str(
                                "f-string: invalid conversion character: expected 's', 'r', or 'a'")}));
            }
        }

        @Test
        void fstringException3() {
            try {
                PyObject result = Python.eval(""
                        + "abc = 'test'\n"
                        + "f'{ abc !}'\n");

            } catch (RaiseException e) {
                PyObject exception = e.getException();
                Python runtime = exception.getRuntime();

                assertEquals(exception.getType(), runtime.typeOrThrow("SyntaxError"));
                assertEquals(runtime.getattr(exception, "args"),
                        runtime.tuple(new PyObject[]{runtime.str(
                                "f-string: invalid conversion character: expected 's', 'r', or 'a'")}));
            }
        }

        @Test
        void bytesASCIIOnly() throws UnsupportedEncodingException {
            PyObject result = Python.eval(""
                    + "abcd = b'abcd'\n"
                    + "abcd\n");
            byte[] actual = "abcd".getBytes("UTF-8");

            assertEquals(result, result.getRuntime().bytes(actual));
        }

        @Test
        void bytesHiragana() throws UnsupportedEncodingException {
            PyObject result = Python.eval(""
                    + "abcd = b'a\\xE3\\x81\\x82b\\xE3\\x81\\x84c\\xE3\\x81\\x86d\\\\\\g\\h'\n"
                    + "abcd\n");
            byte[] actual = "aあbいcうd\\\\g\\h".getBytes("UTF-8");

            assertEquals(result, result.getRuntime().bytes(actual));
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
        void tuple4() {
            PyObject result = Python.eval("*range(10),");

            List<PyObject> list = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                list.add(result.getRuntime().number(i));
            }

            assertEquals(result.toJava(List.class), list);
            assertEquals(result.toJava(String.class), "(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)");
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
        void dictEmpty() {
            PyObject result = Python.eval("{}");

            LinkedHashMap<PyObject, PyObject> map = new LinkedHashMap<>();

            assertEquals(result.toJava(Map.class), map);
            assertEquals(result.toJava(String.class), "{}");
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
        void forBreak() throws IOException {
            evalStdOutToResult(""
                    + "for x in range(5):\n"
                    + "  if x == 3:\n"
                    + "    break\n"
                    + "  print(x)", result -> {
                assertEquals(result, ""
                        + "0" + System.lineSeparator()
                        + "1" + System.lineSeparator()
                        + "2" + System.lineSeparator()
                );
            });
        }

        @Test
        void forContinue() throws IOException {
            evalStdOutToResult(""
                    + "for i in range(5):\n"
                    + "  if i == 2:\n"
                    + "    continue\n"
                    + "  print(i)", result -> {
                assertEquals(result, ""
                        + "0" + System.lineSeparator()
                        + "1" + System.lineSeparator()
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
        void forStmt2() throws IOException {
            evalStdOutToResult(""
                            + "list1 = [ [1,5,7], [10,3, 4], [6, 8, 5]]\n"

                            + "for list1_item in list1:\n"
                            + "  for item in list1_item:\n"
                            + "    print(item)\n"
                            + "    if(item >= 10):\n"
                            + "      print('over 10')\n"
                            + "      break\n"
                            + "  else:\n"
                            + "    continue\n"
                            + "  break",
                    result -> {
                        assertEquals(result, ""
                                + "1" + System.lineSeparator()
                                + "5" + System.lineSeparator()
                                + "7" + System.lineSeparator()
                                + "10" + System.lineSeparator()
                                + "over 10" + System.lineSeparator()
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
    class While {
        @Test
        void whileStmt() throws IOException {
            evalStdOutToResult(""
                            + "a = 0\n"
                            + "while a < 5:\n"
                            + "  print(a)\n"
                            + "  a = a + 1"
                    , result -> {
                        assertEquals(result, ""
                                + "0" + System.lineSeparator()
                                + "1" + System.lineSeparator()
                                + "2" + System.lineSeparator()
                                + "3" + System.lineSeparator()
                                + "4" + System.lineSeparator());
                    });
        }

        @Test
        void whileStmtBreak() throws IOException {
            evalStdOutToResult(""
                            + "while True:\n"
                            + "  print(1)\n"
                            + "  break\n"
                            + "else:\n"
                            + "  print(99)"
                    , result -> {
                        assertEquals(result, ""
                                + "1" + System.lineSeparator());
                    });
        }

        @Test
        void whileStmtContinue() throws IOException {
            evalStdOutToResult(""
                            + "a = 0\n"
                            + "while a < 5:\n"
                            + "  a = a + 1\n"
                            + "  if a == 3:\n"
                            + "    continue\n"
                            + "  print(a)"
                    , result -> {
                        assertEquals(result, ""
                                + "1" + System.lineSeparator()
                                + "2" + System.lineSeparator()
                                + "4" + System.lineSeparator()
                                + "5" + System.lineSeparator());
                    });
        }

        @Test
        void whileStmtElse1() throws IOException {
            evalStdOutToResult(""
                            + "a = 0\n"
                            + "while a < 5:\n"
                            + "  print(a)\n"
                            + "  a = a + 1\n"
                            + "else:\n"
                            + "  print(99)\n"
                    , result -> {
                        assertEquals(result, ""
                                + "0" + System.lineSeparator()
                                + "1" + System.lineSeparator()
                                + "2" + System.lineSeparator()
                                + "3" + System.lineSeparator()
                                + "4" + System.lineSeparator()
                                + "99" + System.lineSeparator());
                    });
        }

        @Test
        void whileStmtElse2() throws IOException {
            evalStdOutToResult(""
                            + "a = 0\n"
                            + "try:\n"
                            + "  while a < 5:\n"
                            + "    print(a)\n"
                            + "    if a == 2:\n"
                            + "      raise Exception()\n"
                            + "    a = a + 1\n"
                            + "  else:\n"
                            + "    print(99)\n"
                            + "except Exception:\n"
                            + "  print(9999)"
                    , result -> {
                        assertEquals(result, ""
                                + "0" + System.lineSeparator()
                                + "1" + System.lineSeparator()
                                + "2" + System.lineSeparator()
                                + "9999" + System.lineSeparator());
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
        void defineOverrideClass() throws IOException {
            evalStdOutToResult(""
                    + "class T(object):\n"
                    + "  def a(self):\n"
                    + "    print('cafebabepy')\n"
                    + "t = T()\n"
                    + "t.a()", result -> {
                assertEquals(result, "cafebabepy" + System.lineSeparator());
            });
        }

        @Test
        void defineMethodClass() {
            PyObject result = Python.eval(""
                    + "class T:\n"
                    + "  def abc(self):\n"
                    + "    pass\n"
                    + "T.abc");

            assertEquals(result.getName(), "abc");

            Python runtime = result.getRuntime();
            assertEquals(result.getType(), runtime.typeOrThrow("function", false));
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
            assertEquals(result.getType(), runtime.typeOrThrow("method", false));
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

    @Nested
    class Function {
        @Test
        void lambda() {
            PyObject result = Python.eval(""
                    + "a = lambda x: x + 1\n"
                    + "a(99)");

            Python runtime = result.getRuntime();
            assertEquals(result, runtime.number(100));
        }

        @Test
        void lambdaNoArgument() throws IOException {
            PyObject result = Python.eval(""
                    + "a = lambda: 99 + 1\n"
                    + "a()");

            Python runtime = result.getRuntime();
            assertEquals(result, runtime.number(100));
        }

        @Test
        void functionDefaultArguments0() throws IOException {
            evalStdOutToResult(""
                    + "def a(c = 9, b = 99, d = 999):\n"
                    + "  print(c)\n"
                    + "  print(b)\n"
                    + "  print(d)\n"
                    + "a()", result -> {
                assertEquals(result, "9" + System.lineSeparator() + "99" + System.lineSeparator() + "999" + System.lineSeparator());
            });
        }

        @Test
        void functionDefaultArguments1() throws IOException {
            evalStdOutToResult(""
                    + "def a(c = 9, b = 99, d = 999):\n"
                    + "  print(c)\n"
                    + "  print(b)\n"
                    + "  print(d)\n"
                    + "a(1)", result -> {
                assertEquals(result, "1" + System.lineSeparator() + "99" + System.lineSeparator() + "999" + System.lineSeparator());
            });
        }

        @Test
        void functionDefaultArguments2() throws IOException {
            evalStdOutToResult(""
                    + "def a(c = 9, b = 99, d = 999):\n"
                    + "  print(c)\n"
                    + "  print(b)\n"
                    + "  print(d)\n"
                    + "a(1, 11)", result -> {
                assertEquals(result, "1" + System.lineSeparator() + "11" + System.lineSeparator() + "999" + System.lineSeparator());
            });
        }

        @Test
        void functionDefaultArguments3() throws IOException {
            evalStdOutToResult(""
                    + "def a(c = 9, b = 99, d = 999):\n"
                    + "  print(c)\n"
                    + "  print(b)\n"
                    + "  print(d)\n"
                    + "a(1, 11, 111)", result -> {
                assertEquals(result, "1" + System.lineSeparator() + "11" + System.lineSeparator() + "111" + System.lineSeparator());
            });
        }

        @Test
        void functionDefaultArguments4() throws IOException {
            evalStdOutToResult(""
                    + "def a(c, b = 99, d = 999):\n"
                    + "  print(c)\n"
                    + "  print(b)\n"
                    + "  print(d)\n"
                    + "a(1)", result -> {
                assertEquals(result, "1" + System.lineSeparator() + "99" + System.lineSeparator() + "999" + System.lineSeparator());
            });
        }

        @Test
        void functionDefaultArguments5() throws IOException {
            evalStdOutToResult(""
                    + "def a(c, b, d = 999):\n"
                    + "  print(c)\n"
                    + "  print(b)\n"
                    + "  print(d)\n"
                    + "a(1, 99)", result -> {
                assertEquals(result, "1" + System.lineSeparator() + "99" + System.lineSeparator() + "999" + System.lineSeparator());
            });
        }

        @Test
        void functionVariableArguments1() throws IOException {
            evalStdOutToResult(""
                    + "def a(*a):\n"
                    + "  print(a)\n"
                    + "a()", result -> {
                assertEquals(result, "()" + System.lineSeparator());
            });
        }

        @Test
        void functionVariableArguments2() throws IOException {
            evalStdOutToResult(""
                    + "def a(*a):\n"
                    + "  print(a)\n"
                    + "a(1, 2, 3)", result -> {
                assertEquals(result, "(1, 2, 3)" + System.lineSeparator());
            });
        }

        @Test
        void functionVariableArguments3() throws IOException {
            evalStdOutToResult(""
                    + "def a(a, *b):\n"
                    + "  print(a)\n"
                    + "  print(b)\n"
                    + "a(1, 2, 3)", result -> {
                assertEquals(result, "1" + System.lineSeparator() + "(2, 3)" + System.lineSeparator());
            });
        }

        @Test
        void functionVariableArguments4() throws IOException {
            evalStdOutToResult(""
                    + "def a(a, *b):\n"
                    + "  print(a)\n"
                    + "  print(b)\n"
                    + "a(1)", result -> {
                assertEquals(result, "1" + System.lineSeparator() + "()" + System.lineSeparator());
            });
        }

        @Test
        void functionKeywordOnlyArguments1() throws IOException {
            evalStdOutToResult(""
                    + "def a(*b, a):\n"
                    + "  print(a)\n"
                    + "  print(b)\n"
                    + "a(2, 3, a = 1)", result -> {
                assertEquals(result, "1" + System.lineSeparator() + "(2, 3)" + System.lineSeparator());
            });
        }

        @Test
        void functionKeywordOnlyArguments2() throws IOException {
            evalStdOutToResult(""
                    + "def a(*b, a = 99):\n"
                    + "  print(a)\n"
                    + "  print(b)\n"
                    + "a(1, 2, 3)", result -> {
                assertEquals(result, "99" + System.lineSeparator() + "(1, 2, 3)" + System.lineSeparator());
            });
        }

        @Test
        void functionKeywordOnlyArguments3() throws IOException {
            evalStdOutToResult(""
                    + "def a(b, *, c):\n"
                    + "  print(b)\n"
                    + "  print(c)\n"
                    + "a(1, c = 3)", result -> {
                assertEquals(result, "1" + System.lineSeparator() + "3" + System.lineSeparator());
            });
        }

        @Test
        void functionDefaultArgumentsScope() throws IOException {
            evalStdOutToResult(""
                    + "def a(b = []):\n"
                    + "  b.append(1)\n"
                    + "  return b\n"
                    + "print(a())\n"
                    + "print(a())", result -> {
                assertEquals(result, "[1]" + System.lineSeparator() + "[1, 1]" + System.lineSeparator());
            });
        }

        @Test
        void functionKwarg1() throws IOException {
            evalStdOutToResult(""
                    + "def a(b, c):\n"
                    + "  print(b)\n"
                    + "  print(c)\n"
                    + "a(b = 1, c = 2)", result -> {
                assertEquals(result, "1" + System.lineSeparator() + "2" + System.lineSeparator());
            });
        }

        @Test
        void functionKwarg2() throws IOException {
            evalStdOutToResult(""
                    + "def a(b, c):\n"
                    + "  print(b)\n"
                    + "  print(c)\n"
                    + "a(1, c = 2)", result -> {
                assertEquals(result, "1" + System.lineSeparator() + "2" + System.lineSeparator());
            });
        }

        @Test
        void functionKwarg3() throws IOException {
            evalStdOutToResult(""
                    + "def a(b, c):\n"
                    + "  print(b)\n"
                    + "  print(c)\n"
                    + "a(c = 2, b = 1)", result -> {
                assertEquals(result, "1" + System.lineSeparator() + "2" + System.lineSeparator());
            });
        }

        @Test
        void functionKwargs1() {
            PyObject result = Python.eval(""
                    + "def a(**kwargs):\n"
                    + "  return kwargs\n"
                    + "a(a = 1, b = 2)");

            Python runtime = result.getRuntime();

            LinkedHashMap<PyObject, PyObject> actual = new LinkedHashMap<>();
            actual.put(runtime.str("a"), runtime.number(1));
            actual.put(runtime.str("b"), runtime.number(2));

            assertEquals(result, runtime.dict(actual));
        }

        @Test
        void functionKwargs2() throws IOException {
            evalStdOutToResult(""
                    + "def a(b, **kwargs):\n"
                    + "  print(b)\n"
                    + "  print(kwargs)\n"
                    + "a(b = 1, c = 2)", result -> {
                assertEquals(result, "1" + System.lineSeparator() + "{'c': 2}" + System.lineSeparator());
            });
        }

        @Test
        void functionKwargs3() throws IOException {
            evalStdOutToResult(""
                    + "def a(*b, c, **kwargs):\n"
                    + "  print(b)\n"
                    + "  print(c)\n"
                    + "  print(kwargs)\n"
                    + "a(1, 2, c = 3, d = 99, e = 999)", result -> {
                assertEquals(result, "(1, 2)" + System.lineSeparator() + "3" + System.lineSeparator() + "{'d': 99, 'e': 999}" + System.lineSeparator());
            });
        }

        @Test
        void functionKwargs4() throws IOException {
            evalStdOutToResult(""
                    + "def a(b, *c, **kwargs):\n"
                    + "  print(b)\n"
                    + "  print(c)\n"
                    + "  print(kwargs)\n"
                    + "a(1, 2, 3, 4, d = 99, e = 999)", result -> {
                assertEquals(result, "1" + System.lineSeparator() + "(2, 3, 4)" + System.lineSeparator() + "{'d': 99, 'e': 999}" + System.lineSeparator());
            });
        }

        @Test
        void functionKwargs5() throws IOException {
            evalStdOutToResult(""
                    + "def a(b, *c, d, **kwargs):\n"
                    + "  print(b)\n"
                    + "  print(c)\n"
                    + "  print(d)\n"
                    + "  print(kwargs)\n"
                    + "a(1, 2, 3, 4, d = 99, e = 999)", result -> {
                assertEquals(result, "1" + System.lineSeparator() + "(2, 3, 4)" + System.lineSeparator() + "99" + System.lineSeparator() + "{'e': 999}" + System.lineSeparator());
            });
        }

        @Test
        void functionCallStarred1() throws IOException {
            evalStdOutToResult(""
                    + "def a(b, *c, d, **kwargs):\n"
                    + "  print(b)\n"
                    + "  print(c)\n"
                    + "  print(d)\n"
                    + "  print(kwargs)\n"
                    + "a(1, *(2, 3, 4), d = 99, e = 999)", result -> {
                assertEquals(result, "1" + System.lineSeparator() + "(2, 3, 4)" + System.lineSeparator() + "99" + System.lineSeparator() + "{'e': 999}" + System.lineSeparator());
            });
        }

        @Test
        void functionCallStarred2() throws IOException {
            evalStdOutToResult(""
                    + "def a(*b):\n"
                    + "  print(b)\n"
                    + "a(*(1, 2), *(3, 4))", result -> {
                assertEquals(result, "(1, 2, 3, 4)" + System.lineSeparator());
            });
        }

        @Test
        void functionCallStarred3() throws IOException {
            evalStdOutToResult(""
                    + "def a(b, *c):\n"
                    + "  print(b)\n"
                    + "  print(c)\n"
                    + "a(*(1, 2), *(3, 4))", result -> {
                assertEquals(result, "1" + System.lineSeparator() + "(2, 3, 4)" + System.lineSeparator());
            });
        }

        @Test
        void functionCallStarred4() throws IOException {
            evalStdOutToResult(""
                    + "def a(*b, c):\n"
                    + "  print(b)\n"
                    + "  print(c)\n"
                    + "a(*(1, 2), *(3, 4), c = 99)", result -> {
                assertEquals(result, "(1, 2, 3, 4)" + System.lineSeparator() + "99" + System.lineSeparator());
            });
        }

        @Test
        void functionCallNotStarred() throws IOException {
            evalStdOutToResult(""
                    + "def a(b, *c, d, **kwargs):\n"
                    + "  print(b)\n"
                    + "  print(c)\n"
                    + "  print(d)\n"
                    + "  print(kwargs)\n"
                    + "a(1, (2, 3, 4), d = 99, e = 999)", result -> {
                assertEquals(result, "1" + System.lineSeparator() + "((2, 3, 4),)" + System.lineSeparator() + "99" + System.lineSeparator() + "{'e': 999}" + System.lineSeparator());
            });
        }

        @Test
        void functionCallDoubleStarred1() throws IOException {
            evalStdOutToResult(""
                    + "def a(b, *c, d, **kwargs):\n"
                    + "  print(b)\n"
                    + "  print(c)\n"
                    + "  print(d)\n"
                    + "  print(kwargs)\n"
                    + "a(**{'e': 999}, b = 1, d = 99)", result -> {
                assertEquals(result, "1" + System.lineSeparator() + "()" + System.lineSeparator() + "99" + System.lineSeparator() + "{'e': 999}" + System.lineSeparator());
            });
        }

        @Test
        void functionCallDoubleStarred2() throws IOException {
            evalStdOutToResult(""
                    + "def a(**kwargs):\n"
                    + "  print(kwargs)\n"
                    + "a(**{'1': 2}, **{'3': 4})", result -> {
                assertEquals(result, "{'1': 2, '3': 4}" + System.lineSeparator());
            });
        }

        @Test
        void functionCallDoubleStarred3() throws IOException {
            evalStdOutToResult(""
                    + "def a(b, **kwargs):\n"
                    + "  print(b)\n"
                    + "  print(kwargs)\n"
                    + "a(**{'1': 2}, **{'3': 4}, b = 99)", result -> {
                assertEquals(result, "99" + System.lineSeparator() + "{'1': 2, '3': 4}" + System.lineSeparator());
            });
        }

        @Test
        void functionCallDoubleStarred4() throws IOException {
            evalStdOutToResult(""
                    + "def a(b, **kwargs):\n"
                    + "  print(b)\n"
                    + "  print(kwargs)\n"
                    + "a(**{'1': 2},b = 99, **{'3': 4})", result -> {
                assertEquals(result, "99" + System.lineSeparator() + "{'1': 2, '3': 4}" + System.lineSeparator());
            });
        }

        @Test
        void functionCallDoubleStarred5() throws IOException {
            evalStdOutToResult(""
                    + "def a(b, **kwargs):\n"
                    + "  print(b)\n"
                    + "  print(kwargs)\n"
                    + "a(99, **{'1': 2}, **{'3': 4})", result -> {
                assertEquals(result, "99" + System.lineSeparator() + "{'1': 2, '3': 4}" + System.lineSeparator());
            });
        }

        @Test
        void functionCallDouble() throws IOException {
            evalStdOutToResult(""
                    + "def a(b, **kwargs):\n"
                    + "  print(b)\n"
                    + "  print(kwargs)\n"
                    + "a(99, **{'1': 2}, **{'3': 4})", result -> {
                assertEquals(result, "99" + System.lineSeparator() + "{'1': 2, '3': 4}" + System.lineSeparator());
            });
        }

        @Test
        void functionArgumentFor1() throws IOException {
            evalStdOutToResult(""
                    + "def a(b):\n"
                    + "  for x in b:\n"
                    + "    print(x)\n"
                    + "a(x for x in range(5))", result -> {
                assertEquals(result, ""
                        + "0" + System.lineSeparator()
                        + "1" + System.lineSeparator()
                        + "2" + System.lineSeparator()
                        + "3" + System.lineSeparator()
                        + "4" + System.lineSeparator());
            });
        }

        @Test
        void functionArgumentFor2() throws IOException {
            evalStdOutToResult(""
                    + "b = 1\n"
                    + "def a(b):\n"
                    + "  for x in b:\n"
                    + "    print(x)\n"
                    + "a(b for x in range(5))", result -> {
                assertEquals(result, ""
                        + "1" + System.lineSeparator()
                        + "1" + System.lineSeparator()
                        + "1" + System.lineSeparator()
                        + "1" + System.lineSeparator()
                        + "1" + System.lineSeparator());
            });
        }

        @Test
        void functionArgumentForException() {
            try {
                Python.eval(""
                        + "def a(b, c):\n"
                        + "  for x in b:\n"
                        + "    print(x)\n"
                        + "    print(c)\n"
                        + "a(x for x in range(5), 10)");

            } catch (RaiseException e) {
                PyObject exception = e.getException();
                Python runtime = exception.getRuntime();

                assertEquals(exception.getType(), runtime.typeOrThrow("SyntaxError"));
                assertEquals(runtime.getattr(exception, "args"),
                        runtime.tuple(new PyObject[]{runtime.str("Generator expression must be parenthesized")}));
            }
        }

        @Test
        void emptyYield() {
            PyObject result = Python.eval(""
                    + "def a(): yield\n"
                    + "a()");

            Python runtime = result.getRuntime();
            assertEquals(result.getType(), runtime.typeOrThrow("generator"));
        }

        @Test
        void generator1() throws IOException {
            evalStdOutToResult(""
                    + "def a(x):\n"
                    + "  yield 1\n"
                    + "  yield x\n"
                    + "  return 10\n"

                    + "for x in a(5):\n"
                    + "  print(x)\n"

                    + "for x in a(99):\n"
                    + "  print(x)", result -> {
                assertEquals(result, ""
                        + "1" + System.lineSeparator()
                        + "5" + System.lineSeparator()
                        + "1" + System.lineSeparator()
                        + "99" + System.lineSeparator());
            });
        }

        @Test
        void generator2() throws IOException {
            evalStdOutToResult(""
                    + "def a(x):\n"
                    + "  yield 1\n"
                    + "  return 10\n"

                    + "x = a(10)\n"
                    + "print(x.__next__())\n"
                    + "try:\n"
                    + "  x.__next__()\n"
                    + "except Exception as e:\n"
                    + "  print(e.value)", result -> {
                assertEquals(result, ""
                        + "1" + System.lineSeparator()
                        + "10" + System.lineSeparator());
            });
        }

        @Test
        void generator3() throws IOException {
            evalStdOutToResult(""
                    + "def generate_nums():\n"
                    + "  num = 0\n"
                    + "  while True:\n"
                    + "    yield num\n"
                    + "    num = num + 1\n"

                    + "nums = generate_nums()\n"

                    + "for x in nums:\n"
                    + "  print(x)\n"
                    + "  if x > 9:\n"
                    + "     break", result -> {
                assertEquals(result, ""
                        + "0" + System.lineSeparator()
                        + "1" + System.lineSeparator()
                        + "2" + System.lineSeparator()
                        + "3" + System.lineSeparator()
                        + "4" + System.lineSeparator()
                        + "5" + System.lineSeparator()
                        + "6" + System.lineSeparator()
                        + "7" + System.lineSeparator()
                        + "8" + System.lineSeparator()
                        + "9" + System.lineSeparator()
                        + "10" + System.lineSeparator());
            });
        }
    }

    @Nested
    class With {
        @Test
        void with() throws IOException {
            evalStdOutToResult(""
                    + "class A:\n"
                    + "  def __enter__(self):\n"
                    + "    print('enter')\n"
                    + "    return self\n"
                    + "  def __exit__(self, ex_type, ex_value, trace):\n"
                    + "    print('exit')\n"

                    + "with A() as a:\n"
                    + "  print('with')", result -> {
                assertEquals(result, ""
                        + "enter" + System.lineSeparator()
                        + "with" + System.lineSeparator()
                        + "exit" + System.lineSeparator());
            });
        }

        @Test
        void withRaise() throws IOException {
            evalStdOutToResult(""
                    + "class A:\n"
                    + "  def __init__(self):\n"
                    + "    self.a = 1\n"
                    + "  def __enter__(self):\n"
                    + "    print('enterA')\n"
                    + "    return self\n"
                    + "  def __exit__(self, ex_type, ex_value, trace):\n"
                    + "    print('exitA')\n"

                    + "class B:\n"
                    + "  def __init__(self):\n"
                    + "    self.b = 2\n"
                    + "  def __enter__(self):\n"
                    + "    print('enterB')\n"
                    + "    return self\n"
                    + "  def __exit__(self, ex_type, ex_value, trace):\n"
                    + "    print('exitB')\n"

                    + "try:\n"
                    + "  with A() as a, B() as b:\n"
                    + "    print('with')\n"
                    + "    print(a.a)\n"
                    + "    print(b.b)\n"
                    + "    raise Exception(1, 2, 3)\n"
                    + "  print('fail')\n"
                    + "except Exception as e:\n"
                    + "  print('exception')\n"
                    + "  print(e.args)", result -> {
                assertEquals(result, ""
                        + "enterA" + System.lineSeparator()
                        + "enterB" + System.lineSeparator()
                        + "with" + System.lineSeparator()
                        + "1" + System.lineSeparator()
                        + "2" + System.lineSeparator()
                        + "exitB" + System.lineSeparator()
                        + "exitA" + System.lineSeparator()
                        + "exception" + System.lineSeparator()
                        + "(1, 2, 3)" + System.lineSeparator());
            });
        }

        @Test
        void withExit() throws IOException {
            evalStdOutToResult(""
                    + "class A:\n"
                    + "  def __enter__(self):\n"
                    + "    print('enter')\n"
                    + "    return self\n"
                    + "  def __exit__(self, ex_type, ex_value, trace):\n"
                    + "    print(ex_type)\n"
                    + "    print(ex_value)\n"

                    + "try:\n"
                    + "  with A():\n"
                    + "    print('with')\n"
                    + "    raise Exception(1, 2, 3)\n"
                    + "  print('fail')\n"
                    + "except Exception as e:\n"
                    + "  print('exception')", result -> {
                assertEquals(result, ""
                        + "enter" + System.lineSeparator()
                        + "with" + System.lineSeparator()
                        + "<class 'Exception'>" + System.lineSeparator()
                        + "(1, 2, 3)" + System.lineSeparator()
                        + "exception" + System.lineSeparator());
            });
        }

        @Test
        void withNotFoundEnter() {
            try {
                Python.eval(""
                        + "class A:\n"
                        + "  def __enter__(self):\n"
                        + "    pass\n"

                        + "with A() as a:\n"
                        + "  print('with')");

                fail("with error");

            } catch (RaiseException e) {
                Python runtime = e.getException().getRuntime();
                assertEquals(e.getException().getType(), runtime.typeOrThrow("AttributeError"));
            }
        }

        @Test
        void withNotFoundExit() {
            try {
                Python.eval(""
                        + "class A:\n"
                        + "  def __exit__(self, ex_type, ex_value, trace):\n"
                        + "    pass\n"

                        + "with A() as a:\n"
                        + "  print('with')");

                fail("with error");

            } catch (RaiseException e) {
                Python runtime = e.getException().getRuntime();
                assertEquals(e.getException().getType(), runtime.typeOrThrow("AttributeError"));
            }
        }

        @Test
        void withNotFountEnterAndExit() {
            try {
                Python.eval(""
                        + "class A:\n"
                        + "  pass\n"

                        + "with A() as a:\n"
                        + "  print('with')");

                fail("with error");

            } catch (RaiseException e) {
                Python runtime = e.getException().getRuntime();
                assertEquals(e.getException().getType(), runtime.typeOrThrow("AttributeError"));
            }
        }
    }

    @Nested
    class InvalidStatement {
        @Test
        void invalidReturn1() {
            try {
                Python.eval(""
                        + "return 1");

                fail("with error");

            } catch (RaiseException e) {
                PyObject exception = e.getException();
                Python runtime = exception.getRuntime();

                assertEquals(exception.getType(), runtime.typeOrThrow("SyntaxError"));
                assertEquals(runtime.getattr(exception, "args"), runtime.tuple(new PyObject[]{runtime.str("'return' outside function")}));
            }
        }

        @Test
        void invalidReturn2() {
            try {
                Python.eval(""
                        + "def a():\n"
                        + "  class T:\n"
                        + "    return 1");

                fail("with error");

            } catch (RaiseException e) {
                PyObject exception = e.getException();
                Python runtime = exception.getRuntime();

                assertEquals(exception.getType(), runtime.typeOrThrow("SyntaxError"));
                assertEquals(runtime.getattr(exception, "args"), runtime.tuple(new PyObject[]{runtime.str("'return' outside function")}));
            }
        }

        @Test
        void invalidBreak() {
            try {
                Python.eval(""
                        + "break");

                fail("with error");

            } catch (RaiseException e) {
                PyObject exception = e.getException();
                Python runtime = exception.getRuntime();

                assertEquals(exception.getType(), runtime.typeOrThrow("SyntaxError"));
                assertEquals(runtime.getattr(exception, "args"),
                        runtime.tuple(
                                runtime.str("'break' outside loop"),
                                runtime.str("<string>"),
                                runtime.number(1),
                                runtime.number(0),
                                runtime.str("break")));
            }
        }
    }

    @Nested
    class Decorator {
        @Test
        void decorator() throws IOException {
            evalStdOutToResult(""
                    + "def decorator(function):\n"
                    + "  def retfunc(*args):\n"
                    + "    print('decorator_start')\n"
                    + "    function(*args)\n"
                    + "    print('decorator_end')\n"
                    + "  return retfunc\n"
                    + "@decorator\n"
                    + "def a(): print('test')\n"
                    + "a()", result -> {
                assertEquals(result, ""
                        + "decorator_start" + System.lineSeparator()
                        + "test" + System.lineSeparator()
                        + "decorator_end" + System.lineSeparator()
                );
            });
        }

        @Test
        void multiDecorator() throws IOException {
            evalStdOutToResult(""
                    + "def decorator1(function):\n"
                    + "  def retfunc(*args):\n"
                    + "    print('decorator1_start')\n"
                    + "    function(*args)\n"
                    + "    print('decorator1_end')\n"

                    + "  return retfunc\n"

                    + "def decorator2(function):\n"
                    + "  def retfunc(*args):\n"
                    + "    print('decorator2_start')\n"
                    + "    function(*args)\n"
                    + "    print('decorator2_end')\n"

                    + "  return retfunc\n"

                    + "@decorator1\n"
                    + "@decorator2\n"
                    + "def a(): print('test')\n"
                    + "a()", result -> {
                assertEquals(result, ""
                        + "decorator1_start" + System.lineSeparator()
                        + "decorator2_start" + System.lineSeparator()
                        + "test" + System.lineSeparator()
                        + "decorator2_end" + System.lineSeparator()
                        + "decorator1_end" + System.lineSeparator()
                );
            });
        }

        @Test
        void argumentsDecorator1() throws IOException {
            evalStdOutToResult(""
                    + "funcs = []\n"
                    + "def appender(*args, **kwargs):\n"
                    + "  def decorator(function):\n"
                    + "    print(args)\n"

                    + "    if kwargs.get('option1'):\n"
                    + "      print('option1 is True')\n"

                    + "    funcs.append(function)\n"

                    + "  return decorator\n"

                    + "@appender('arg1', option1 = True)\n"
                    + "def hoge(): print('hoge')\n"

                    + "@appender('arg2', option2 = False)\n"
                    + "def huga(): print('huga')\n"

                    + "for f in funcs: f()", result -> {
                assertEquals(result, ""
                        + "('arg1',)" + System.lineSeparator()
                        + "option1 is True" + System.lineSeparator()
                        + "('arg2',)" + System.lineSeparator()
                        + "hoge" + System.lineSeparator()
                        + "huga" + System.lineSeparator()
                );
            });
        }

        @Test
        void argumentsDecorator2() throws IOException {
            evalStdOutToResult(""
                    + "def args_joiner(*dargs, **dkwargs):\n"
                    + "  def decorator(f):\n"
                    + "    def wrapper(*args, **kwargs):\n"
                    + "      newargs = dargs + args\n"
                    + "      newkwargs = {**kwargs, **dkwargs}\n"

                    + "      f(*newargs, **newkwargs)\n"

                    + "    return wrapper\n"

                    + "  return decorator\n"

                    + "@args_joiner('darg', dkwarg=True)\n"
                    + "def print_args(*args, **kwargs):\n"
                    + "  print(args)\n"
                    + "  print(kwargs)\n"

                    + "print_args('arg', kwarg=False)", result -> {
                assertEquals(result, ""
                        + "('darg', 'arg')" + System.lineSeparator()
                        + "{'kwarg': False, 'dkwarg': True}" + System.lineSeparator()
                );
            });
        }

        @Test
        void staticmethod() throws IOException {
            evalStdOutToResult(""
                    + "class T:\n"
                    + "  @staticmethod\n"
                    + "  def a():\n"
                    + "    print('static_method')\n"

                    + "t = T()\n"
                    + "t.a()\n"
                    + "T.a()", result -> {
                assertEquals(result, ""
                        + "static_method" + System.lineSeparator()
                        + "static_method" + System.lineSeparator()
                );
            });
        }

        @Test
        void classmethod() throws IOException {
            evalStdOutToResult(""
                    + "class T:\n"
                    + "  @classmethod\n"
                    + "  def a(klass, arg):\n"
                    + "    print(klass)\n"
                    + "    print(arg)\n"

                    + "t = T()\n"
                    + "t.a('test1')\n"
                    + "T.a('test2')", result -> {
                assertEquals(result, ""
                        + "<class '__main__.T'>" + System.lineSeparator()
                        + "test1" + System.lineSeparator()
                        + "<class '__main__.T'>" + System.lineSeparator()
                        + "test2" + System.lineSeparator()
                );
            });
        }
    }

    @Nested
    class Comment {
        @Test
        void comment() throws IOException {
            evalStdOutToResult(""
                    + "# test1\n"
                    + "print('test') # test2", result -> {
                assertEquals(result, "test" + System.lineSeparator());
            });
        }

        @Test
        void lineComment1() throws IOException {
            evalStdOutToResult(""
                    + "\"\"\"\n"
                    + "test1\n"
                    + "\"\"\"\n"
                    + "print('test')", result -> {
                assertEquals(result, "test" + System.lineSeparator());
            });
        }

        @Test
        void lineComment2() throws IOException {
            evalStdOutToResult(""
                    + "def a():\n"
                    + "  \"\"\"comment\"\"\"\n"
                    + "  print('test')  \n"
                    + "a()", result -> {
                assertEquals(result, "test" + System.lineSeparator());
            });
        }
    }
}