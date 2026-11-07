package customer;

public class CriticCustomer extends Customer {

    public CriticCustomer(int customerId) {
        super(customerId);
    }

    /** Critic customer loses 12 satisfaction points per turn. */
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
}
