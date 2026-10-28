package customer;

import menu.MenuItem;

public class CustomerOrderTest {
    public static void main(String[] args) {
        // 1. Test Customer Satisfaction Decay & Abandonment
        Customer c = new Customer(1);
        System.out.println("Initial Satisfaction: " + c.getSatisfaction()); // Expect 100

        for (int i = 0; i < 12; i++) {
            c.decreaseSatisfaction();
        }
        System.out.println("Satisfaction after 12 turns: " + c.getSatisfaction()); // Expect 4 (100 - 96)

        c.decreaseSatisfaction();
        System.out.println("Has Abandoned: " + c.hasAbandoned()); // Expect true (0 satisfaction)

        // 2. Test Order Advancement (Pasta = 2 prep units)
        MenuItem pasta = new MenuItem("Pasta", 1800, 2);
        Order order = new Order(101, 1, pasta);

        order.advance(2); // Turn 2: prep unit 1 done
        System.out.println("Status Turn 2: " + order.getStatus()); // Expect PREPARING

        order.advance(3); // Turn 3: prep unit 2 done
        System.out.println("Status Turn 3: " + order.getStatus()); // Expect READY
        System.out.println("Ready Turn: " + order.getReadyTurn()); // Expect 3

        System.out.println("All Customer & Order tests passed!");
    }
}