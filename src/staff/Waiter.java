package staff;

import customer.Customer;
import customer.CustomerStatus;
import customer.Order;
import customer.OrderStatus;

/**
 * Performs the manager-directed Waiter actions on customers and orders.
 *
 * The guards here are an intentional second line of defence. The
 * GameController validates every choice before calling, so these checks
 * should never fire in normal play.
 */
public class Waiter {

    public void seat(Customer customer, Table table) {
        customer.seatAt(table);
    }

    /**
     * Assigns an order to a seated customer.
     * The controller is responsible for creating and storing the order.
     */
    public void takeOrder(Customer customer, Order order) {
        if (customer == null || order == null) {
            throw new IllegalArgumentException(
                "Customer and order cannot be null"
            );
        }

        if (customer.getStatus() != CustomerStatus.SEATED) {
            throw new IllegalStateException(
                "Customer must be seated before ordering"
            );
        }

        if (customer.getOrder() != null) {
            throw new IllegalStateException(
                "Customer already has an order"
            );
        }

        if (order.getCustomerId() != customer.getCustomerId()) {
            throw new IllegalArgumentException(
                "Order belongs to a different customer"
            );
        }

        customer.assignOrder(order);
    }

    /**
     * Serves a READY order on a turn after it became ready.
     */
    public void serve(Order order, Customer customer, int currentTurn) {
        if (order == null || customer == null) {
            throw new IllegalArgumentException(
                "Order and customer cannot be null"
            );
        }

        if (customer.getOrder() != order) {
            throw new IllegalStateException(
                "Order does not belong to this customer"
            );
        }

        if (customer.getStatus() != CustomerStatus.SEATED) {
            throw new IllegalStateException(
                "Customer is not waiting for food"
            );
        }

        if (order.getStatus() != OrderStatus.READY) {
            throw new IllegalStateException(
                "Order is not ready"
            );
        }

        if (currentTurn <= order.getReadyTurn()) {
            throw new IllegalStateException(
                "Order cannot be served on the same turn it became ready"
            );
        }

        order.markServed();
        customer.onServed(currentTurn);
    }
}