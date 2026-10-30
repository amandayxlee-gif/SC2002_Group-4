package cli;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * End-to-end black-box console checks against the actual GameController.
 * No testing framework or external libraries are required.
 * Run with: java -ea -cp out cli.CLISmokeTest
 */
public class CLISmokeTest {
    public static void main(String[] args) throws Exception {
        for (String theme : new String[]{"arcade", "tycoon", "quest"}) {
            String defeat = runCLI(theme, noActionInputs());
            contains(defeat, "FINAL SERVICE REPORT", theme + " defeat flow");
            contains(defeat, "DEFEAT", theme + " defeat result");
            contains(defeat, "N/A", theme + " zero-payment average");
            contains(defeat, "0", theme + " zero revenue");
            contains(defeat, "Paid: 0", theme + " no paid customers");
            contains(defeat, "Unhappy departures: 2", theme + " abandonment count");
            System.out.println("PASS " + theme + " full 18-turn defeat");

            String victory = runCLI(theme, victoryInputs());
            contains(victory, "VICTORY", theme + " victory result");
            contains(victory, "$51.00", theme + " victory revenue");
            contains(victory, "Paid: 4", theme + " final paid metric");
            contains(victory, "Average paid satisfaction: 68.0", theme + " paid satisfaction");
            System.out.println("PASS " + theme + " full 18-turn victory");

            String errors = runCLI(theme,
                    "bad\n9\n1\n99\n1\n1\n1\n1\n999\n0\nq\n");
            contains(errors, "whole number", theme + " text input rejection");
            contains(errors, "Choose 1, 2, 3, or 4", theme + " menu range rejection");
            contains(errors, "Customer is not in the waiting queue", theme + " target rejection");
            contains(errors, "Order does not exist", theme + " chef target rejection");
            contains(errors, "Waiter: SEAT completed", theme + " action retry success");
            contains(errors, "Player quit", theme + " quit");
            System.out.println("PASS " + theme + " invalid inputs + recovery + quit");

            String eof = runCLI(theme, "");
            contains(eof, "Input ended", theme + " input EOF");
            System.out.println("PASS " + theme + " graceful EOF");
        }
        System.out.println("ALL 12 CLI CHECKS PASSED");
    }

    private static String noActionInputs() {
        StringBuilder b = new StringBuilder();
        for (int i = 1; i <= 18; i++) b.append("4\n0\n");
        return b.toString();
    }

    /** Mirrors the validated Stage 1 winning sequence in ControllerSimulationTest. */
    private static String victoryInputs() {
        StringBuilder b = new StringBuilder();
        for (int turn = 1; turn <= 18; turn++) {
            switch (turn) {
                case 1: b.append("1\n1\n1\n"); break;
                case 2: b.append("2\n1\n"); break;
                case 4: b.append("3\n1\n"); break;
                case 5: b.append("1\n2\n2\n"); break;
                case 6: b.append("2\n2\n"); break;
                case 7: b.append("1\n3\n1\n"); break;
                case 8: b.append("3\n2\n"); break;
                case 9: b.append("2\n1\n"); break;
                case 11: b.append("1\n4\n2\n"); break;
                case 12: b.append("3\n3\n"); break;
                case 13: b.append("2\n2\n"); break;
                case 14: b.append("3\n4\n"); break;
                default: b.append("4\n"); break;
            }
            switch (turn) {
                case 3: b.append("1\n"); break;
                case 7: b.append("2\n"); break;
                case 10: b.append("3\n"); break;
                case 11: b.append("3\n"); break;
                case 13: b.append("4\n"); break;
                default: b.append("0\n"); break;
            }
        }
        return b.toString();
    }

    private static String runCLI(String theme, String keyboardInput) throws Exception {
        InputStream previousIn = System.in;
        PrintStream previousOut = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream(keyboardInput.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(buffer, true, "UTF-8"));
            RestaurantCLI.main(new String[]{theme});
            return buffer.toString("UTF-8");
        } finally {
            System.setIn(previousIn);
            System.setOut(previousOut);
        }
    }

    private static void contains(String output, String expected, String name) {
        if (!output.contains(expected)) {
            throw new AssertionError(name + ": expected to find [" + expected + "] in:\n" + output);
        }
    }
}
