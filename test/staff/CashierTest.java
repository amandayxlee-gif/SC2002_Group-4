package staff;

import customer.Customer;
import customer.CustomerStatus;
import customer.Order;
import customer.OrderStatus;
import menu.MenuItem;

public class CashierTest {

    public static void main(String[] args) {
        Waiter waiter = new Waiter();
        Chef chef = new Chef();
        Cashier cashier = new Cashier();

        Customer customer = new Customer(1);
        Table table = new Table(1);
        MenuItem salad = new MenuItem("Salad", 900, 1);
        Order order = new Order(1, 1, salad);

        // Turn 1: seat and take the order
        waiter.seat(customer, table);
        waiter.takeOrder(customer, order);

        // Turn 2: prepare food
        chef.prepare(order, 2);

        // Turn 3: serve food
        waiter.serve(order, customer, 3);

        // Turn 4: finish eating
        customer.finishEating(4);

        assert customer.getStatus() == CustomerStatus.READY_TO_PAY;

        // Payment on turn 4 should fail
        boolean rejected = false;

        try {
            cashier.processPayment(customer, 4);
        } catch (IllegalStateException e) {
            rejected = true;
        }

        assert rejected : "Same-turn payment should be rejected";

        assert table.getOccupant() == customer;
        assert order.getStatus() == OrderStatus.SERVED;

        // Turn 5: payment succeeds
        int amount = cashier.processPayment(customer, 5);

        assert amount == 900;
        assert customer.getStatus() == CustomerStatus.PAID;
        assert order.getStatus() == OrderStatus.PAID;
        assert table.isAvailable();

        System.out.println("All Cashier tests passed!");
    }
}