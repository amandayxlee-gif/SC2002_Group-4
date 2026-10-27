package harness;

import game.GameController;
import game.GameResult;
import game.WaitChoice;
import menu.Menu;

/**
 * basically just to test code runs properly. update as needed. 
 * 
 * TEST HARNESS (temporary, not part of the game): verifies the Stage 1
 * turn-sequence skeleton — the 18-turn loop, arrivals on turns
 * 1/4/7/10/13/16, end-of-turn processing and the victory/defeat check.
 * Not the CLI (Person 5 scope); delete or replace once the CLI exists.
 *
 * NOTE: with the Waiter/Chef/kitchen logic still TODO, no customer can be
 * seated, served or paid, so this scripted run ends in DEFEAT. A reproducible
 * victory run becomes possible once those gaps are filled.
 */
public class Stage1TurnSequenceHarness {

    public static void main(String[] args) {
        GameController controller = new GameController(new Menu());
        controller.startGame();

        for (int turn = 1; turn <= 18; turn++) {
            controller.startTurn();

            // Manager decision point, repeated per design_choices section 2:
            // one choice at a time, validated and executed immediately.
            // executeWaiterChoice returns null when executed, or the failure
            // reason (which the real CLI would show via displayError before
            // letting the manager redecide). WaitChoice is used here because
            // seat/order/serve execution is still a TODO.
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
