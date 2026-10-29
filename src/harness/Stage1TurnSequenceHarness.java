package harness;

import game.GameController;
import game.GameResult;
import game.WaitChoice;
import menu.Menu;

/**
 * Runs a simple 18-turn test without taking any restaurant actions.
 * The game ends in defeat because no customers are served or paid.
 */
public class Stage1TurnSequenceHarness {

    public static void main(String[] args) {
        GameController controller = new GameController(new Menu());
        controller.startGame();

        for (int turn = 1; turn <= 18; turn++) {
            controller.startTurn();

            // Skip the Waiter action and attempt an invalid Chef order.
            String waiterError = controller.executeWaiterChoice(new WaitChoice());
            String chefError = controller.executeChefChoice(-1); // no order exists yet to pick

            System.out.printf(
                    "Turn %2d | waiting: %d | waiter choice: %s | chef choice: %s%n",
                    turn,
                    controller.getGameState().getWaitingList().size(),
                    describe(waiterError),
                    describe(chefError));

            controller.endTurn();
        }

        GameResult result = controller.checkResult();
        System.out.println("Result after turn 18: " + result);

        // Simple "if defeat" check, with the causes fetched separately for
        // display — the pattern the real CLI can reuse.
        if (result == GameResult.DEFEAT) {
            for (String cause : controller.getGameState().getDefeatCauses()) {
                System.out.println("  missed target: " + cause);
            }
        }
    }

    private static String describe(String error) {
        return error == null ? "executed" : "rejected: " + error;
    }
}
