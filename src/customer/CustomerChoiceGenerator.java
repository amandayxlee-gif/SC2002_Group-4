package customer;

import menu.Menu;
import menu.MenuItem;

public class CustomerChoiceGenerator {

    /** Customer C4 selects the ComboMeal (deterministic) */
    private static final int COMBO_CUSTOMER_ID = 4;

    public MenuItem chooseItem(Menu menu, Customer customer) {
        if (customer.getCustomerId() == COMBO_CUSTOMER_ID) {
            return menu.getComboMeal();
        }
        return customer.chooseMenuItem(menu.getItems());
    }
}
