package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.Python;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PySuperTypeTest {
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

    @Test
    void boundSuperTest() throws IOException {
        evalStdOutToResult(""
                        + "class A:\n"
                        + "  def __init__(self):\n"
                        + "    print(99)\n"
                        + "class B(A):\n"
                        + "  def __init__(self):\n"
                        + "    super(B, self).__init__()\n"
                        + "B()\n",
                result -> {
                    assertEquals(result, ""
                            + "99" + System.lineSeparator()
                    );
                });
    }
}
