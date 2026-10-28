package game;

import customer.Customer;
import customer.CustomerStatus;
import customer.Order;
import customer.OrderStatus;
import menu.Menu;

public class ControllerFullFlowTest {

    public static void main(String[] args) {

        // Create the game
        GameController controller = new GameController(new Menu());
        GameState state = controller.getGameState();

        // =====================================
        // TURN 1: Customer arrives and is seated
        // =====================================

        controller.startTurn();

        Customer customer = state.getCustomers().get(0);

        String seatResult = controller.executeWaiterChoice(
            new SeatChoice(1, 1)
        );

        assert seatResult == null;
        assert customer.getTable() == state.getTables().get(0);

        controller.endTurn();

        // =====================================
        // TURN 2: Waiter takes customer's order
        // =====================================

        controller.startTurn();

        String takeOrderResult = controller.executeWaiterChoice(
            new TakeOrderChoice(1)
        );

        assert takeOrderResult == null;

        Order order = customer.getOrder();

        assert order != null;
        assert order.getStatus() == OrderStatus.PLACED;

        controller.endTurn();

        // =====================================
        // TURN 3: Chef prepares the food
        // =====================================

        controller.startTurn();

        String chefResult = controller.executeChefChoice(
            order.getOrderId()
        );

        System.out.println("Chef result: " + chefResult);
        System.out.println("Food: " + order.getItem().getName());
        System.out.println("Preparation units: "
            + order.getItem().getPreparationUnits());
        System.out.println("Remaining units: "
            + order.getRemainingUnits());
        System.out.println("Order status: " + order.getStatus());
        System.out.println("Ready turn: " + order.getReadyTurn());

        assert chefResult == null;
        assert order.getStatus() == OrderStatus.READY;
        assert order.getRemainingUnits() == 0;
        assert order.getReadyTurn() == 3;

        // Cannot serve food on the turn it becomes READY
        String earlyServeResult = controller.executeWaiterChoice(
            new ServeChoice(order.getOrderId())
        );

        assert earlyServeResult != null;

        controller.endTurn();

        // =====================================
        // TURN 4: Waiter serves the food
        // =====================================

        controller.startTurn();

        String serveResult = controller.executeWaiterChoice(
            new ServeChoice(order.getOrderId())
        );

        System.out.println("Serve result: " + serveResult);

        assert serveResult == null;
        assert customer.getStatus() == CustomerStatus.EATING;
        assert order.getStatus() == OrderStatus.SERVED;
        assert state.getServedCount() == 1;

        controller.endTurn();

        // =====================================
        // TURN 5: Customer finishes eating
        // =====================================

        controller.startTurn();
        controller.endTurn();

        assert customer.getStatus() == CustomerStatus.READY_TO_PAY;
        assert state.getPaidCount() == 0;

        // =====================================
        // TURN 6: Cashier processes payment
        // =====================================

        controller.startTurn();
        controller.endTurn();

        assert customer.getStatus() == CustomerStatus.PAID;
        assert order.getStatus() == OrderStatus.PAID;
        assert state.getPaidCount() == 1;

        // Salad costs $9 = 900 cents
        assert state.getRevenueCents() == 900;

        // Table becomes available again
        assert state.getTables().get(0).isAvailable();

        System.out.println("All Controller full-flow tests passed!");
    }
}