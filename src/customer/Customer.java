package customer;

import staff.Table;

public class Customer {

    public static final int MAX_SATISFACTION = 100;

    private final int customerId;
    protected int satisfaction;
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

    public void setStatus(CustomerStatus status) {
        this.status = status;
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

    public boolean canPayOn(int currentTurn) {
        return status == CustomerStatus.READY_TO_PAY;
    }

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

    /** Standard customer loses 8 satisfaction points per waiting/seated turn. */
    public void decreaseSatisfaction() {
        if (status == CustomerStatus.WAITING || status == CustomerStatus.SEATED) {
            satisfaction = Math.max(0, satisfaction - 8);
        }
    }

    /** Lifecycle method when a customer leaves or abandons. */
    public void leave() {
        if (table != null) {
            table.free();
            table = null;
        }
        if (order != null && order.getStatus() != OrderStatus.PAID) {
            order.cancel();
        }
        this.status = CustomerStatus.LEFT;
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

    /** Calculates bill in cents (Standard customer pays 100%). */
    public int calculateBillCents() {
        if (order == null)
            return 0;
        return order.getFinalPriceCents();
    }

    public String getCustomerTypeLabel() {
        return "Regular";
    }

    @Override
    public String toString() {
        return getCustomerTypeLabel() + " Customer #" + customerId;
    }
}