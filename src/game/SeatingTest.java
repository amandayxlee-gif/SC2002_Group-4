package game;

import customer.Customer;
import customer.CustomerStatus;
import staff.Table;

public class SeatingTest {
    public static void main(String[] args) {
        GameState state = new GameState();
        Customer customer = new Customer(1);
        Table table = state.getAvailableTable();

        state.addCustomer(customer);
        customer.seatAt(table);
        state.removeWaitingCustomer(customer);

        assert customer.getStatus() == CustomerStatus.SEATED;
        assert table.getOccupant() == customer;
        assert customer.getTable() == table;
        assert state.getWaitingList().isEmpty();

        System.out.println("All seating tests passed!");
    }
}