
package game;

import menu.Menu;
import customer.CustomerStatus;

public class HostStage3Test {

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static void main(String[] args) {

        GameController controller =
            new GameController(new Menu());

        // Host cannot act outside an active turn.
        check(
            controller.executeHostChoice(
                new SeatChoice(1, 1)
            ) != null,
            "Host must not act outside an active turn"
        );

        controller.startTurn();

        // Invalid choices should be rejected.
        check(
            controller.executeHostChoice(null) != null,
            "Null choice rejected"
        );

        check(
            controller.executeHostChoice(
                new SeatChoice(999, 1)
            ) != null,
            "Unknown customer rejected"
        );

        check(
            controller.executeHostChoice(
                new SeatChoice(1, 999)
            ) != null,
            "Unknown table rejected"
        );

        // Host seats the first customer.
        check(
            controller.executeHostChoice(
                new SeatChoice(1, 1)
            ) == null,
            "Host should seat customer 1"
        );

        check(
            controller.getGameState()
                .findCustomer(1)
                .getStatus() == CustomerStatus.SEATED,
            "Customer should be seated"
        );

        check(
            controller.getGameState()
                .getWaitingList()
                .isEmpty(),
            "Waiting queue should be updated"
        );

        // Host cannot seat twice in one turn.
        check(
            controller.executeHostChoice(
                new SeatChoice(1, 2)
            ) != null,
            "Host cannot act twice"
        );

        // Waiter still has an independent action.
        check(
            controller.executeWaiterChoice(
                new WaitChoice()
            ) == null,
            "Waiter retains independent action"
        );

        // Happy Hour cannot be activated after Host acts.
        check(
            !controller.activateHappyHour(),
            "Happy Hour should be blocked after Host action"
        );

        controller.endTurn();
        controller.startTurn();

        // Already seated customers cannot be seated again.
        check(
            controller.executeHostChoice(
                new SeatChoice(1, 2)
            ) != null,
            "Cannot reseat an already seated customer"
        );

        controller.endTurn();

        System.out.println("HostStage3Test PASSED");
    }
}
