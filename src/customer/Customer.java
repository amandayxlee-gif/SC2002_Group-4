package customer;

import java.util.List;
import menu.MenuItem;
import staff.Table;

public class Customer {

    /** Satisfaction starts at 100 and can never exceed it. */
    public static final int MAX_SATISFACTION = 100;

    /** Regular customers lose this many satisfaction points per waiting or seated turn. */
    public static final int DEFAULT_SATISFACTION_LOSS_PER_TURN = 8;

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

    /** A customer can pay only on a turn after they finished eating. */
    public boolean canPayOn(int currentTurn) {
        return status == CustomerStatus.READY_TO_PAY && currentTurn > readyToPayTurn;
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

    /** Assigns an order and locks in this customer's final bill at placement. */
    public void assignOrder(Order order) {
        this.order = order;
        order.setFinalPriceCents(calculateBillCents());
    }

    /**
     * Waiting or seated customers who have not been served lose satisfaction
     * (floor 0). Subclasses override this with their own loss rate.
     */
    public void decreaseSatisfaction() {
        if (status == CustomerStatus.WAITING || status == CustomerStatus.SEATED) {
            satisfaction = Math.max(0, satisfaction - DEFAULT_SATISFACTION_LOSS_PER_TURN);
        }
    }

    /**
     * Increases satisfaction during Happy Hour.
     * Satisfaction cannot exceed the maximum of 100.
     */
    public void increaseSatisfaction(int amount) {

        if (amount < 0) {
            throw new IllegalArgumentException(
                "Satisfaction recovery cannot be negative"
            );
        }

        satisfaction = Math.min(
            MAX_SATISFACTION,
            satisfaction + amount
        );
    }

    /** Leaves unhappy: frees the table and cancels any unpaid order. */
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

    public int calculateBillCents() {
        if (order == null)
            return 0;
        return order.getItem().getPriceCents();
    }

    /** Deterministic choice rule: menu items are picked in rotation by customer ID. */
    public MenuItem chooseMenuItem(List<MenuItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("Menu is empty");
        }
        int index = Math.floorMod(customerId - 1, items.size());
        return items.get(index);
    }

    public String getCustomerTypeLabel() {
        return "Regular";
    }

    @Override
    public String toString() {
        return getCustomerTypeLabel() + " Customer #" + customerId;
    }
}
