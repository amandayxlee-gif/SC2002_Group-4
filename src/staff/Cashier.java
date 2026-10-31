
package staff;

import customer.Customer;
import customer.CustomerStatus;
import customer.Order;
import customer.OrderStatus;

/**
 * Collects payment from a customer directed to it by the TurnProcessor.
 *
 * The guards here are an intentional second line of defence, since the
 * TurnProcessor only calls for an eligible customer. These checks
 * should never fire in normal play.
 */
public class Cashier {

    /**
     * Processes payment for a customer who finished eating
     * on an earlier turn.
     *
     * Returns the amount collected in cents.
     */
    public int processPayment(Customer customer, int currentTurn) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }

        if (customer.getStatus() != CustomerStatus.READY_TO_PAY) {
            throw new IllegalStateException(
                "Customer is not ready to pay"
            );
        }

        if (currentTurn <= customer.getReadyToPayTurn()) {
            throw new IllegalStateException(
                "Customer cannot pay on the same turn they finished eating"
            );
        }

        Order order = customer.getOrder();

        if (order == null || order.getStatus() != OrderStatus.SERVED) {
            throw new IllegalStateException(
                "Customer does not have a served order"
            );
        }

        Table table = customer.getTable();

        if (table == null || table.getOccupant() != customer) {
            throw new IllegalStateException(
                "Customer does not occupy a valid table"
            );
        }

        int amountCents = order.getFinalPriceCents();

        order.markPaid();
        customer.onPaid();
        table.free();

        return amountCents;
    }
}