
package game;

import customer.Customer;
import customer.CustomerStatus;
import customer.Order;
import customer.OrderStatus;
import menu.MenuItem;
import staff.Cashier;
import staff.Chef;
import staff.Table;
import staff.Waiter;

public class TurnProcessorTest {

    public static void main(String[] args) {
        testEatingAndPayment();
        testAbandonment();

        System.out.println("All TurnProcessor tests passed!");
    }

    private static void testEatingAndPayment() {
        GameState state = new GameState();
        TurnProcessor processor = new TurnProcessor(state, new Cashier());

        Customer customer = new Customer(1);
        state.addCustomer(customer);

        Table table = state.getTables().get(0);
        Waiter waiter = new Waiter();

        waiter.seat(customer, table);
        state.removeWaitingCustomer(customer);

        MenuItem salad = new MenuItem("Salad", 900, 1);
        Order order = new Order(1, 1, salad);

        waiter.takeOrder(customer, order);
        state.addOrder(order);

        Chef chef = new Chef();
        chef.prepare(order, 2);

        waiter.serve(order, customer, 3);
        state.recordServedCustomer();

        // End of turn 3: still eating
        processor.runEndOfTurn(3);

        assert customer.getStatus() == CustomerStatus.EATING;
        assert state.getPaidCount() == 0;

        // End of turn 4: finishes eating, but cannot pay yet
        processor.runEndOfTurn(4);

        assert customer.getStatus() == CustomerStatus.READY_TO_PAY;
        assert customer.getReadyToPayTurn() == 4;
        assert state.getPaidCount() == 0;

        // End of turn 5: payment succeeds
        processor.runEndOfTurn(5);

        assert customer.getStatus() == CustomerStatus.PAID;
        assert order.getStatus() == OrderStatus.PAID;
        assert state.getPaidCount() == 1;
        assert state.getRevenueCents() == 900;
        assert state.getServedCount() == 1;
        assert table.isAvailable();
    }

    private static void testAbandonment() {
        GameState state = new GameState();
        TurnProcessor processor = new TurnProcessor(state, new Cashier());

        Customer customer = new Customer(2);
        state.addCustomer(customer);

        // Satisfaction: 100 -> 92 -> ... -> 4 -> 0
        for (int turn = 1; turn <= 13; turn++) {
            processor.runEndOfTurn(turn);
        }

        assert customer.getSatisfaction() == 0;
        assert customer.getStatus() == CustomerStatus.LEFT;
        assert state.getUnhappyDepartures() == 1;
        assert state.getWaitingList().isEmpty();

        // Processing another turn must not count departure twice
        processor.runEndOfTurn(14);

        assert state.getUnhappyDepartures() == 1;
    }
}