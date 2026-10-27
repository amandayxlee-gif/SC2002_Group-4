package customer;

import staff.Table;

public class Customer {

    private final int customerId;
    private int satisfaction;
    private CustomerStatus status;
    private Table table;

    public Customer(int customerId) {
        this.customerId = customerId;
        this.satisfaction = 100;
        this.status = CustomerStatus.WAITING;
    }

    public int getCustomerId() {
        return customerId;
    }

    public int getSatisfaction() {
        return satisfaction;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    public Table getTable() {
        return table;
    }

    /** Called when the Waiter seats this customer. */
    public void seatAt(Table table) {
        if (status != CustomerStatus.WAITING) {
            throw new IllegalStateException("Customer is not waiting");
        }
        if (table == null || !table.isAvailable()) {
            throw new IllegalArgumentException("Table is unavailable");
        }

        this.table = table;
        this.status = CustomerStatus.SEATED;
        table.occupy(this);
    }

    // Remaining lifecycle methods are Stage 1 TODOs.

    public void assignOrder(Order order) {
        // TODO
    }

    public void decreaseSatisfaction() {
        // TODO
    }

    public boolean hasAbandoned() {
        return false; // TODO
    }

    public void onServed(int currentTurn) {
        // TODO
    }

    public void finishEating(int currentTurn) {
        // TODO
    }

    public void onPaid() {
        // TODO
    }
}