package customer;

import menu.Menu;
import menu.MenuItem;
import java.util.List;

public class CustomerChoiceGenerator {

    /**
     * Deterministic selection rule: (customerId - 1) % menuSize[cite: 1, 16].
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
