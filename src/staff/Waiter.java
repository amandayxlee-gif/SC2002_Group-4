package staff;

import customer.Customer;
import menu.MenuItem;

/**
 * STUB (Person 3 scope): the Waiter performs at most one task per turn,
 * chosen by the manager. Method bodies are TODOs; GameController owns the
 * validate-then-execute-immediately sequencing around these calls.
 */
public class Waiter {

    public void seat(Customer customer, Table table) {
        customer.seatAt(table);
    }

    /** TODO: take a seated customer's order (item chosen by CustomerChoiceGenerator). */
    public void takeOrder(Customer customer, MenuItem item) {
        // Intentionally empty.
    }

    /** TODO: serve a READY order; only when currentTurn > order.getReadyTurn(). */
    public void serve(customer.Order order, Customer customer, int currentTurn) {
        // Intentionally empty.
    }
}
