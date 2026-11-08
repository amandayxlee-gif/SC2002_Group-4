package customer;

import java.util.List;
import menu.MenuItem;

public class CriticCustomer extends Customer {

    public CriticCustomer(int customerId) {
        super(customerId);
    }

    @Override
    public void decreaseSatisfaction() {
        if (getStatus() == CustomerStatus.WAITING || getStatus() == CustomerStatus.SEATED) {
            satisfaction = Math.max(0, satisfaction - 12);
        }
    }

    @Override
    public String getCustomerTypeLabel() {
        return "Critic";
    }

    @Override
    public MenuItem chooseMenuItem(List<MenuItem> items) {
        for (MenuItem item : items) {
            if ("Pasta".equalsIgnoreCase(item.getName())) {
                return item;
            }
        }
        return super.chooseMenuItem(items);
    }
}
