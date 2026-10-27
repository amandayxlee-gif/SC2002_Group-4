package game;

/** Waiter choice: serve the READY order with this id. */
public class ServeChoice extends WaiterChoice {

    private final int orderId;

    public ServeChoice(int orderId) {
        super(WaiterAction.SERVE);
        this.orderId = orderId;
    }

    public int getOrderId() {
        return orderId;
    }
}
