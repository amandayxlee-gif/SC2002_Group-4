package game;

/** Waiter choice: take the order of the seated customer at this table. */
public class TakeOrderChoice extends WaiterChoice {

    private final int tableId;

    public TakeOrderChoice(int tableId) {
        super(WaiterAction.TAKE_ORDER);
        this.tableId = tableId;
    }

    public int getTableId() {
        return tableId;
    }
}
