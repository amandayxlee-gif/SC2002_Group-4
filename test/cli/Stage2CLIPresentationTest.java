package cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import customer.CriticCustomer;
import customer.Customer;
import customer.Order;
import customer.VIPCustomer;
import game.GameController;
import game.GameState;
import menu.Menu;

public final class Stage2CLIPresentationTest {

    private Stage2CLIPresentationTest() { }

    public static void main(String[] args) throws Exception {
        Menu menu = new Menu();
        GameController controller = new GameController(menu);
        GameState state = controller.getGameState();

        Customer regular = new Customer(state.nextCustomerId());
        Customer vip = new VIPCustomer(state.nextCustomerId());
        Customer critic = new CriticCustomer(state.nextCustomerId());
        state.addCustomer(regular);
        state.addCustomer(vip);
        state.addCustomer(critic);

        Order vipOrder = new Order(state.nextOrderId(), vip.getCustomerId(), menu.getItems().get(0));
        vip.assignOrder(vipOrder);
        state.addOrder(vipOrder);

        RestaurantCLI cli = new RestaurantCLI(controller);
        String queueOutput = capture(cli::displayState);
        lineContains(queueOutput, "C1 ", "[REGULAR]");
        lineContains(queueOutput, "C2 ", "[VIP]");
        lineContains(queueOutput, "C3 ", "[CRITIC]");
        lineContains(queueOutput, "#1 C2", "$8.10 (menu $9.00)");
        lineContains(queueOutput, "#1 C2", "[VIP]");

        vip.seatAt(state.findTable(1));
        state.removeWaitingCustomer(vip);
        critic.seatAt(state.findTable(2));
        state.removeWaitingCustomer(critic);

        String seatedOutput = capture(cli::displayState);
        lineContains(seatedOutput, "T1: C2", "[VIP]");
        lineContains(seatedOutput, "T2: C3", "[CRITIC]");
        lineContains(seatedOutput, "C1 ", "[REGULAR]");

        System.out.println("PASS customer-type labels in queue, tables and orders");
        System.out.println("PASS VIP bill preview uses customer polymorphism");
        System.out.println("PASS Stage 2 CLI presentation test");
    }

    private static String capture(Runnable action) throws Exception {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8.name()));
            action.run();
            return buffer.toString(StandardCharsets.UTF_8.name());
        } finally {
            System.setOut(original);
        }
    }

    private static void lineContains(String output, String prefix, String required) {
        for (String line : output.split("\\R")) {
            if (line.contains(prefix) && line.contains(required)) {
                return;
            }
        }
        throw new AssertionError("Expected a line containing both [" + prefix + "] and ["
                + required + "] but found:\n" + output);
    }
}
