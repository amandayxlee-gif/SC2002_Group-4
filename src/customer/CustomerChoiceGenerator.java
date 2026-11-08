package customer;

import menu.Menu;
import menu.MenuItem;

public class CustomerChoiceGenerator {

    public MenuItem chooseItem(Menu menu, Customer customer) {
        return customer.chooseMenuItem(menu.getItems());
    }
}
