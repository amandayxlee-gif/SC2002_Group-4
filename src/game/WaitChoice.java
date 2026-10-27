package game;

/** Waiter choice: do nothing this turn. No target. */
public class WaitChoice extends WaiterChoice {

    public WaitChoice() {
        super(WaiterAction.WAIT);
    }
}
