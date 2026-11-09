package game;

import customer.Customer;
import customer.CustomerStatus;
import staff.Cashier;

public class TurnProcessor {

    private final GameState gameState;
    private final Cashier cashier;

    public TurnProcessor(GameState gameState, Cashier cashier) {
        this.gameState = gameState;
        this.cashier = cashier;
    }

/**
 * Restores 15 satisfaction to eligible customers
 * during an active Happy Hour turn.
 */
public void applyHappyHourRecovery() {

    HappyHour happyHour = gameState.getHappyHour();
    int currentTurn = gameState.getCurrentTurn();

    if (!happyHour.isActive(currentTurn)) {
        return;
    }

    for (Customer customer : gameState.getCustomers()) {

        CustomerStatus status = customer.getStatus();

        if (status == CustomerStatus.WAITING
                || status == CustomerStatus.SEATED) {

            customer.increaseSatisfaction(
                happyHour.getSatisfactionRecovery()
            );
        }
    }
    }
    
    public void runEndOfTurn(int currentTurn) {
        processEating(currentTurn);
        processPayments(currentTurn);
        updateSatisfaction();
        handleAbandonment();
    }

    /**
     * Customers finish eating one turn after being served.
     */
    private void processEating(int currentTurn) {
        for (Customer customer : gameState.getCustomers()) {
            if (customer.getStatus() == CustomerStatus.EATING
                    && currentTurn > customer.getServedTurn()) {
                customer.finishEating(currentTurn);
            }
        }
    }

    /**
     * Process at most one payment per turn.
     * Customers are checked in arrival order.
     */
    private void processPayments(int currentTurn) {
        for (Customer customer : gameState.getCustomers()) {
            if (customer.canPayOn(currentTurn)) {

                int amount = cashier.processPayment(customer, currentTurn);
                gameState.recordPayment(customer, amount);

                return;
            }
        }
    }

    /**
     * Waiting and seated customers lose satisfaction.
     */
    private void updateSatisfaction() {
        for (Customer customer : gameState.getCustomers()) {
            customer.decreaseSatisfaction();
        }
    }

    /**
     * Customers who reach zero satisfaction leave without paying.
     * Customer.leave() frees the table and cancels any unpaid order.
     */
    private void handleAbandonment() {
        for (Customer customer : gameState.getCustomers()) {
            if (!customer.hasAbandoned()) {
                continue;
            }

            gameState.removeWaitingCustomer(customer);
            customer.leave();
            gameState.recordUnhappyDeparture();
        }
    }
}