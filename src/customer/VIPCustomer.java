package customer;

public class VIPCustomer extends Customer {

    public VIPCustomer(int customerId) {
        super(customerId);
    }

    /** VIP customer loses only 5 satisfaction points per turn. */
    @Override
    public void decreaseSatisfaction() {
        if (getStatus() == CustomerStatus.WAITING || getStatus() == CustomerStatus.SEATED) {
            satisfaction = Math.max(0, satisfaction - 5);
        }
    }

    /** VIP customer receives a 10% discount on their bill. */
    @Override
    public int calculateBillCents() {
        int originalPrice = super.calculateBillCents();
        return (int) Math.round(originalPrice * 0.90);
    }

    @Override
    public String getCustomerTypeLabel() {
        return "VIP";
    }
}
