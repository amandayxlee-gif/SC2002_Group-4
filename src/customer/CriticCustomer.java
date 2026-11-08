package customer;

import java.util.List;
import menu.MenuItem;

/** Critic customer: loses 12 satisfaction per waiting turn and always orders Pasta. */
public class CriticCustomer extends Customer {

    public static final int CRITIC_SATISFACTION_LOSS_PER_TURN = 12;

    public CriticCustomer(int customerId) {
        super(customerId);
    }

    @Override
    public void decreaseSatisfaction() {
        if (getStatus() == CustomerStatus.WAITING || getStatus() == CustomerStatus.SEATED) {
            satisfaction = Math.max(0, satisfaction - CRITIC_SATISFACTION_LOSS_PER_TURN);
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
