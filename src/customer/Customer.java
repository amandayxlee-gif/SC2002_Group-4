package customer;

import staff.Table;

public class Customer {

    /** Satisfaction starts at 100 and can never exceed it (assignment doc section 3.2). */
    public static final int MAX_SATISFACTION = 100;

    /**
     * Regular-customer default: waiting or seated customers who have not been
     * served lose 8 satisfaction points per turn (assignment doc section 3.2, step 5).
     */
    public static final int DEFAULT_SATISFACTION_LOSS_PER_TURN = 8;

    private final int customerId;
    private int satisfaction;
    private CustomerStatus status;
    private Table table;
    private Order order;
    private int servedTurn = -1;
    private int readyToPayTurn = -1;

    public Customer(int customerId) {
        this.customerId = customerId;
        this.satisfaction = MAX_SATISFACTION;
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
     * Lifecycle hook: marks the customer as having left unhappy. 
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
     * Waiting or seated customers lose satisfaction each turn (minimum 0).
     */
    public void decreaseSatisfaction() {
        if (status == CustomerStatus.WAITING || status == CustomerStatus.SEATED) {
            satisfaction = Math.max(0, satisfaction - DEFAULT_SATISFACTION_LOSS_PER_TURN);
        }
    }

    /** A customer can pay only on a turn after they finished eating. */
    public boolean canPayOn(int turn) {
        return status == CustomerStatus.READY_TO_PAY && turn > readyToPayTurn;
    }

    public boolean hasAbandoned() {
        return satisfaction <= 0 && (status == CustomerStatus.WAITING || status == CustomerStatus.SEATED);
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
