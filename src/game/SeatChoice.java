package game;

/** Waiter choice: seat the queued customer with this id at the table with this id. */
public class SeatChoice extends WaiterChoice {

    private final int customerId;
    private final int tableId;

    public SeatChoice(int customerId, int tableId) {
        super(WaiterAction.SEAT);
        this.customerId = customerId;
        this.tableId = tableId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public int getTableId() {
        return tableId;
    }
}
