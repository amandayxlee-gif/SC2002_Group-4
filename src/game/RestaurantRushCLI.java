package game;

import java.util.Scanner;
import java.util.NoSuchElementException;

import customer.Customer;
import customer.Order;
import menu.Menu;
import staff.Table;

public class RestaurantRushCLI {

    private final Scanner scanner = new Scanner(System.in);
    private final GameController controller =
            new GameController(new Menu());

    public static void main(String[] args) {
        new RestaurantRushCLI().play();
    }

    public void play() {
        System.out.println("================================");
        System.out.println("        RESTAURANT RUSH");
        System.out.println("================================");
        System.out.println("Manage your restaurant for 18 turns!");
        System.out.println("Type Q at any prompt to quit.");

        try {
            controller.startGame();

            while (!controller.isGameOver()) {
                controller.startTurn();

                System.out.println("\n==============================");
                System.out.println("TURN "
                        + controller.getGameState().getCurrentTurn());
                System.out.println("==============================");

                // Required start-of-turn statistics
                showStats();

                // Required restaurant state
                showRestaurant();

                // Waiter acts before Chef
                chooseWaiterAction();
                chooseChefAction();

                controller.endTurn();

                System.out.println("\n--- END OF TURN ---");
                showStats();
            }

            System.out.println("\n==============================");
            System.out.println("           GAME OVER");
            System.out.println("==============================");

            GameResult result = controller.checkResult();

            System.out.println("Result: " + result);
            showStats();

            if (result == GameResult.DEFEAT) {
                System.out.println("\nReasons for defeat:");

                for (String reason :
                        controller.getGameState().getDefeatCauses()) {
                    System.out.println("- " + reason);
                }
            }

        } catch (QuitGameException e) {
            System.out.println(
                    "\nExiting Restaurant Rush. Goodbye!");

        } catch (NoSuchElementException e) {
            System.out.println(
                    "\nInput stream closed. Exiting Restaurant Rush.");

        } finally {
            scanner.close();
        }
    }

    private void showRestaurant() {
        GameState state = controller.getGameState();

        System.out.println("\n--- WAITING CUSTOMERS ---");

        if (state.getWaitingList().isEmpty()) {
            System.out.println("None");
        } else {
            for (Customer customer : state.getWaitingList()) {
                System.out.println(
                        "Customer " + customer.getCustomerId()
                        + " | Satisfaction: "
                        + customer.getSatisfaction());
            }
        }

        System.out.println("\n--- TABLES ---");

        for (Table table : state.getTables()) {
            if (table.isAvailable()) {
                System.out.println(
                        "Table " + table.getTableId() + ": EMPTY");
            } else {
                Customer customer = table.getOccupant();

                System.out.println(
                        "Table " + table.getTableId()
                        + ": Customer " + customer.getCustomerId()
                        + " | " + customer.getStatus()
                        + " | Satisfaction: "
                        + customer.getSatisfaction());
            }
        }

        showOrders();
    }

    private void showOrders() {
        GameState state = controller.getGameState();

        System.out.println("\n--- CURRENT ORDERS ---");

        if (state.getOrders().isEmpty()) {
            System.out.println("No orders yet.");
            return;
        }

        for (Order order : state.getOrders()) {
            System.out.println(
                    "Order " + order.getOrderId()
                    + " | Customer " + order.getCustomerId()
                    + " | " + order.getItem().getName()
                    + " | Status: " + order.getStatus()
                    + " | Remaining prep: "
                    + order.getRemainingUnits());
        }
    }

    private void chooseWaiterAction() {
        while (true) {
            System.out.println("\n--- WAITER ACTION ---");
            System.out.println("1. Seat customer");
            System.out.println("2. Take order");
            System.out.println("3. Serve food");
            System.out.println("4. Wait");
            System.out.println("Q. Quit game");

            int choice = readInt("Choose action: ");
            WaiterChoice action;

            switch (choice) {
                case 1:
                    int customerId = readInt("Customer ID: ");
                    int tableId = readInt("Table ID: ");

                    action = new SeatChoice(customerId, tableId);
                    break;

                case 2:
                    int orderTableId = readInt("Table ID: ");

                    action = new TakeOrderChoice(orderTableId);
                    break;

                case 3:
                    showOrders();

                    int orderId = readInt("Order ID: ");

                    action = new ServeChoice(orderId);
                    break;

                case 4:
                    action = new WaitChoice();
                    break;

                default:
                    System.out.println(
                            "Invalid action. Please try again.");
                    continue;
            }

            String result =
                    controller.executeWaiterChoice(action);

            if (result == null) {
                System.out.println("Waiter action successful!");
                return;
            }

            System.out.println("Action rejected: " + result);
            System.out.println("Please choose another action.");
        }
    }

    private void chooseChefAction() {
        while (true) {
            // Display newly created orders after Waiter acts
            showOrders();

            System.out.println("\n--- CHEF ACTION ---");
            System.out.println("1. Prepare order");
            System.out.println("2. Wait");
            System.out.println("Q. Quit game");

            int choice = readInt("Choose action: ");

            if (choice == 2) {
                System.out.println("Chef waits this turn.");
                return;
            }

            if (choice != 1) {
                System.out.println(
                        "Invalid action. Please try again.");
                continue;
            }

            int orderId = readInt("Order ID: ");

            String result =
                    controller.executeChefChoice(orderId);

            if (result == null) {
                System.out.println(
                        "Chef successfully prepared one unit!");
                return;
            }

            System.out.println("Action rejected: " + result);
            System.out.println("Please choose another action.");
        }
    }

    private void showStats() {
        GameState state = controller.getGameState();

        System.out.printf(
                "Revenue: $%.2f%n",
                state.getRevenueCents() / 100.0);

        System.out.println(
                "Paid customers: " + state.getPaidCount());

        System.out.println(
                "Served customers: " + state.getServedCount());

        if (state.getPaidCount() == 0) {
            System.out.println("Average satisfaction: N/A");
        } else {
            System.out.printf(
                    "Average satisfaction: %.2f%n",
                    state.getAverageSatisfaction());
        }

        System.out.println(
                "Unhappy departures: "
                + state.getUnhappyDepartures());
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);

            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("q")) {
                throw new QuitGameException();
            }

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println(
                        "Please enter a whole number or Q to quit.");
            }
        }
    }

    private static class QuitGameException
            extends RuntimeException {
    }
}