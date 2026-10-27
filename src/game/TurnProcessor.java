package game;

import staff.Cashier;

/**
 * Runs the automatic (non player-driven) part of a turn: eating, payment,
 * satisfaction loss and abandonment (assignment doc section 3.2, steps 4
 * and 5). Player-driven steps (seat, order, serve, prepare) stay in the
 * GameController, which keeps the controller focused on sequencing
 * (design_choices.txt section 3.2).
 *
 * STEP BODIES ARE TODOs: the eating/payment/satisfaction
 * logic belongs to the customer and staff work packages. Only the call
 * order of the steps is fixed here.
 */
public class TurnProcessor {

    private final GameState gameState;
    private final Cashier cashier;

    public TurnProcessor(GameState gameState, Cashier cashier) {
        this.gameState = gameState;
        this.cashier = cashier;
    }

    /**
     * Runs the end-of-turn steps in the order fixed by the assignment doc:
     * eating, then payment, then satisfaction loss, then abandonment.
     * Called by GameController.endTurn() with the current turn number.
     */
    public void runEndOfTurn(int currentTurn) {
        processEating(currentTurn);
        processPayments(currentTurn);
        updateSatisfaction(currentTurn);
        handleAbandonment();
    }

    /**
     * TODO (step 4): a customer SERVED on an earlier turn finishes one full
     * turn of eating and becomes READY_TO_PAY. A customer who became READY_TO_PAY
     * this turn pays next turn at the earliest. Timing: served on turn S,
     * finishes eating on turn S+1, pays on turn S+2 at the earliest
     * (assignment doc section 3.2 step 4 and worked example section 3.3).
     * Uses Customer.onServed / Customer.finishEating lifecycle hooks.
     */
    private void processEating(int currentTurn) {
        // Intentionally empty: customer lifecycle logic is out of scope for now.
    }

    /**
     * TODO (step 4): the Cashier automatically handles at most one
     * READY_TO_PAY customer per turn, oldest first. Payment adds the bill to
     * revenue, marks customer and order PAID, and frees the table
     * (via GameState.recordPayment and Cashier.processPayment).
     */
    private void processPayments(int currentTurn) {
        // Intentionally empty.
    }

    /**
     * TODO (step 5): every waiting or seated customer not yet served loses
     * satisfaction (Regular: 8 points, minimum 0). Served, eating and
     * ready-to-pay customers lose nothing. Uses Customer.decreaseSatisfaction.
     */
    private void updateSatisfaction(int currentTurn) {
        // Intentionally empty: satisfaction logic is out of scope for now.
    }

    /**
     * TODO (step 5): a customer whose satisfaction reaches 0 leaves without
     * paying: cancel any unfinished order (Order.cancel) and free the table
     * (Table.free), then GameState.recordUnhappyDeparture.
     */
    private void handleAbandonment() {
        // Intentionally empty.
    }
}
