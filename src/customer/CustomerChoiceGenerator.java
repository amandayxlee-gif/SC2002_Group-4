package customer;

import menu.Menu;
import menu.MenuItem;

/**
 * STUB (Person 2 scope): decides what a customer orders. Customers choose
 * their own food — the manager never chooses it (assignment doc section 2.2)
 * — via a documented deterministic rule or seeded randomness.
 */
public class CustomerChoiceGenerator {

    /**
     * GAP: replace with the documented deterministic/seeded choice rule.
     * STUB for now: always the first menu item so a run can proceed.
     */
    public MenuItem chooseItem(Menu menu, Customer customer) {
        return menu.getItems().get(0); // TODO
    }
}
