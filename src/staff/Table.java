package staff;

import customer.Customer;

/**
 * STUB (Person 3 scope): a table in the base setting. Kept functional
 * (not a TODO shell) because GameState owns the two tables and the seat /
 * free / availability checks are simple state, needed by the turn flow.
 */
public class Table {

    private final int tableId;
    private Customer occupant;

    public Table(int tableId) {
        this.tableId = tableId;
    }

    public int getTableId() {
        return tableId;
    }

    public Customer getOccupant() {
        return occupant;
    }

    public boolean isAvailable() {
        return occupant == null;
    }

    public void occupy(Customer customer) {
        this.occupant = customer;
    }

    public void free() {
        this.occupant = null;
    }
}
