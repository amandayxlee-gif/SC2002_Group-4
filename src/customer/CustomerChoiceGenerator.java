package customer;

import menu.Menu;
import menu.MenuItem;
import java.util.List;

public class CustomerChoiceGenerator {

    /**
     * Chooses menu items in a fixed order based on customer ID.
     */
    public MenuItem chooseItem(Menu menu, Customer customer) {
        List<MenuItem> items = menu.getItems();
        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("Menu is empty");
        }
        int index = Math.abs(customer.getCustomerId() - 1) % items.size();
        return items.get(index);
    }
}
