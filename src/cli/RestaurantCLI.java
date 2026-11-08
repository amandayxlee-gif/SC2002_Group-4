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

public class RestaurantCLI {

    private static final String RESET = "\u001B[0m";
    private static final String CYAN = "\u001B[96m";
    private static final String MAGENTA = "\u001B[95m";
    private static final String GREEN = "\u001B[92m";
    private static final String YELLOW = "\u001B[93m";
    private static final String RED = "\u001B[91m";

    private static final int LOW_SATISFACTION_WARNING = 32;

    private final GameController controller;
    private final Scanner input;

    public RestaurantCLI(GameController controller) {
        if (controller == null) {
            throw new IllegalArgumentException("GameController is required");
        }
        this.controller = controller;
        this.input = new Scanner(System.in);
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            System.out.println("Usage: java -cp out cli.Main");
            return;
        }
        new RestaurantCLI(new GameController(new Menu())).run();
    }

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
                meter(state.getPaidCount() == 0 ? 0 : (int) state.getAveragePaidSatisfaction(),
                        GameState.VICTORY_MIN_AVERAGE_SATISFACTION, 12),
                average(state), GameState.VICTORY_MIN_AVERAGE_SATISFACTION);
        System.out.println("  +-----------------------------------------------------------------+");

        System.out.println(accent("  >>> CUSTOMER PATIENCE <<<", YELLOW));
        if (state.getWaitingList().isEmpty()) {
            System.out.println("    No customers waiting.");
        }
        for (Customer customer : state.getWaitingList()) {
            String line = String.format(Locale.ROOT, "    C%-2d SAT %3d/100 %s  %s",
                    customer.getCustomerId(), customer.getSatisfaction(),
                    meter(customer.getSatisfaction(), Customer.MAX_SATISFACTION, 10),
                    customerTypeLabel(customer));
            System.out.println(customer.getSatisfaction() <= LOW_SATISFACTION_WARNING
                    ? accent(line, RED) : line);
        }

        System.out.println(accent("  >>> TABLE STATUS <<<", CYAN));
        for (Table table : state.getTables()) {
            if (table.isAvailable()) {
                System.out.printf("    T%d: Empty / ready for guest%n", table.getTableId());
            } else {
                Customer customer = table.getOccupant();
                System.out.printf("    T%d: C%d | %s | satisfaction %d  %s%n",
                        table.getTableId(), customer.getCustomerId(),
                        customerStatusLabel(customer), customer.getSatisfaction(),
                        customerTypeLabel(customer));
            }
        }
        showOrders();
        System.out.println(accent("+=====================================================================+", MAGENTA));
    }

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

    public void run() {
        welcome();
        try {
            while (!controller.isGameOver()) {
                controller.startTurn();
                displayState();

                while (true) {
                    WaiterChoice choice = getWaiterChoice();
                    String error = controller.executeWaiterChoice(choice);
                    if (error == null) {
                        showWaiterSuccess(choice);
                        displayState();
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
        } finally {
            input.close();
        }
    }

    private void welcome() {
        System.out.println(accent("+=====================================================================+", MAGENTA));
        System.out.println(accent("|                        RESTAURANT RUSH                              |", MAGENTA));
        System.out.println(accent("+=====================================================================+", MAGENTA));
        System.out.println("  18 turns | 2 tables | Waiter + Chef | automatic Cashier");
        System.out.println("  WIN: 4+ paid guests, $45.00+ revenue, 60+ average satisfaction");
        System.out.println("  GUESTS: REGULAR (-8 patience), VIP (-5, 10% off), CRITIC (-12)");
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
            Customer customer = state.findCustomer(order.getCustomerId());
            System.out.printf("    #%d C%d  %-7s %-24s  %-25s  prep left %d  %s%n",
                    order.getOrderId(), order.getCustomerId(), order.getItem().getName(),
                    orderPriceLabel(order), orderStatusLabel(order, state.getCurrentTurn()),
                    order.getRemainingUnits(), customer == null ? "" : customerTypeLabel(customer));
        }
    }

    private static String customerTypeLabel(Customer customer) {
        return "[" + customer.getCustomerTypeLabel().toUpperCase(Locale.ROOT) + "]";
    }

    private static String orderPriceLabel(Order order) {
        int menuPriceCents = order.getItem().getPriceCents();
        int chargedPriceCents = order.getFinalPriceCents();
        if (menuPriceCents == chargedPriceCents) {
            return money(chargedPriceCents);
        }
        return money(chargedPriceCents) + " (menu " + money(menuPriceCents) + ")";
    }

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
            case READY -> order.canBeServedOn(currentTurn)
                    ? "Ready to serve" : "Ready (serve next turn)";
            case SERVED -> "Meal delivered";
            case PAID -> "Bill paid";
            case CANCELLED -> "Cancelled";
        };
    }

    private void showWaiterSuccess(WaiterChoice choice) {
        if (choice instanceof SeatChoice seat) {
            Customer customer = controller.getGameState().findCustomer(seat.getCustomerId());
            success("Customer C" + seat.getCustomerId() + " seated at Table " + seat.getTableId()
                    + ". " + customerTypeLabel(customer));
        } else if (choice instanceof TakeOrderChoice takeOrder) {
            Table table = controller.getGameState().findTable(takeOrder.getTableId());
            Customer customer = table.getOccupant();
            Order order = customer.getOrder();
            success("Order #" + order.getOrderId() + ": C" + customer.getCustomerId()
                    + " ordered " + order.getItem().getName() + " ("
                    + orderPriceLabel(order) + "). " + customerTypeLabel(customer));
        } else if (choice instanceof ServeChoice serve) {
            success("Order #" + serve.getOrderId() + " delivered to the customer.");
        } else {
            success("Waiter waits this turn.");
        }
    }

    private void showChefSuccess(int orderId) {
        Order order = controller.getGameState().findOrder(orderId);
        if (order == null) {
            return;
        }
        if (order.getRemainingUnits() == 0) {
            success("Finished preparing " + order.getItem().getName()
                    + " (order #" + orderId + "). Ready for serving NEXT turn.");
        } else {
            success("Prepared one unit of " + order.getItem().getName() + " (order #"
                    + orderId + "). " + order.getRemainingUnits() + " unit(s) left.");
        }
    }

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
                        + " left unhappy! They ran out of patience. "
                        + customerTypeLabel(customer), RED));
            }
        }
        System.out.printf("  Total cash %s | Paid %d | Avg satisfaction %s | Walkouts %d%n",
                money(state.getRevenueCents()), state.getPaidCount(), average(state),
                state.getUnhappyDepartures());
    }

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
                : String.format(Locale.ROOT, "%.1f", state.getAveragePaidSatisfaction());
    }

    private static String accent(String message, String colour) {
        return System.getenv("NO_COLOR") == null ? colour + message + RESET : message;
    }

    private static final class QuitRequested extends RuntimeException { }
}
