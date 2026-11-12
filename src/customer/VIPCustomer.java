package customer;

/** VIP customer: loses 5 satisfaction per waiting turn and pays 10% off the menu price. */
public class VIPCustomer extends Customer {

    public static final int VIP_SATISFACTION_LOSS_PER_TURN = 5;
    public static final int VIP_DISCOUNT_PERCENT = 10;

    public VIPCustomer(int customerId) {
        super(customerId);
    }

    @Override
    public void decreaseSatisfaction() {
        if (getStatus() == CustomerStatus.WAITING || getStatus() == CustomerStatus.SEATED) {
            satisfaction = Math.max(0, satisfaction - VIP_SATISFACTION_LOSS_PER_TURN);
        }
    }

    @Override
    public int calculateBillCents() {
        int originalPrice = super.calculateBillCents();
        return (originalPrice * (100 - VIP_DISCOUNT_PERCENT) + 50) / 100;
    }

    @Override
    public String getCustomerTypeLabel() {
        return "VIP";
    }
}
