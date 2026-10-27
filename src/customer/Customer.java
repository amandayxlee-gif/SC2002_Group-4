package customer;

/**
 * STUB (Person 2 scope): minimal customer, just enough for the turn
 * controller to run — an arrival is created with satisfaction 100 and
 * status WAITING (assignment doc section 3.2, step 1).
 *
 */
public class Customer {

    private final int customerId;
    private int satisfaction;
    private CustomerStatus status;

    public Customer(int customerId) {
        this.customerId = customerId;
        this.satisfaction = 100;
        this.status = CustomerStatus.WAITING;
    }

    public int getCustomerId() {
        return customerId;
    }

    public int getSatisfaction() {
        return satisfaction;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    // ------------------------------------------------------------------
    // GAP: the lifecycle hooks below are TODOs (Person 2 scope). Status
    // changes only through these methods; there is no public status setter,
    // so Stage 2 subclasses can later override the hooks.
    // ------------------------------------------------------------------

    /** TODO: attach the chosen order */
    public void assignOrder(Order order) {
        // Intentionally empty.
    }

    /** TODO: lose the waiting satisfaction (Regular: 8, minimum 0). */
    public void decreaseSatisfaction() {
        // Intentionally empty.
    }

    /** TODO: true once satisfaction has reached 0. */
    public boolean hasAbandoned() {
        return false; // TODO
    }

    /** TODO: record servedTurn; status moves to EATING. */
    public void onServed(int currentTurn) {
        // Intentionally empty.
    }

    /** TODO: one full turn of eating; becomes READY_TO_PAY. */
    public void finishEating(int currentTurn) {
        // Intentionally empty.
    }

    /** TODO: status moves to PAID. */
    public void onPaid() {
        // Intentionally empty.
    }
}
