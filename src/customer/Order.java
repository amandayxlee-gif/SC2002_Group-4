package customer;

import menu.MenuItem;

/**
 * STUB (Person 2 scope): minimal order, just enough for GameState to hold
 * orders. Status-change rules (advance, serving/readyTurn restriction,
 * payment, cancellation) are NOT implemented here.
 */
public class Order {

    private final int orderId;
    private final int customerId;
    private final MenuItem item;
    private OrderStatus status;

    /**
     * customerId is recorded at creation as a plain immutable id (no live
     * back-reference to the Customer object), so the UI can display and sort
     * orders by customer and ServeChoice(orderId) can resolve ownership
     * without traversing customers.
     */
    public Order(int orderId, int customerId, MenuItem item) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.item = item;
        this.status = OrderStatus.PLACED;
    }

    public int getOrderId() {
        return orderId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public MenuItem getItem() {
        return item;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public int getFinalPriceCents() {
        return item.getPriceCents();
    }

    // ------------------------------------------------------------------
    // GAP: the status transitions below are TODOs (Person 2 scope).
    // ------------------------------------------------------------------

    /** TODO: one preparation unit done; becomes READY when the last is done; records readyTurn. */
    public void advance(int currentTurn) {
        // Intentionally empty.
    }

    /** TODO: READY -> SERVED; only valid when currentTurn > readyTurn. */
    public void markServed() {
        // Intentionally empty.
    }

    /** TODO: SERVED -> PAID. */
    public void markPaid() {
        // Intentionally empty.
    }

    /** TODO: -> CANCELLED (abandonment). */
    public void cancel() {
        // Intentionally empty.
    }
}
