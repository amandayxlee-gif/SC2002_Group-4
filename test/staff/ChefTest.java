package staff;

import customer.Order;
import customer.OrderStatus;
import menu.MenuItem;

public class ChefTest {

    public static void main(String[] args) {
        Chef chef = new Chef();
        MenuItem pasta = new MenuItem("Pasta", 1800, 2);
        Order order = new Order(1, 1, pasta);

        assert order.getStatus() == OrderStatus.PLACED;

        chef.prepare(order, 2);

        assert order.getStatus() == OrderStatus.PREPARING;
        assert order.getRemainingUnits() == 1;

        chef.prepare(order, 3);

        assert order.getStatus() == OrderStatus.READY;
        assert order.getRemainingUnits() == 0;
        assert order.getReadyTurn() == 3;

        boolean rejected = false;

        try {
            chef.prepare(order, 4);
        } catch (IllegalStateException e) {
            rejected = true;
        }

        assert rejected : "Chef should reject an already-ready order";

        System.out.println("All Chef tests passed!");
    }
}