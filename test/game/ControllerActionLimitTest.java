package game;

import customer.Customer;
import customer.Order;
import customer.OrderStatus;
import menu.Menu;

public class ControllerActionLimitTest {

    public static void main(String[] args) {

        GameController controller = new GameController(new Menu());
        GameState state = controller.getGameState();

        // TURN 1
        controller.startTurn();

        // Invalid action should not consume Waiter's turn
        String invalidSeat = controller.executeWaiterChoice(
            new SeatChoice(1, 99)
        );

        assert invalidSeat != null;

        // First valid Waiter action succeeds
        String firstSeat = controller.executeWaiterChoice(
            new SeatChoice(1, 1)
        );

        assert firstSeat == null;

        // Second Waiter action in same turn must fail
        String secondSeat = controller.executeWaiterChoice(
            new SeatChoice(1, 2)
        );

        assert secondSeat != null;
        assert secondSeat.equals("Waiter has already acted this turn");

        controller.endTurn();

        // TURN 2
        controller.startTurn();

        // Waiter action resets on a new turn
        String takeOrder = controller.executeWaiterChoice(
            new TakeOrderChoice(1)
        );

        assert takeOrder == null;

        Customer customer = state.getCustomers().get(0);
        Order order = customer.getOrder();

        assert order != null;

        // Invalid Chef action should not consume Chef's turn
        String invalidChef = controller.executeChefChoice(999);

        assert invalidChef != null;

        // First valid Chef action succeeds
        String firstChef = controller.executeChefChoice(
            order.getOrderId()
        );

        assert firstChef == null;
        assert order.getStatus() == OrderStatus.READY;

        // Second Chef action must fail
        String secondChef = controller.executeChefChoice(
            order.getOrderId()
        );

        assert secondChef != null;
        assert secondChef.equals("Chef has already acted this turn");

        controller.endTurn();

        System.out.println("All Controller action-limit tests passed!");
    }
}