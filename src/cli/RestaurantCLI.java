package cli;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;

import customer.Customer;
import customer.CustomerStatus;
import customer.Order;
import game.GameController;
import game.GameResult;
import game.GameState;
import game.SeatChoice;
import game.ServeChoice;
import game.TakeOrderChoice;
import game.WaitChoice;
import game.WaiterChoice;
import menu.Menu;
import staff.Table;

/**
 * Restaurant Rush's single interactive command-line boundary.
 *
 * This class reads decisions and renders snapshots of the domain objects.
 * GameController alone validates and changes the simulation state.
 */
public class RestaurantCLI {

    private static final String RESET = "\u001B[0m";
    private static final String CYAN = "\u001B[96m";
    private static final String MAGENTA = "\u001B[95m";
    private static final String GREEN = "\u001B[92m";
    private static final String YELLOW = "\u001B[93m";
    private static final String RED = "\u001B[91m";

    private final GameController controller;
    private final Scanner input;

    public RestaurantCLI(GameController controller) {
        if (controller == null) {
            throw new IllegalArgumentException("GameController is required");
        }
        this.controller = controller;
        this.input = new Scanner(System.in);
    }

    /** Run from the repository root with: java -cp out cli.Main */
    public static void main(String[] args) {
        if (args.length > 0) {
            System.out.println("Usage: java -cp out cli.Main");
            return;
        }
        new RestaurantCLI(new GameController(new Menu())).run();
    }

    /** Display the current controller-owned state, without modifying it. */
    public void displayState() {
        GameState state = controller.getGameState();
        System.out.println(accent("\n+=========================[ RESTAURANT RUSH ]=========================+", MAGENTA));
        System.out.printf("  TURN %02d / %02d    SHIFT %s%n", state.getCurrentTurn(),
                GameState.TOTAL_TURNS, meter(state.getCurrentTurn(), GameState.TOTAL_TURNS, 18));
        System.out.printf("  CASH %s     PAID %d     SERVED %d     WALKOUTS %d%n",
                money(state.getRevenueCents()), state.getPaidCount(), state.getServedCount(),
                state.getUnhappyDepartures());
        System.out.println(accent("  +-- VICTORY METERS ------------------------------------------------+", CYAN));
        System.out.printf("  | GUESTS        %s  %d / %d paid%n",
                meter(state.getPaidCount(), GameState.VICTORY_MIN_PAID_CUSTOMERS, 12),
                state.getPaidCount(), GameState.VICTORY_MIN_PAID_CUSTOMERS);
        System.out.printf("  | SALES         %s  %s / %s%n",
                meter(state.getRevenueCents(), GameState.VICTORY_MIN_REVENUE_CENTS, 12),
                money(state.getRevenueCents()), money(GameState.VICTORY_MIN_REVENUE_CENTS));
        System.out.printf("  | SATISFACTION  %s  avg %s / %d%n",
                meter(state.getPaidCount() == 0 ? 0 : (int) state.getAverageSatisfaction(),
                        GameState.VICTORY_MIN_AVERAGE_SATISFACTION, 12),
                average(state), GameState.VICTORY_MIN_AVERAGE_SATISFACTION);
        System.out.println("  +-----------------------------------------------------------------+");

        System.out.println(accent("  >>> CUSTOMER PATIENCE <<<", YELLOW));
        if (state.getWaitingList().isEmpty()) {
            System.out.println("    No customers waiting.");
        }
        for (Customer customer : state.getWaitingList()) {
            String line = String.format(Locale.ROOT, "    C%-2d SAT %3d/100 %s",
                    customer.getCustomerId(), customer.getSatisfaction(),
                    meter(customer.getSatisfaction(), 100, 10));
            System.out.println(customer.getSatisfaction() <= 32 ? accent(line, RED) : line);
        }

        System.out.println(accent("  >>> TABLE STATUS <<<", CYAN));
        for (Table table : state.getTables()) {
            if (table.isAvailable()) {
                System.out.printf("    T%d: Empty / ready for guest%n", table.getTableId());
            } else {
                Customer customer = table.getOccupant();
                System.out.printf("    T%d: C%d | %s | satisfaction %d%n",
                        table.getTableId(), customer.getCustomerId(),
                        customerStatusLabel(customer), customer.getSatisfaction());
            }
        }
        showOrders();
        System.out.println(accent("+=====================================================================+", MAGENTA));
    }

    /** Read a single choice; invalid menu input never reaches GameController. */
    public WaiterChoice getWaiterChoice() {
        while (true) {
            System.out.println(accent("\n  [NEXT MOVE] WAITER SELECT", MAGENTA));
            System.out.println("    [1] SEAT GUEST        [2] TAKE ORDER");
            System.out.println("    [3] DELIVER FOOD      [0] WAIT");
            int action = readInt("WAITER action [0-3; Q=quit] > ");
            switch (action) {
                case 0:
                    return new WaitChoice();
                case 1:
                    if (controller.getGameState().getWaitingList().isEmpty()) {
                        displayError("No customers are waiting to be seated.");
                        break;
                    }
                    return new SeatChoice(readInt("Waiting customer ID > "),
                            readInt("Free table ID > "));
                case 2:
                    return new TakeOrderChoice(readInt("Occupied table ID > "));
                case 3:
                    showOrders();
                    return new ServeChoice(readInt("Order ID to serve > "));
                default:
                    displayError("Choose 0, 1, 2, or 3.");
            }
        }
    }

    /** A UI choice of 0 maps to -1 so the controller is never asked to prepare it. */
    public int getChefOrderChoice() {
        System.out.println(accent("\n  [CHEF STATION]", CYAN));
        showOrders();
        System.out.println("  Enter an order ID to prepare ONE unit of that dish.");
        System.out.println("  Enter 0 to wait; a completed dish can be served next turn.");
        int id = readInt("CHEF order ID [0=wait; Q=quit] > ");
        return id == 0 ? -1 : id;
    }

    public void displayError(String message) {
        System.out.println(accent("  [REJECTED] " + message, RED));
        System.out.println("  No staff task was consumed. Choose again.");
    }

    public void displayResult(GameResult result) {
        GameState state = controller.getGameState();
        System.out.println("\n+=====================================================================+");
        System.out.println(accent("           RESTAURANT RUSH - FINAL SERVICE REPORT: " + result,
                result == GameResult.VICTORY ? GREEN : RED));
        System.out.println("+=====================================================================+");
        System.out.printf("  Revenue: %s   Paid: %d   Served: %d%n", money(state.getRevenueCents()),
                state.getPaidCount(), state.getServedCount());
        System.out.printf("  Average paid satisfaction: %s   Unhappy departures: %d%n",
                average(state), state.getUnhappyDepartures());
        if (result == GameResult.DEFEAT) {
            System.out.println("  Unmet objectives:");
            for (String reason : state.getDefeatCauses()) {
                System.out.println("    - " + reason);
            }
        } else {
            System.out.println(accent("  ALL THREE RESTAURANT OBJECTIVES COMPLETED!", GREEN));
        }
        System.out.println("+=====================================================================+\n");
    }

    /** Start -> state -> Waiter -> refreshed state -> Chef -> automatic updates. */
    public void run() {
        welcome();
        try {
            controller.startGame();
            while (!controller.isGameOver()) {
                controller.startTurn();
                displayState();

                while (true) {
                    WaiterChoice choice = getWaiterChoice();
                    String error = controller.executeWaiterChoice(choice);
                    if (error == null) {
                        showWaiterSuccess(choice);
                        displayState(); // Refreshed immediately after the accepted action.
                        break;
                    }
                    displayError(error);
                }

                while (true) {
                    int orderId = getChefOrderChoice();
                    if (orderId == -1) {
                        success("Chef waits this turn.");
                        break;
                    }
                    String error = controller.executeChefChoice(orderId);
                    if (error == null) {
                        showChefSuccess(orderId);
                        break;
                    }
                    displayError(error);
                }

                TurnSnapshot before = new TurnSnapshot(controller.getGameState());
                controller.endTurn();
                showEndTurn(before);
            }
            displayResult(controller.checkResult());
        } catch (QuitRequested e) {
            System.out.println("\nPlayer quit; game not completed. Thanks for playing!");
        } catch (NoSuchElementException e) {
            System.out.println("\nInput ended; game not completed.");
        }
    }

    private void welcome() {
        System.out.println(accent("+=====================================================================+", MAGENTA));
        System.out.println(accent("|                        RESTAURANT RUSH                              |", MAGENTA));
        System.out.println(accent("+=====================================================================+", MAGENTA));
        System.out.println("  18 turns | 2 tables | Waiter + Chef | automatic Cashier");
        System.out.println("  WIN: 4+ paid guests, $45.00+ revenue, 60+ average satisfaction");
        System.out.println("  Enter 0 to wait; Q quits at any input prompt.");
    }

    private void showOrders() {
        GameState state = controller.getGameState();
        System.out.println(accent("  KITCHEN ORDERS (price is per dish)", CYAN));
        if (state.getOrders().isEmpty()) {
            System.out.println("    No orders have been placed yet.");
            return;
        }
        for (Order order : state.getOrders()) {
            System.out.printf("    #%d C%d  %-7s %-7s  %-25s  prep left %d%n",
                    order.getOrderId(), order.getCustomerId(), order.getItem().getName(),
                    money(order.getFinalPriceCents()), orderStatusLabel(order, state.getCurrentTurn()),
                    order.getRemainingUnits());
        }
    }

    /** A single location for all domain-status-to-player-label translations. */
    private static String customerStatusLabel(Customer customer) {
        return switch (customer.getStatus()) {
            case WAITING -> "Waiting outside";
            case SEATED -> customer.getOrder() == null ? "Awaiting order" : "Waiting for food";
            case EATING -> "Enjoying meal";
            case READY_TO_PAY -> "Waiting for bill";
            case PAID -> "Paid and departed";
            case LEFT -> "Left unhappy";
        };
    }

    private static String orderStatusLabel(Order order, int currentTurn) {
        return switch (order.getStatus()) {
            case PLACED -> "Awaiting preparation";
            case PREPARING -> "Cooking";
            case READY -> currentTurn <= order.getReadyTurn()
                    ? "Ready (serve next turn)" : "Ready to serve";
            case SERVED -> "Meal delivered";
            case PAID -> "Bill paid";
            case CANCELLED -> "Cancelled";
        };
    }

    private void showWaiterSuccess(WaiterChoice choice) {
        if (choice instanceof SeatChoice seat) {
            success("Customer C" + seat.getCustomerId() + " seated at Table " + seat.getTableId() + ".");
        } else if (choice instanceof TakeOrderChoice takeOrder) {
            for (Table table : controller.getGameState().getTables()) {
                if (table.getTableId() == takeOrder.getTableId()) {
                    Customer customer = table.getOccupant();
                    Order order = customer.getOrder();
                    success("Order #" + order.getOrderId() + ": C" + customer.getCustomerId()
                            + " ordered " + order.getItem().getName() + " ("
                            + money(order.getFinalPriceCents()) + ").");
                    return;
                }
            }
        } else if (choice instanceof ServeChoice serve) {
            success("Order #" + serve.getOrderId() + " delivered to the customer.");
        } else {
            success("Waiter waits this turn.");
        }
    }

    private void showChefSuccess(int orderId) {
        for (Order order : controller.getGameState().getOrders()) {
            if (order.getOrderId() == orderId) {
                if (order.getRemainingUnits() == 0) {
                    success("Finished preparing " + order.getItem().getName()
                            + " (order #" + orderId + "). Ready for serving NEXT turn.");
                } else {
                    success("Prepared one unit of " + order.getItem().getName() + " (order #"
                            + orderId + "). " + order.getRemainingUnits() + " unit(s) left.");
                }
                return;
            }
        }
    }

    /** Announce actual changes, rather than implying a payment happens every turn. */
    private void showEndTurn(TurnSnapshot before) {
        GameState state = controller.getGameState();
        System.out.println(accent("\n  === TURN " + state.getCurrentTurn() + " COMPLETE - SHIFT SUMMARY ===", GREEN));
        int newlyPaid = state.getPaidCount() - before.paidCount;
        int newRevenue = state.getRevenueCents() - before.revenueCents;
        if (newlyPaid > 0) {
            System.out.printf("  Cashier collected %s from %d customer(s).%n",
                    money(newRevenue), newlyPaid);
        } else {
            System.out.println("  No payments collected this turn.");
        }
        for (Customer customer : state.getCustomers()) {
            if (customer.getStatus() == CustomerStatus.LEFT
                    && before.activeCustomerIds.contains(customer.getCustomerId())) {
                System.out.println(accent("  [!] Customer C" + customer.getCustomerId()
                        + " left unhappy! They ran out of patience.", RED));
            }
        }
        System.out.printf("  Total cash %s | Paid %d | Avg satisfaction %s | Walkouts %d%n",
                money(state.getRevenueCents()), state.getPaidCount(), average(state),
                state.getUnhappyDepartures());
    }

    /** Remember only the values needed to report events after endTurn mutates state. */
    private static final class TurnSnapshot {
        private final int revenueCents;
        private final int paidCount;
        private final List<Integer> activeCustomerIds = new ArrayList<>();

        private TurnSnapshot(GameState state) {
            revenueCents = state.getRevenueCents();
            paidCount = state.getPaidCount();
            for (Customer customer : state.getCustomers()) {
                if (customer.getStatus() != CustomerStatus.LEFT) {
                    activeCustomerIds.add(customer.getCustomerId());
                }
            }
        }
    }

    private void success(String message) {
        System.out.println(accent("  [OK] " + message, GREEN));
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!input.hasNextLine()) {
                throw new NoSuchElementException("Input ended");
            }
            String line = input.nextLine().trim();
            if (line.equalsIgnoreCase("q")) {
                throw new QuitRequested();
            }
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                displayError("Enter a whole number, or Q to quit.");
            }
        }
    }

    private static String meter(int value, int target, int width) {
        int filled = Math.min(width, Math.max(0, (int) ((long) value * width / target)));
        return "[" + "#".repeat(filled) + "-".repeat(width - filled) + "]";
    }

    private static String money(int cents) {
        return String.format(Locale.ROOT, "$%.2f", cents / 100.0);
    }

    private static String average(GameState state) {
        return state.getPaidCount() == 0 ? "N/A"
                : String.format(Locale.ROOT, "%.1f", state.getAverageSatisfaction());
    }

    private static String accent(String message, String colour) {
        return System.getenv("NO_COLOR") == null ? colour + message + RESET : message;
    }

    private static final class QuitRequested extends RuntimeException { }
}
