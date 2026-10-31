package cli;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Black-box CLI checks using the real GameController and scripted keyboard input.
 * No external test libraries are needed.
 * Run with: java -ea -cp out cli.CLISmokeTest
 */
public class CLISmokeTest {
    public static void main(String[] args) throws Exception {
        testDefeatAndWalkouts();
        testVictoryAndOrderDisplays();
        testInvalidInputAndRefresh();
        testEmptyQueueDoesNotConsumeWaiterAction();
        testEndOfInput();
        System.out.println("ALL RESTAURANT RUSH CLI CHECKS PASSED");
    }

    private static void testDefeatAndWalkouts() throws Exception {
        String output = runCLI(noActionInputs());
        contains(output, "RESTAURANT RUSH", "single game title");
        contains(output, "FINAL SERVICE REPORT: DEFEAT", "full 18-turn defeat");
        contains(output, "Average paid satisfaction: N/A", "zero paid guests");
        contains(output, "Unhappy departures: 2", "departure total");
        contains(output, "Customer C1 left unhappy!", "first walkout event");
        contains(output, "Customer C2 left unhappy!", "second walkout event");
        contains(output, "[0] WAIT", "Waiter wait choice is 0");
        absent(output, "[4] SKIP ACTION", "old Waiter shortcut removed");
        System.out.println("PASS 18-turn defeat, walkout events, no-payment average, Waiter 0");
    }

    private static void testVictoryAndOrderDisplays() throws Exception {
        String output = runCLI(victoryInputs());
        contains(output, "FINAL SERVICE REPORT: VICTORY", "victory outcome");
        contains(output, "Revenue: $51.00   Paid: 4", "victory revenue and guests");
        contains(output, "Average paid satisfaction: 68.0", "victory satisfaction");
        contains(output, "SATISFACTION", "victory meter terminology");
        contains(output, "ordered Salad ($9.00)", "order-taking price");
        contains(output, "Burger  $15.00", "kitchen price");
        contains(output, "Pasta   $18.00", "kitchen price for two-unit dish");
        contains(output, "Awaiting preparation", "human-readable placed status");
        contains(output, "Waiting for food", "context-sensitive seated status");
        contains(output, "Ready for serving NEXT turn", "Chef readiness restriction");
        contains(output, "Ready to serve", "later-turn serving status");
        contains(output, "Cashier collected", "actual payment summary");
        absent(output, "READY_TO_PAY", "no domain enum displayed");
        absent(output, "TAKE_ORDER", "no domain action displayed");
        absent(output, "TURN CLEARED: AUTOMATIC STAFF PROCESSED", "natural turn banner");
        System.out.println("PASS 18-turn victory, price, readable statuses, Chef timing, turn summary");
    }

    private static void testInvalidInputAndRefresh() throws Exception {
        String output = runCLI("bad\n9\n1\n99\n1\n1\n1\n1\n999\n0\nq\n");
        contains(output, "whole number", "nonnumeric selection rejected");
        contains(output, "Choose 0, 1, 2, or 3", "Waiter menu validation");
        contains(output, "Customer is not in the waiting queue", "backend validation");
        contains(output, "Order does not exist", "Chef target validation");
        contains(output, "Customer C1 seated at Table 1", "retry succeeded");
        contains(output, "T1: C1 | Awaiting order", "HUD refreshed after successful seating");
        contains(output, "No customers waiting.", "queue refreshed after seating");
        contains(output, "Chef waits this turn", "Chef option 0 works");
        contains(output, "Player quit", "quit operation");
        System.out.println("PASS invalid input recovery, state refresh, Chef 0, quit");
    }

    private static void testEmptyQueueDoesNotConsumeWaiterAction() throws Exception {
        // Seat C1 on turn 1; on turn 2, try seating despite an empty queue.
        String output = runCLI("1\n1\n1\n0\n1\n0\n0\nq\n");
        contains(output, "No customers are waiting to be seated", "empty-queue hint");
        contains(output, "No staff task was consumed", "empty-queue attempt is harmless");
        contains(output, "Waiter waits this turn", "Waiter can reselect option 0");
        System.out.println("PASS empty queue gives hint and allows a new Waiter choice");
    }

    private static void testEndOfInput() throws Exception {
        contains(runCLI(""), "Input ended", "graceful EOF");
        System.out.println("PASS closed input handling");
    }

    private static String noActionInputs() {
        StringBuilder inputs = new StringBuilder();
        for (int turn = 1; turn <= 18; turn++) {
            inputs.append("0\n0\n");
        }
        return inputs.toString();
    }

    /** ControllerSimulationTest's known winning 18-turn sequence. */
    private static String victoryInputs() {
        StringBuilder inputs = new StringBuilder();
        for (int turn = 1; turn <= 18; turn++) {
            switch (turn) {
                case 1: inputs.append("1\n1\n1\n"); break;
                case 2: inputs.append("2\n1\n"); break;
                case 4: inputs.append("3\n1\n"); break;
                case 5: inputs.append("1\n2\n2\n"); break;
                case 6: inputs.append("2\n2\n"); break;
                case 7: inputs.append("1\n3\n1\n"); break;
                case 8: inputs.append("3\n2\n"); break;
                case 9: inputs.append("2\n1\n"); break;
                case 11: inputs.append("1\n4\n2\n"); break;
                case 12: inputs.append("3\n3\n"); break;
                case 13: inputs.append("2\n2\n"); break;
                case 14: inputs.append("3\n4\n"); break;
                default: inputs.append("0\n"); break;
            }
            switch (turn) {
                case 3: inputs.append("1\n"); break;
                case 7: inputs.append("2\n"); break;
                case 10: inputs.append("3\n"); break;
                case 11: inputs.append("3\n"); break;
                case 13: inputs.append("4\n"); break;
                default: inputs.append("0\n"); break;
            }
        }
        return inputs.toString();
    }

    private static String runCLI(String keyboardInput) throws Exception {
        InputStream previousInput = System.in;
        PrintStream previousOutput = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream(keyboardInput.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(buffer, true, "UTF-8"));
            RestaurantCLI.main(new String[0]);
            return buffer.toString("UTF-8");
        } finally {
            System.setIn(previousInput);
            System.setOut(previousOutput);
        }
    }

    private static void contains(String output, String expected, String context) {
        if (!output.contains(expected)) {
            throw new AssertionError(context + ": missing [" + expected + "] in output:\n" + output);
        }
    }

    private static void absent(String output, String forbidden, String context) {
        if (output.contains(forbidden)) {
            throw new AssertionError(context + ": unexpectedly displayed [" + forbidden + "]");
        }
    }
}
