package customer;

import menu.MenuItem;

public class Order {

    private final int orderId;
    private final int customerId;
    private final MenuItem item;
    private OrderStatus status;
    private int remainingUnits;
    private int finalPriceCents;
    private int readyTurn = -1;

    public Order(int orderId, int customerId, MenuItem item) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.item = item;
        this.status = OrderStatus.PLACED;
        this.remainingUnits = item.getPreparationUnits();
        this.finalPriceCents = item.getPriceCents();
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

    public int getRemainingUnits() {
        return remainingUnits;
    }

    public int getFinalPriceCents() {
        return finalPriceCents;
    }

    public int getReadyTurn() {
        return readyTurn;
    }

    /**
     * Prepares one unit. The order becomes READY when no units remain.
     */
    public void advance(int currentTurn) {
        if (status == OrderStatus.PLACED) {
            status = OrderStatus.PREPARING;
        }
        if (remainingUnits > 0) {
            remainingUnits--;
        }
        if (remainingUnits == 0 && status != OrderStatus.READY) {
            status = OrderStatus.READY;
            readyTurn = currentTurn;
        }
    }

    public void markServed() {
        this.status = OrderStatus.SERVED;
    }

    public void markPaid() {
        this.status = OrderStatus.PAID;
    }

    public void cancel() {
        this.status = OrderStatus.CANCELLED;
    }
}
