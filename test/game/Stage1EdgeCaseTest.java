package game;

import customer.Customer;
import customer.CustomerStatus;
import customer.Order;
import customer.OrderStatus;
import menu.Menu;
import staff.Cashier;
import staff.Table;

public class Stage1EdgeCaseTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {

        System.out.println("====================================");
        System.out.println(" RESTAURANT RUSH - EDGE CASE TESTS");
        System.out.println("====================================");

        run("Initial game state",
                Stage1EdgeCaseTest::testInitialState);

        run("Scheduled arrivals",
                Stage1EdgeCaseTest::testArrivals);

        run("Invalid actions preserve state",
                Stage1EdgeCaseTest::testInvalidActions);

        run("One Waiter action per turn",
                Stage1EdgeCaseTest::testWaiterLimit);

        run("One Chef action per turn",
                Stage1EdgeCaseTest::testChefLimit);

        run("Order preparation and serving timing",
                Stage1EdgeCaseTest::testPreparationTiming);

        run("Payment and table reuse",
                Stage1EdgeCaseTest::testPaymentAndTableReuse);

        run("Abandonment counted once",
                Stage1EdgeCaseTest::testAbandonment);

        run("Collections cannot be modified externally",
                Stage1EdgeCaseTest::testUnmodifiableCollections);

        run("Game runs exactly 18 turns",
                Stage1EdgeCaseTest::testGameDuration);

        run("Victory and defeat requirements",
                Stage1EdgeCaseTest::testResultRequirements);

        System.out.println("\n====================================");
        System.out.println(" EDGE CASE TEST RESULTS");
        System.out.println("====================================");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);

        if (failed != 0) {
            System.exit(1);
        }

        System.out.println("ALL EDGE CASE TESTS PASSED!");
    }

    private static void run(String name, Runnable test) {
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

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static GameController newController() {
        return new GameController(new Menu());
    }

    private static void testInitialState() {
        GameState state = new GameState();

        check(state.getCurrentTurn() == 0,
                "Game must start at turn 0");

        check(state.getTables().size() == 2,
                "Game must have exactly two tables");

        check(state.getRevenueCents() == 0,
                "Revenue must start at zero");

        check(state.getPaidCount() == 0,
                "Paid count must start at zero");

        check(state.getAverageSatisfaction() == 0.0,
                "Average satisfaction must start at zero");

        check(!state.isGameOver(),
                "Game must not be over initially");

        for (Table table : state.getTables()) {
            check(table.isAvailable(),
                    "Both tables must initially be empty");
        }
    }

    private static void testArrivals() {
        GameController controller = newController();

        int[] arrivalTurns = {1, 4, 7, 10, 13, 16};
        int expectedCustomers = 0;

        for (int turn = 1; turn <= 18; turn++) {
            controller.startTurn();

            for (int arrival : arrivalTurns) {
                if (turn == arrival) {
                    expectedCustomers++;
                }
            }

            check(controller.getGameState()
                            .getCustomers().size() == expectedCustomers,
                    "Incorrect customer count at turn " + turn);

            controller.endTurn();
        }

        check(controller.getGameState()
                        .getCustomers().size() == 6,
                "Exactly six customers must arrive");
    }

    private static void testInvalidActions() {
        GameController controller = newController();

        controller.startTurn();

        GameState state = controller.getGameState();

        check(controller.executeWaiterChoice(null) != null,
                "Null waiter choice must be rejected");

        check(controller.executeWaiterChoice(
                        new SeatChoice(999, 1)) != null,
                "Unknown customer must be rejected");

        check(controller.executeWaiterChoice(
                        new SeatChoice(1, 999)) != null,
                "Unknown table must be rejected");

        check(controller.executeWaiterChoice(
                        new TakeOrderChoice(1)) != null,
                "Cannot take order from empty table");

        check(controller.executeWaiterChoice(
                        new ServeChoice(999)) != null,
                "Cannot serve nonexistent order");

        check(controller.executeChefChoice(999) != null,
                "Cannot prepare nonexistent order");

        check(state.getWaitingList().size() == 1,
                "Invalid actions must not remove customer");

        check(state.getOrders().isEmpty(),
                "Invalid actions must not create orders");

        check(state.getTables().get(0).isAvailable(),
                "Invalid actions must not occupy tables");

        check(controller.executeWaiterChoice(
                        new SeatChoice(1, 1)) == null,
                "Valid action should still work after rejection");

        controller.endTurn();
    }

    private static void testWaiterLimit() {
        GameController controller = newController();

        controller.startTurn();

        check(controller.executeWaiterChoice(
                        new SeatChoice(1, 1)) == null,
                "First waiter action must succeed");

        check(controller.executeWaiterChoice(
                        new WaitChoice()) != null,
                "Second waiter action must be rejected");

        controller.endTurn();

        controller.startTurn();

        check(controller.executeWaiterChoice(
                        new TakeOrderChoice(1)) == null,
                "Waiter limit must reset next turn");

        controller.endTurn();
    }

    private static void testChefLimit() {
        GameController controller = newController();

        controller.startTurn();
        check(controller.executeWaiterChoice(
                        new SeatChoice(1, 1)) == null,
                "Customer must be seated");
        controller.endTurn();

        controller.startTurn();
        check(controller.executeWaiterChoice(
                        new TakeOrderChoice(1)) == null,
                "Order must be created");

        check(controller.executeChefChoice(1) == null,
                "First Chef preparation must succeed");

        check(controller.executeChefChoice(1) != null,
                "Chef cannot prepare twice in one turn");

        controller.endTurn();
    }

    private static void testPreparationTiming() {
        GameController controller = newController();

        controller.startTurn();
        check(controller.executeWaiterChoice(
                        new SeatChoice(1, 1)) == null,
                "Seat must succeed");
        controller.endTurn();

        controller.startTurn();
        check(controller.executeWaiterChoice(
                        new TakeOrderChoice(1)) == null,
                "Take order must succeed");

        Order order = controller.getGameState()
                .getOrders().get(0);

        check(order.getStatus() == OrderStatus.PLACED,
                "New order must be PLACED");

        check(controller.executeChefChoice(1) == null,
                "Chef must prepare order");

        check(order.getStatus() == OrderStatus.READY,
                "One-unit Salad must become READY");

        check(controller.executeWaiterChoice(
                        new ServeChoice(1)) != null,
                "Waiter cannot act twice in same turn");

        controller.endTurn();

        controller.startTurn();

        check(controller.executeWaiterChoice(
                        new ServeChoice(1)) == null,
                "READY order must be servable next turn");

        check(order.getStatus() == OrderStatus.SERVED,
                "Served order must have SERVED status");

        check(controller.getGameState().getServedCount() == 1,
                "Served count must increase");

        controller.endTurn();
    }

    private static void testPaymentAndTableReuse() {
        GameController controller = newController();

        // Turn 1: Seat
        controller.startTurn();
        check(controller.executeWaiterChoice(
                        new SeatChoice(1, 1)) == null,
                "Seating must succeed");
        controller.endTurn();

        // Turn 2: Take order and prepare
        controller.startTurn();
        check(controller.executeWaiterChoice(
                        new TakeOrderChoice(1)) == null,
                "Order taking must succeed");
        check(controller.executeChefChoice(1) == null,
                "Preparation must succeed");
        controller.endTurn();

        // Turn 3: Serve
        controller.startTurn();
        check(controller.executeWaiterChoice(
                        new ServeChoice(1)) == null,
                "Serving must succeed");
        controller.endTurn();

        // Turn 4: Eating
        controller.startTurn();
        controller.endTurn();

        check(controller.getGameState().getPaidCount() == 0,
                "Customer must not pay too early");

        // Turn 5: Payment
        controller.startTurn();
        controller.endTurn();

        GameState state = controller.getGameState();

        check(state.getPaidCount() == 1,
                "Customer must have paid");

        check(state.getRevenueCents() == 900,
                "Salad payment must add 900 cents");

        check(state.getAverageSatisfaction() == 84.0,
                "Paid satisfaction should be 84");

        check(state.getTables().get(0).isAvailable(),
                "Table must be released after payment");

        check(state.getOrders().get(0).getStatus()
                        == OrderStatus.PAID,
                "Order must become PAID");

        // Turn 6: Seat second customer at same table
        controller.startTurn();

        check(controller.executeWaiterChoice(
                        new SeatChoice(2, 1)) == null,
                "Released table must be reusable");

        check(!state.getTables().get(0).isAvailable(),
                "Reused table must become occupied");

        controller.endTurn();
    }

    private static void testAbandonment() {
        GameState state = new GameState();
        TurnProcessor processor =
                new TurnProcessor(state, new Cashier());

        Customer customer = new Customer(1);
        state.addCustomer(customer);

        for (int turn = 1; turn <= 13; turn++) {
            processor.runEndOfTurn(turn);
        }

        check(customer.getStatus() == CustomerStatus.LEFT,
                "Customer must leave at zero satisfaction");

        check(customer.getSatisfaction() == 0,
                "Satisfaction must not go below zero");

        check(state.getUnhappyDepartures() == 1,
                "Departure must be counted once");

        check(state.getWaitingList().isEmpty(),
                "Abandoned customer must leave waiting list");

        processor.runEndOfTurn(14);

        check(state.getUnhappyDepartures() == 1,
                "Departure must not be counted twice");
    }

    private static void testUnmodifiableCollections() {
        GameState state = new GameState();

        boolean rejected = false;

        try {
            state.getTables().clear();
        } catch (UnsupportedOperationException e) {
            rejected = true;
        }

        check(rejected,
                "External code must not modify table list");

        check(state.getTables().size() == 2,
                "Tables must remain unchanged");
    }

    private static void testGameDuration() {
        GameController controller = newController();

        for (int turn = 1; turn <= 18; turn++) {
            check(!controller.isGameOver(),
                    "Game must not end before turn 18");

            controller.startTurn();

            check(controller.getGameState()
                            .getCurrentTurn() == turn,
                    "Incorrect turn number");

            controller.endTurn();
        }

        check(controller.isGameOver(),
                "Game must end after turn 18");

        boolean rejected = false;

        try {
            controller.startTurn();
        } catch (IllegalStateException e) {
            rejected = true;
        }

        check(rejected,
                "Starting turn 19 must be rejected");
    }

    private static void testResultRequirements() {
        GameState state = new GameState();

        check(state.checkResult() == GameResult.DEFEAT,
                "Zero progress must be defeat");

        check(state.getDefeatCauses().size() == 3,
                "Zero progress must miss all three targets");

        Customer customer = new Customer(1);

        // Four payments of $15 each, satisfaction 100
        for (int i = 0; i < 4; i++) {
            state.recordPayment(customer, 1500);
        }

        check(state.getPaidCount() == 4,
                "Paid count must be four");

        check(state.getRevenueCents() == 6000,
                "Revenue must be 6000 cents");

        check(state.getAverageSatisfaction() == 100.0,
                "Average satisfaction must be 100");

        check(state.checkResult() == GameResult.VICTORY,
                "All three targets met must mean victory");

        check(state.getDefeatCauses().isEmpty(),
                "Victory must have no defeat causes");
    }
}