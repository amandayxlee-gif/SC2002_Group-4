package customer;

import staff.Table;

public class Customer {

    private final int customerId;
    private int satisfaction;
    private CustomerStatus status;
    private Table table;
    private Order order;
    private int servedTurn = -1;
    private int readyToPayTurn = -1;

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

    /**
     * Lifecycle hook: marks the customer as having left unhappy. Status
     * changes only through these hooks — there is deliberately no public
     * status setter — so Stage 2 subclasses can override the hooks.
     */
    public void leave() {
        this.status = CustomerStatus.LEFT;
    }

    public Table getTable() {
        return table;
    }

    public Order getOrder() {
        return order;
    }

    public int getServedTurn() {
        return servedTurn;
    }

    public int getReadyToPayTurn() {
        return readyToPayTurn;
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

    public void assignOrder(Order order) {
        this.order = order;
    }

    /**
     * Waiting or seated customers lose 8 satisfaction points per turn (minimum 0).

     */
    public void decreaseSatisfaction() {
        if (status == CustomerStatus.WAITING || status == CustomerStatus.SEATED) {
            satisfaction = Math.max(0, satisfaction - 8);
        }
    }

    public boolean hasAbandoned() {
        return satisfaction == 0 && (status == CustomerStatus.WAITING || status == CustomerStatus.SEATED);
    }

    public void onServed(int currentTurn) {
        this.servedTurn = currentTurn;
        this.status = CustomerStatus.EATING;
    }

    public void finishEating(int currentTurn) {
        this.status = CustomerStatus.READY_TO_PAY;
        this.readyToPayTurn = currentTurn;
    }

    public void onPaid() {
        this.status = CustomerStatus.PAID;
    }
}
