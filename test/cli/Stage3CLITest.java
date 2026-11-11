package cli;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Stage3CLITest {
    private static void has(String text, String required, String label) {
        if (!text.contains(required)) {
            throw new AssertionError(label + ": missing " + required);
        }
    }

    private static void absent(String text, String forbidden, String label) {
        if (text.contains(forbidden)) {
            throw new AssertionError(label + ": unexpected " + forbidden);
        }
    }

    private static String replay(String scenario) throws Exception {
        String input = Files.readString(Path.of("evidence/stage3/" + scenario + "_input.txt"));
        InputStream originalInput = System.in;
        PrintStream originalOutput = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            RestaurantCLI.main(new String[0]);
            return output.toString(StandardCharsets.UTF_8);
        } finally {
            System.setIn(originalInput);
            System.setOut(originalOutput);
        }
    }

    public static void main(String[] args) throws Exception {
        String victory = replay("victory");
        has(victory, "FINAL SERVICE REPORT: VICTORY", "18-turn victory");
        has(victory, "Revenue: $77.94   Paid: 5", "Victory with Combo and Happy Hour");
        has(victory, "Average paid satisfaction: 83.8", "Victory satisfaction");
        has(victory, "Host seated Customer C1", "Host prompt and success feedback");
        has(victory, "HAPPY HOUR: ACTIVE (2 turn(s) remaining)", "Happy Hour HUD duration");
        has(victory, "HAPPY HOUR: ACTIVE (1 turn(s) remaining)", "Happy Hour next turn");
        has(victory, "Salad + Pasta Combo ($19.44 (menu $24.30))", "Combo discounted display");
        has(victory, "Combo components: Salad + Pasta (3 preparation units)", "Combo component display");
        has(victory, "Cashier collected $19.44", "Cashier collects Combo Happy Hour bill");
        absent(victory, "Input ended; game not completed", "Victory script completes");
        System.out.println("PASS Stage 3 victory, Host, ComboMeal and Happy Hour UI");

        String vip = replay("vip_happyhour");
        has(vip, "Burger ($10.80 (menu $15.00))", "VIP and Happy Hour stacked discount");
        has(vip, "Revenue: $80.10   Paid: 5", "VIP stacked discount reflected in revenue");
        absent(vip, "Input ended; game not completed", "VIP script completes");
        System.out.println("PASS VIP with stacked discount and locked payment");

        String defeat = replay("defeat");
        has(defeat, "FINAL SERVICE REPORT: DEFEAT", "No-action 18-turn defeat");
        has(defeat, "Unhappy departures: 1", "Defeat walkouts");
        absent(defeat, "Input ended; game not completed", "Defeat script completes");
        System.out.println("PASS Stage 3 no-action defeat");

        String invalid = replay("invalid");
        has(invalid, "Choose 0 or 1 for Happy Hour", "Happy Hour validation");
        has(invalid, "Choose 0 or 1 for Host", "Host validation");
        has(invalid, "Customer is not in the waiting queue", "Host backend validation");
        has(invalid, "Host seated Customer C1", "Host recovery");
        has(invalid, "Order does not exist", "Chef validation");
        has(invalid, "Finished preparing Salad", "Chef recovery");
        has(invalid, "Player quit", "Intentional termination");
        System.out.println("PASS Stage 3 invalid choices and recovery");
        System.out.println("ALL STAGE 3 CLI CHECKS PASSED");
    }
}
