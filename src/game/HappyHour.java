package game;

public class HappyHour { 
    private boolean used = false; 
    private int startTurn = -1; 

    private static final int DURATION = 2; 
    private static final double DISCOUNT= 0.2;
    private static final int SATISFACTION_RECOVERY = 15;

    // Activate Happy Hour once per game
    public boolean activate(int currentTurn) {

        if (used || currentTurn < 1
            || currentTurn > GameState.TOTAL_TURNS) {
        return false;
    }
    used = true;
    startTurn = currentTurn;
    return true;
}


    //Check if Happy Hour is active
    public boolean isActive(int currentTurn) {

        return used
            && currentTurn >= startTurn
            && currentTurn < startTurn + DURATION;
    }

    //Check remaining turns
    public int getRemainingTurns(int currentTurn) {

        if (!isActive(currentTurn)) {
            return 0;
        }

        return startTurn + DURATION - currentTurn;
    }

    //Applying discount for Happy Hour (20%)
    public int applyDiscount(int priceCents, int currentTurn) {

        if (!isActive(currentTurn)) {
            return priceCents;
        }

        return (int) Math.round(priceCents * (1- DISCOUNT));
    }
    
    // Return satisfaction recovery amount
    public int getSatisfactionRecovery() {
        return SATISFACTION_RECOVERY;
    }

    public boolean hasBeenUsed() {
        return used;
    }

}