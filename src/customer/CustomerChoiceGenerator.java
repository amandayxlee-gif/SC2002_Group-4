package customer;

import menu.Menu;
import menu.MenuItem;

public class CustomerChoiceGenerator {
    public MenuItem chooseItem(Menu menu, Customer customer) {
        if (customer.getCustomerId() == 4) {
            return menu.getComboMeal();
        }
        return customer.chooseMenuItem(menu.getItems());
    }
}
