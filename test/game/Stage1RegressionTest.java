package game;

public class Stage1RegressionTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {

        System.out.println("====================================");
        System.out.println(" RESTAURANT RUSH - STAGE 1 TESTS");
        System.out.println("====================================");

        run("Controller seating",
                () -> ControllerSeatingTest.main(new String[0]));

        run("Controller take order",
                () -> ControllerTakeOrderTest.main(new String[0]));

        run("Controller full flow",
                () -> ControllerFullFlowTest.main(new String[0]));

        run("Controller action limits",
                () -> ControllerActionLimitTest.main(new String[0]));

        run("Controller turn sequence",
                () -> ControllerTurnSequenceTest.main(new String[0]));

        run("Turn processor",
                () -> TurnProcessorTest.main(new String[0]));

        run("18-turn victory and defeat simulations",
                () -> ControllerSimulationTest.main(new String[0]));

        run("Waiter",
                () -> staff.WaiterTest.main(new String[0]));

        run("Chef",
                () -> staff.ChefTest.main(new String[0]));

        run("Cashier",
                () -> staff.CashierTest.main(new String[0]));

        System.out.println("\n====================================");
        System.out.println(" FINAL TEST RESULTS");
        System.out.println("====================================");

        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);

        if (failed != 0) {
            System.exit(1);
        }

        System.out.println(
                "ALL STAGE 1 REGRESSION TESTS PASSED!");
    }

    private static void run(String name, Runnable test) {

        System.out.println("\n--- Testing: " + name + " ---");

        try {
            test.run();
            passed++;
            System.out.println("[PASS] " + name);

        } catch (Throwable error) {
            failed++;
            System.out.println("[FAIL] " + name);
            error.printStackTrace(System.out);
        }
    }
}