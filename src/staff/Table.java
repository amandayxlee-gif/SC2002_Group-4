package staff;

import customer.Customer;

/**
 * Stores a table's ID and current customer.
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
