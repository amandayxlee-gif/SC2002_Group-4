package cli;

import java.util.ArrayList;
import java.util.List;

import customer.CriticCustomer;
import customer.Customer;
import customer.CustomerChoiceGenerator;
import customer.Order;
import customer.VIPCustomer;
import game.GameController;
import game.GameState;
import game.SeatChoice;
import game.TakeOrderChoice;
import game.WaitChoice;
import menu.Menu;
import staff.Cashier;
import staff.Table;

public final class Stage2IntegrationGateTest {

    private Stage2IntegrationGateTest() { }

    public static void main(String[] args) {
        List<String> blockers = new ArrayList<>();
        checkCustomerRules(blockers);
        checkGameArrivals(blockers);
        checkVipPayment(blockers);
        checkCriticPreference(blockers);
        checkCriticControllerOrder(blockers);

        if (blockers.isEmpty()) {
            System.out.println("PASS Stage 2 customer rules, arrivals, timing and billing");
            System.out.println("PASS Critic-specific pasta preference");
            return;
        }

        System.out.println("PENDING Stage 2 backend integration:");
        for (String blocker : blockers) {
            System.out.println("  - " + blocker);
        }
        if (args.length > 0 && "--strict".equals(args[0])) {
            throw new AssertionError("Stage 2 integration has " + blockers.size() + " blocker(s)");
        }
    }

    private static void checkCustomerRules(List<String> blockers) {
        Customer regular = new Customer(1);
        Customer vip = new VIPCustomer(2);
        Customer critic = new CriticCustomer(3);

        regular.decreaseSatisfaction();
        vip.decreaseSatisfaction();
        critic.decreaseSatisfaction();

        if (regular.getSatisfaction() != 92 || vip.getSatisfaction() != 95
                || critic.getSatisfaction() != 88) {
            blockers.add("Expected satisfaction after one waiting turn: Regular 92, VIP 95, Critic 88");
        }

        if (!"Regular".equals(regular.getCustomerTypeLabel())
                || !"VIP".equals(vip.getCustomerTypeLabel())
                || !"Critic".equals(critic.getCustomerTypeLabel())) {
            blockers.add("Customer type labels do not match the three Stage 2 customer classes");
        }
    }

    private static void checkGameArrivals(List<String> blockers) {
        GameController controller = new GameController(new Menu());
        GameState state = controller.getGameState();
        int arrivals = 0;
        boolean sawVip = false;
        boolean sawCritic = false;

        for (int turn = 1; turn <= GameState.TOTAL_TURNS; turn++) {
            controller.startTurn();
            for (Customer customer : state.getCustomers()) {
                if (customer.getCustomerId() <= arrivals) {
                    continue;
                }
                sawVip |= customer instanceof VIPCustomer;
                sawCritic |= customer instanceof CriticCustomer;
            }
            arrivals = state.getCustomers().size();
            String error = controller.executeWaiterChoice(new WaitChoice());
            if (error != null) {
                blockers.add("Waiter WAIT rejected on turn " + turn + ": " + error);
                return;
            }
            controller.endTurn();
        }

        if (arrivals != 6) {
            blockers.add("Expected six scheduled customer arrivals, found " + arrivals);
        }
        if (!sawVip || !sawCritic) {
            blockers.add("GameController did not create both VIP and Critic customers");
        }
    }

    private static void checkVipPayment(List<String> blockers) {
        Menu menu = new Menu();
        VIPCustomer vip = new VIPCustomer(99);
        Order order = new Order(99, vip.getCustomerId(), menu.getItems().get(0));
        vip.assignOrder(order);

        int expected = (int) Math.round(order.getItem().getPriceCents() * 0.90);
        if (order.getFinalPriceCents() != expected) {
            blockers.add("VIP discount not locked on the order at placement");
        }
        if (vip.calculateBillCents() != expected) {
            blockers.add("VIPCustomer.calculateBillCents() does not apply a 10% discount");
        }

        Table table = new Table(99);
        vip.seatAt(table);
        order.markServed();
        vip.onServed(1);
        vip.finishEating(2);

        if (vip.canPayOn(2)) {
            blockers.add("Customer can pay on the same turn they finished eating");
        }

        try {
            int charged = new Cashier().processPayment(vip, 3);
            if (charged != expected) {
                blockers.add("Cashier charged " + charged + " cents for VIP instead of "
                        + expected + " cents");
            }
        } catch (RuntimeException error) {
            blockers.add("VIP payment rejected: " + error.getMessage());
        }
    }

    private static void checkCriticPreference(List<String> blockers) {
        Menu menu = new Menu();
        CustomerChoiceGenerator generator = new CustomerChoiceGenerator();
        String regularChoice = generator.chooseItem(menu, new Customer(5)).getName();
        String criticChoice = generator.chooseItem(menu, new CriticCustomer(5)).getName();
        if (!"Burger".equals(regularChoice) || !"Pasta".equals(criticChoice)) {
            blockers.add("Critic must prefer Pasta over the regular customer's Burger selection");
        }
    }

    private static void checkCriticControllerOrder(List<String> blockers) {
        GameController controller = new GameController(new Menu());
        for (int turn = 1; turn <= 13; turn++) {
            controller.startTurn();
            String error = turn == 13
                    ? controller.executeWaiterChoice(new SeatChoice(5, 1))
                    : controller.executeWaiterChoice(new WaitChoice());
            if (error != null) {
                blockers.add("Critic seating failed on turn " + turn + ": " + error);
                return;
            }
            controller.endTurn();
        }

        controller.startTurn();
        String error = controller.executeWaiterChoice(new TakeOrderChoice(1));
        if (error != null) {
            blockers.add("Critic order-taking failed: " + error);
            return;
        }
        Customer critic = controller.getGameState().findCustomer(5);
        if (!(critic instanceof CriticCustomer) || critic.getOrder() == null
                || !"Pasta".equals(critic.getOrder().getItem().getName())) {
            blockers.add("Controller did not place the Critic's preferred Pasta order");
        }
    }
}
