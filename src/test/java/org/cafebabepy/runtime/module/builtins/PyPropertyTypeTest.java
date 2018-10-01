package org.cafebabepy.runtime.module.builtins;

import org.cafebabepy.runtime.Python;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PyPropertyTypeTest {
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
    void setterTest() throws IOException {
        evalStdOutToResult(""
                        + "class T:\n"
                        + "  def __init__(self):\n"
                        + "    self._test = 99\n"

                        + "  @property\n"
                        + "  def test(self):\n"
                        + "    return self._test\n"
                        + "  @test.setter\n"
                        + "  def test(self, test):\n"
                        + "    self._test = test * 2\n"
                        + "t = T()\n"
                        + "t.test = 10\n"
                        + "print(t.test)",
                result -> {
                    assertEquals(result, ""
                            + "20" + System.lineSeparator()
                    );
                });
    }
}
