package staff;

import customer.Customer;
import customer.CustomerStatus;
import customer.Order;
import customer.OrderStatus;
import menu.MenuItem;

public class WaiterTest {

    public static void main(String[] args) {
        Waiter waiter = new Waiter();
        Customer customer = new Customer(1);
        Table table = new Table(1);

        MenuItem salad = new MenuItem("Salad", 900, 1);
        Order order = new Order(1, 1, salad);

        // 1. Seat the customer
        waiter.seat(customer, table);

        assert customer.getStatus() == CustomerStatus.SEATED;
        assert table.getOccupant() == customer;

        // 2. Take the customer's order
        waiter.takeOrder(customer, order);

        assert customer.getOrder() == order;
        assert order.getStatus() == OrderStatus.PLACED;

        // 3. Prepare the order
        Chef chef = new Chef();
        chef.prepare(order, 2);

        assert order.getStatus() == OrderStatus.READY;

        // 4. Serving on the same turn must fail
        boolean rejected = false;

        try {
            waiter.serve(order, customer, 2);
        } catch (IllegalStateException e) {
            rejected = true;
        }

        assert rejected : "Same-turn serving should be rejected";

        // 5. Serve on the next turn
        waiter.serve(order, customer, 3);

        assert order.getStatus() == OrderStatus.SERVED;
        assert customer.getStatus() == CustomerStatus.EATING;
        assert customer.getServedTurn() == 3;

        System.out.println("All Waiter tests passed!");
    }
}