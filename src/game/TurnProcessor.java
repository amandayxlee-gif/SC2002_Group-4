package game;

import customer.Customer;
import customer.CustomerStatus;
import customer.Order;
import customer.OrderStatus;
import staff.Cashier;
import staff.Table;

public class TurnProcessor {

    private final GameState gameState;
    private final Cashier cashier;

    public TurnProcessor(GameState gameState, Cashier cashier) {
        this.gameState = gameState;
        this.cashier = cashier;
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
            if (customer.getStatus() == CustomerStatus.READY_TO_PAY
                    && currentTurn > customer.getReadyToPayTurn()) {

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
     * Remove customers who reach zero satisfaction.
     */
    private void handleAbandonment() {
        for (Customer customer : gameState.getCustomers()) {
            if (!customer.hasAbandoned()) {
                continue;
            }

            Order order = customer.getOrder();

            if (order != null
                    && order.getStatus() != OrderStatus.PAID
                    && order.getStatus() != OrderStatus.CANCELLED) {
                order.cancel();
            }

            Table table = customer.getTable();

            if (table != null && table.getOccupant() == customer) {
                table.free();
            }

            gameState.removeWaitingCustomer(customer);
            customer.setStatus(CustomerStatus.LEFT);
            gameState.recordUnhappyDeparture();
        }
    }
}