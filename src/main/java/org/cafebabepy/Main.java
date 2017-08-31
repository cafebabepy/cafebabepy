package org.cafebabepy;

import org.cafebabepy.interactive.InteractiveConsole;
import org.cafebabepy.runtime.Python;

import java.io.IOException;

/**
 * Created by yotchang4s on 2017/07/14.
 */
public class Main {
    public static void main(String... args) throws IOException {
        /*
        String input = ""
                + "if 3 > 2:\n"
                + "   11\n"
                + "   1 if 1 > 2 else 3\n"
                + "elif 3 < 4:\n"
                + "   22\n"
                + "   222\n"
                + "else:\n"
                + "   33\n"
                + "   333";
        */

        /*
        String input = ""
                + "def fib(n):\n"
                + "  if n > 2:\n"
                + "    return fib(n - 1) + fib(n - 2)\n"
                + "  else:\n"
                + "    return 1\n"
                + "\n"
                + "print(fib(20))\n";
        */
        /*
        String input = ""
                + "for i in range(5):\n"
                + "  print(i)\n"
                + "else:\n"
                + "  print(99)\n";
        */

        /*
        String input = ""
                + "if 1 == 1:\r\n"
                + "  print(2)\n"
                + "if 2 == 2:\n"
                + "  print(3)";
        */

        /*
        String input = ""
                + "fizzbuzz = [('fizzbuzz' if x % 15 == 0 else ('fizz' if x % 3 == 0 else ('buzz' if x % 5 == 0 else x))) for x in range(1, 101)]\n"
                + "for x in fizzbuzz:\n"
                + "  print(x)\n";

        System.out.println(input);
        */

        Python runtime = Python.createRuntime();

        InteractiveConsole console = new InteractiveConsole(runtime);

        console.interact();

        /*
        long time = System.currentTimeMillis();

        CafeBabePyParser parser = new InteractiveParser(runtime);
        PyObject ast = parser.parse(input);

        InterpretEvaluator evaluter = new InterpretEvaluator(runtime);
        evaluter.evalMainModule(ast);

        long runTime = System.currentTimeMillis() - time;

        System.out.println();
        System.out.println(runTime + "ms");
        System.out.println(((double)runTime / 1000 / 60) + "m");
        */
    }
}
