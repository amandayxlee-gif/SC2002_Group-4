package game;

/**
 * Happy Hour pricing policy (docx section 4.3).
 *
 * Can be activated once per game, at the start of a turn. It lasts for that
 * turn and the next. Orders taken while it is active get 20% off their bill;
 * customers still waiting for a table or food recover 15 satisfaction at
 * turn start. Prices are stored as integer cents (docx section 2.2).
 */
public class HappyHour {

    private static final int DURATION = 2;
    private static final int DISCOUNT_PERCENT = 20;
    private static final int SATISFACTION_RECOVERY = 15;

    private boolean used = false;
    private int startTurn = -1;

    // Activates Happy Hour once per game, for the current and next turn
    public boolean activate(int currentTurn) {
        if (used || currentTurn < 1 || currentTurn > GameState.TOTAL_TURNS) {
            return false;
        }
        used = true;
        startTurn = currentTurn;
        return true;
    }

    /** True while Happy Hour is running on the given turn. */
    public boolean isActive(int currentTurn) {
        return used
                && currentTurn >= startTurn
                && currentTurn < startTurn + DURATION;
    }

    /** Turns of Happy Hour left, including the given turn; 0 when inactive. */
    public int getRemainingTurns(int currentTurn) {
        if (!isActive(currentTurn)) {
            return 0;
        }
        return startTurn + DURATION - currentTurn;
    }

    /** Applies discount (20%), rounded half-up to the nearest cent. */
    public int applyDiscount(int priceCents, int currentTurn) {
        if (!isActive(currentTurn)) {
            return priceCents;
        }
        return (priceCents * (100 - DISCOUNT_PERCENT) + 50) / 100;
    }

    /** Satisfaction points recovered*/
    public int getSatisfactionRecovery() {
        return SATISFACTION_RECOVERY;
    }

    public boolean hasBeenUsed() {
        return used;
    }
}
