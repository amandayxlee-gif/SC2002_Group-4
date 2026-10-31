package game;

/**
 * Abstract base for one Waiter decision. Each concrete choice carries only
 * the fields it needs, avoiding an ambiguous generic target id
 */
public abstract class WaiterChoice {

    private final WaiterAction action;

    protected WaiterChoice(WaiterAction action) {
        this.action = action;
    }

    public WaiterAction getAction() {
        return action;
    }
}
