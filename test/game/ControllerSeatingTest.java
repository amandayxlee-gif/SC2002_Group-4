package game;

import customer.Customer;
import customer.CustomerStatus;
import menu.Menu;
import staff.Table;

public class ControllerSeatingTest {
    public static void main(String[] args) {
        GameController controller = new GameController(new Menu());

        controller.startTurn(); // Turn 1: customer arrives

        GameState state = controller.getGameState();
        Customer customer = state.getWaitingList().get(0);

        String result = controller.executeWaiterChoice(
            new SeatChoice(customer.getCustomerId(), 1)
        );

        Table table = state.getTables().get(0);

        System.out.println("Result: " + result);
        System.out.println("Status: " + customer.getStatus());
        System.out.println("Waiting list size: " + state.getWaitingList().size());
        System.out.println("Table occupant: " + table.getOccupant());

        assert result == null;
        assert customer.getStatus() == CustomerStatus.SEATED;
        assert state.getWaitingList().isEmpty();
        assert table.getOccupant() == customer;

        System.out.println("Controller seating test passed!");
    }
}