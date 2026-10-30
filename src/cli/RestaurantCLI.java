package cli;

import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;

import customer.Customer;
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
 * Stage 1 Restaurant Rush console with three gamified themes.
 * Presentation and input belong here; ALL game rules belong to GameController.
 *
 * Preview with: java -cp out cli.RestaurantCLI arcade|tycoon|quest
 * Set NO_COLOR=1 in your terminal to disable ANSI colour codes.
 */
public class RestaurantCLI {

    // Choose the default appearance here (the three packaged files vary this).
    private static final Theme DEFAULT_THEME = Theme.ARCADE;

    private enum Theme { ARCADE, TYCOON, QUEST }

    private static final String RESET = "\u001B[0m";
    private static final String CYAN = "\u001B[96m";
    private static final String MAGENTA = "\u001B[95m";
    private static final String GREEN = "\u001B[92m";
    private static final String YELLOW = "\u001B[93m";
    private static final String RED = "\u001B[91m";

    private final GameController controller;
    private final Scanner input;
    private final Theme theme;

    public RestaurantCLI(GameController controller) {
        this(controller, DEFAULT_THEME);
    }

    private RestaurantCLI(GameController controller, Theme theme) {
        if (controller == null) {
            throw new IllegalArgumentException("GameController is required");
        }
        this.controller = controller;
        this.theme = theme;
        this.input = new Scanner(System.in);
    }

    /** Direct runnable entry point; the supplied cli.Main also delegates here. */
    public static void main(String[] args) {
        Theme selected = DEFAULT_THEME;
        if (args.length > 0) {
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "arcade": selected = Theme.ARCADE; break;
                case "tycoon": selected = Theme.TYCOON; break;
                case "quest": selected = Theme.QUEST; break;
                default:
                    System.out.println("Valid themes: arcade, tycoon, quest");
                    return;
            }
        }
        new RestaurantCLI(new GameController(new Menu()), selected).run();
    }

    public void displayState() {
        GameState state = controller.getGameState();
        switch (theme) {
            case ARCADE: showArcade(state); break;
            case TYCOON: showTycoon(state); break;
            case QUEST: showQuest(state); break;
            default: throw new IllegalStateException("Invalid console theme");
        }
    }

    /** A single Waiter command with an action-specific numeric target. */
    public WaiterChoice getWaiterChoice() {
        while (true) {
            showWaiterMenu();
            int action = readInt("WAITER action [1-4; Q=quit] > ");
            switch (action) {
                case 1:
                    return new SeatChoice(
                            readInt("Waiting customer ID > "),
                            readInt("Free table ID > "));
                case 2:
                    return new TakeOrderChoice(readInt("Occupied table ID > "));
                case 3:
                    showOrders();
                    return new ServeChoice(readInt("READY order ID > "));
                case 4:
                    return new WaitChoice();
                default:
                    displayError("Choose 1, 2, 3, or 4.");
            }
        }
    }

    /** Return -1 to skip Chef preparation; other integers are order IDs. */
    public int getChefOrderChoice() {
        System.out.println(accent("\n  [CHEF STATION]", CYAN));
        showOrders();
        System.out.println("  One preparation unit per turn. Enter 0 to wait.");
        int id = readInt("CHEF order ID [0=wait; Q=quit] > ");
        return id == 0 ? -1 : id;
    }

    public void displayError(String message) {
        System.out.println(accent("  [REJECTED] " + message, RED));
        System.out.println("  No staff task was consumed. Choose again.");
    }

    public void displayResult(GameResult result) {
        GameState state = controller.getGameState();
        System.out.println();
        System.out.println("+====================================================================+");
        System.out.println(accent("            FINAL SERVICE REPORT  /  " + result,
                result == GameResult.VICTORY ? GREEN : RED));
        System.out.println("+====================================================================+");
        System.out.println("  Result: " + result);
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
        System.out.println("+====================================================================+\n");
    }

    /**
     * Same validated sequence as the group's Stage 1 controller tests:
     * arrival/start -> display -> Waiter -> Chef -> automatic end-of-turn.
     */
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
                        successful("Waiter: " + choice.getAction() + " completed");
                        break;
                    }
                    displayError(error);
                }

                // Chef sees any order the Waiter just placed this turn.
                while (true) {
                    int orderId = getChefOrderChoice();
                    if (orderId == -1) {
                        successful("Chef: waiting this turn");
                        break;
                    }
                    String error = controller.executeChefChoice(orderId);
                    if (error == null) {
                        successful("Chef: prepared one unit for order #" + orderId);
                        break;
                    }
                    displayError(error);
                }

                controller.endTurn();
                showEndTurn();
            }
            displayResult(controller.checkResult());
        } catch (QuitRequested e) {
            System.out.println("\nPlayer quit; game not completed. Thanks for playing!");
        } catch (NoSuchElementException e) {
            System.out.println("\nInput ended; game not completed.");
        }
    }

    private void welcome() {
        switch (theme) {
            case ARCADE:
                System.out.println(accent("+====================================================================+", MAGENTA));
                System.out.println(accent("|  R E S T A U R A N T   R U S H   //   A R C A D E   H U D   |", MAGENTA));
                System.out.println(accent("+====================================================================+", MAGENTA));
                break;
            case TYCOON:
                System.out.println(accent("+--------------------------------------------------------------------+", CYAN));
                System.out.println(accent("|                   RESTAURANT RUSH  TYCOON                         |", CYAN));
                System.out.println(accent("|                   YOUR VERY OWN DINING FLOOR                      |", CYAN));
                System.out.println(accent("+--------------------------------------------------------------------+", CYAN));
                break;
            case QUEST:
                System.out.println(accent("  ===============================================", YELLOW));
                System.out.println(accent("       RESTAURANT RUSH // SERVICE QUEST", YELLOW));
                System.out.println(accent("  ===============================================", YELLOW));
                break;
            default: break;
        }
        System.out.println("  18 turns | 2 tables | Waiter + Chef | automatic Cashier");
        System.out.println("  WIN: 4+ paid guests, $45.00+ revenue, 60+ average satisfaction");
        System.out.println("  Q quits at any input prompt.\n");
    }

    private void showArcade(GameState s) {
        System.out.println(accent("\n+==============================[ RESTAURANT RUSH ]==========================+", MAGENTA));
        System.out.printf("  LEVEL %02d / %02d    SHIFT %s%n", s.getCurrentTurn(),
                GameState.TOTAL_TURNS, meter(s.getCurrentTurn(), GameState.TOTAL_TURNS, 18));
        System.out.printf("  CASH %s     PAID %d     SERVED %d     WALKOUTS %d%n",
                money(s.getRevenueCents()), s.getPaidCount(), s.getServedCount(), s.getUnhappyDepartures());
        System.out.println(accent("  +-- VICTORY METERS ------------------------------------------------+", CYAN));
        System.out.printf("  | GUESTS  %s  %d / 4 paid%n", meter(s.getPaidCount(), 4, 12), s.getPaidCount());
        System.out.printf("  | SALES   %s  %s / $45.00%n",
                meter(s.getRevenueCents(), 4500, 12), money(s.getRevenueCents()));
        System.out.printf("  | HAPPY   %s  avg %s / 60%n",
                s.getPaidCount() == 0 ? meter(0, 60, 12) :
                        meter((int) s.getAverageSatisfaction(), 60, 12), average(s));
        System.out.println("  +-----------------------------------------------------------------+");
        System.out.println(accent("  >>> CUSTOMER PATIENCE <<<", YELLOW));
        queueWithMeters(s);
        System.out.println(accent("  >>> TABLE STATUS <<<", CYAN));
        for (Table table : s.getTables()) {
            System.out.printf("    T%d: %s%n", table.getTableId(), tableLabel(table));
        }
        showOrders();
        System.out.println(accent("+====================================================================+", MAGENTA));
    }

    private void showTycoon(GameState s) {
        System.out.println(accent("\n+=======================[ RESTAURANT TYCOON ]=========================+", CYAN));
        System.out.printf("  SHIFT %02d/%02d   SALES %-7s   PAID %-2d   GUEST SAT %s%n",
                s.getCurrentTurn(), GameState.TOTAL_TURNS,
                money(s.getRevenueCents()), s.getPaidCount(), average(s));
        System.out.println("  +---------------------------- DINING FLOOR -----------------------+");
        System.out.println("  |                           ENTRANCE                             |");
        System.out.println("  |                         [  QUEUE  ]                            |");
        System.out.println("  |                                                               |");
        System.out.println("  |    +----------------------+    +----------------------+         |");
        Table first = s.getTables().get(0);
        Table second = s.getTables().get(1);
        System.out.printf("  |    | TABLE %-2d             |    | TABLE %-2d             |         |%n",
                first.getTableId(), second.getTableId());
        System.out.printf("  |    | %-20s |    | %-20s |         |%n",
                roomLabel(first), roomLabel(second));
        System.out.println("  |    +----------------------+    +----------------------+         |");
        System.out.println("  |                                                               |");
        System.out.println("  |                        [  KITCHEN  ]                           |");
        System.out.println("  +---------------------------------------------------------------+");
        System.out.println(accent("  GUESTS WAITING OUTSIDE", YELLOW));
        queueWithMeters(s);
        showOrders();
        System.out.printf("  BUSINESS GOALS: paid %d/4  |  revenue %s/$45  |  avg %s/60%n",
                s.getPaidCount(), money(s.getRevenueCents()), average(s));
        System.out.println(accent("+====================================================================+", CYAN));
    }

    private void showQuest(GameState s) {
        System.out.println(accent("\n+==================[ SERVICE QUEST : STAGE 1 ]=========================+", YELLOW));
        System.out.printf("  MISSION TIMER: TURN %02d/%02d  %s%n", s.getCurrentTurn(),
                GameState.TOTAL_TURNS, meter(s.getCurrentTurn(), GameState.TOTAL_TURNS, 18));
        System.out.println(accent("  MAIN OBJECTIVES  (progress so far; checked at turn 18)", MAGENTA));
        System.out.printf("    %s Pay 4 guests       %d/4   %s%n", checkmark(s.getPaidCount() >= 4),
                s.getPaidCount(), meter(s.getPaidCount(), 4, 10));
        System.out.printf("    %s Earn $45 revenue   %s/$45  %s%n", checkmark(s.getRevenueCents() >= 4500),
                money(s.getRevenueCents()), meter(s.getRevenueCents(), 4500, 10));
        System.out.printf("    %s Average 60+ happy  %s/60  %s%n",
                checkmark(s.getPaidCount() > 0 && s.getAverageSatisfaction() >= 60),
                average(s), s.getPaidCount() == 0 ? meter(0, 60, 10) :
                        meter((int) s.getAverageSatisfaction(), 60, 10));
        System.out.println("  ---------------------------------------------------------------");
        System.out.printf("  SERVICE LOG  | Served %d | Paid %d | Walkouts %d%n",
                s.getServedCount(), s.getPaidCount(), s.getUnhappyDepartures());
        System.out.println(accent("  ACTIVE GUESTS / PATIENCE", YELLOW));
        queueWithMeters(s);
        System.out.println(accent("  TABLE CHECKPOINTS", CYAN));
        for (Table table : s.getTables()) {
            System.out.printf("    [T%d] %s%n", table.getTableId(), tableLabel(table));
        }
        showOrders();
        System.out.println(accent("+====================================================================+", YELLOW));
    }

    private void queueWithMeters(GameState s) {
        if (s.getWaitingList().isEmpty()) {
            System.out.println("    No guests waiting.");
        }
        for (Customer c : s.getWaitingList()) {
            String line = String.format(Locale.ROOT, "    C%-2d SAT %3d/100 %s",
                    c.getCustomerId(), c.getSatisfaction(), meter(c.getSatisfaction(), 100, 10));
            System.out.println(c.getSatisfaction() <= 32 ? accent(line, RED) : line);
        }
    }

    private void showOrders() {
        GameState s = controller.getGameState();
        System.out.println(accent("  KITCHEN ORDERS", CYAN));
        if (s.getOrders().isEmpty()) {
            System.out.println("    No orders have been placed yet.");
        }
        for (Order o : s.getOrders()) {
            System.out.printf("    #%d C%d  %-7s  %-10s  prep left %d%n",
                    o.getOrderId(), o.getCustomerId(), o.getItem().getName(),
                    o.getStatus(), o.getRemainingUnits());
        }
    }

    private void showWaiterMenu() {
        switch (theme) {
            case ARCADE:
                System.out.println(accent("\n  [NEXT MOVE] WAITER SELECT", MAGENTA));
                System.out.println("    [1] SEAT GUEST        [2] TAKE ORDER");
                System.out.println("    [3] DELIVER FOOD      [4] SKIP ACTION");
                break;
            case TYCOON:
                System.out.println(accent("\n  [OPERATIONS PANEL]", CYAN));
                System.out.println("    1  Seat guest         2  Take order");
                System.out.println("    3  Serve ready meal   4  Wait");
                break;
            case QUEST:
                System.out.println(accent("\n  [CHOOSE A SERVICE QUEST]", YELLOW));
                System.out.println("    1  Check in customer  2  Collect table order");
                System.out.println("    3  Deliver ready food 4  Hold position");
                break;
            default: break;
        }
    }

    private void showEndTurn() {
        GameState s = controller.getGameState();
        System.out.println(accent("  --- TURN CLEARED: AUTOMATIC STAFF PROCESSED ---", GREEN));
        System.out.printf("  Cash %s | Paid %d | Avg sat %s | Walkouts %d%n",
                money(s.getRevenueCents()), s.getPaidCount(), average(s), s.getUnhappyDepartures());
    }

    private void successful(String message) {
        System.out.println(accent("  [OK] " + message, GREEN));
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!input.hasNextLine()) throw new NoSuchElementException("Input closed");
            String line = input.nextLine().trim();
            if (line.equalsIgnoreCase("q")) throw new QuitRequested();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                displayError("Enter a whole number, or Q to quit.");
            }
        }
    }

    private static String roomLabel(Table t) {
        if (t.isAvailable()) return "[ OPEN ]";
        Customer c = t.getOccupant();
        return "C" + c.getCustomerId() + " / " + c.getStatus();
    }

    private static String tableLabel(Table t) {
        if (t.isAvailable()) return "FREE / READY FOR GUEST";
        Customer c = t.getOccupant();
        return "C" + c.getCustomerId() + " | " + c.getStatus() + " | sat " + c.getSatisfaction();
    }

    private static String checkmark(boolean done) {
        return done ? "[X]" : "[ ]";
    }

    /** Display-only goal meter; never mutates GameState. */
    private static String meter(int value, int target, int width) {
        int filled = Math.min(width, Math.max(0, (int) ((long) value * width / target)));
        return "[" + "#".repeat(filled) + "-".repeat(width - filled) + "]";
    }

    private static String money(int cents) {
        return String.format(Locale.ROOT, "$%.2f", cents / 100.0);
    }

    private static String average(GameState s) {
        if (s.getPaidCount() == 0) return "N/A";
        return String.format(Locale.ROOT, "%.1f", s.getAverageSatisfaction());
    }

    /** VS Code terminals support ANSI; NO_COLOR=1 offers plain-text output. */
    private static String accent(String message, String colour) {
        return System.getenv("NO_COLOR") == null ? colour + message + RESET : message;
    }

    private static class QuitRequested extends RuntimeException { }
}
