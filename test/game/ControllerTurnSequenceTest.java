package game;

import menu.Menu;

public class ControllerTurnSequenceTest {

    public static void main(String[] args) {

        GameController controller = new GameController(new Menu());
        GameState state = controller.getGameState();

        // Cannot perform actions before starting a turn
        assert controller.executeWaiterChoice(new WaitChoice())
                .equals("No active turn");

        assert controller.executeChefChoice(1)
                .equals("No active turn");

        // Cannot end a turn that has not started
        boolean rejected = false;

        try {
            controller.endTurn();
        } catch (IllegalStateException e) {
            rejected = true;
        }

        assert rejected;

        // Start turn 1
        controller.startTurn();
        assert state.getCurrentTurn() == 1;

        // Cannot start another turn while turn 1 is active
        rejected = false;

        try {
            controller.startTurn();
        } catch (IllegalStateException e) {
            rejected = true;
        }

        assert rejected;
        assert state.getCurrentTurn() == 1;

        // End turn 1
        controller.endTurn();

        // Cannot end the same turn twice
        rejected = false;

        try {
            controller.endTurn();
        } catch (IllegalStateException e) {
            rejected = true;
        }

        assert rejected;

        // Cannot perform actions after ending a turn
        assert controller.executeWaiterChoice(new WaitChoice())
                .equals("No active turn");

        assert controller.executeChefChoice(1)
                .equals("No active turn");

        // Complete turns 2 through 18
        for (int turn = 2; turn <= 18; turn++) {
            controller.startTurn();

            assert state.getCurrentTurn() == turn;

            controller.endTurn();
        }

        assert controller.isGameOver();

        // Cannot start turn 19
        rejected = false;

        try {
            controller.startTurn();
        } catch (IllegalStateException e) {
            rejected = true;
        }

        assert rejected;
        assert state.getCurrentTurn() == 18;

        System.out.println("All Controller turn-sequence tests passed!");
    }
}