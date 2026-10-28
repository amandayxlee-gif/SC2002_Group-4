package game;

import customer.Customer;
import customer.Order;
import customer.OrderStatus;
import menu.Menu;

public class ControllerTakeOrderTest {

    public static void main(String[] args) {

        // Create the game
        Menu menu = new Menu();
        GameController controller = new GameController(menu);

        controller.startGame();

        // TURN 1: Customer 1 arrives
        controller.startTurn();

        GameState state = controller.getGameState();

        assert state.getCurrentTurn() == 1;
        assert state.getWaitingList().size() == 1;

        Customer customer = state.getCustomers().get(0);

        // Seat customer 1 at table 1
        String seatResult = controller.executeWaiterChoice(
            new SeatChoice(1, 1)
        );

        assert seatResult == null;
        assert customer.getTable() == state.getTables().get(0);

        // End turn 1
        controller.endTurn();

        // TURN 2: Take customer's order
        controller.startTurn();

        String orderResult = controller.executeWaiterChoice(
            new TakeOrderChoice(1)
        );

        assert orderResult == null;
        assert customer.getOrder() != null;

        // Order should be registered in GameState
        assert state.getOrders().size() == 1;

        Order order = state.getOrders().get(0);

        assert order.getOrderId() == 1;
        assert order.getCustomerId() == 1;
        assert order.getStatus() == OrderStatus.PLACED;

        // Customer 1 should select Salad
        assert order.getItem().getName().equals("Salad");

        // Taking another order for the same customer must fail
        String duplicateResult = controller.executeWaiterChoice(
            new TakeOrderChoice(1)
        );

        assert duplicateResult != null;
        assert state.getOrders().size() == 1;

        System.out.println("All Controller TAKE_ORDER tests passed!");
    }
}