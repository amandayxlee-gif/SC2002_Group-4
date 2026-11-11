package cli;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class CLISmokeTest {
    public static void main(String[] args) throws Exception {
        Stage3CLITest.main(args);
        String output = runCLI("");
        require(output, "Input ended; game not completed.");
        String afterHost = runCLI("0\n1\n1\n1\n0\n0\n0\n0\n1\n0\n0\nq\n");
        require(afterHost, "No customers are waiting to be seated");
        require(afterHost, "No staff task was consumed. Choose again.");
        System.out.println("PASS empty-input handling and Host/Waiter retry");
        System.out.println("ALL RESTAURANT RUSH CLI CHECKS PASSED");
    }

    private static String runCLI(String input) throws Exception {
        InputStream savedInput = System.in;
        PrintStream savedOutput = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
            RestaurantCLI.main(new String[0]);
            return buffer.toString(StandardCharsets.UTF_8);
        } finally {
            System.setIn(savedInput);
            System.setOut(savedOutput);
        }
    }

    private static void require(String output, String expected) {
        if (!output.contains(expected)) {
            throw new AssertionError("Missing " + expected);
        }
    }
}
