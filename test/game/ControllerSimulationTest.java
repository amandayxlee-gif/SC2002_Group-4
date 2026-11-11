package game;

import customer.Customer;
import customer.CustomerStatus;
import menu.Menu;

public class ControllerSimulationTest {

    public static void main(String[] args) {
        testNoActionDefeat();
        testSuccessfulGame();
        System.out.println("All 18-turn simulation tests passed!");
    }

    private static void testNoActionDefeat() {
        GameController controller = new GameController(new Menu());

        for (int turn = 1; turn <= 18; turn++) {
            controller.startTurn();

            assert controller.getGameState().getCurrentTurn() == turn;

            controller.endTurn();
        }

        GameState state = controller.getGameState();

        assert controller.isGameOver();
        assert state.getCustomers().size() == 6;
        assert state.getPaidCount() == 0;
        assert state.getRevenueCents() == 0;
        assert state.getUnhappyDepartures() == 1;
        assert controller.checkResult() == GameResult.DEFEAT;

        assert state.getCustomers().get(0).getStatus()
                == CustomerStatus.LEFT;
        assert state.getCustomers().get(1).getStatus()
                == CustomerStatus.WAITING;

        System.out.println("No-action defeat simulation passed!");
    }

    private static void testSuccessfulGame() {
        GameController controller = new GameController(new Menu());
        for (int turn = 1; turn <= 18; turn++) {
            controller.startTurn();
            if (turn == 10) {
                if (!controller.activateHappyHour()) {
                    throw new AssertionError("Happy Hour activation failed");
                }
            }
            int hostCustomer = switch (turn) {
                case 1 -> 1;
                case 4 -> 2;
                case 7 -> 3;
                case 10 -> 4;
                case 13 -> 5;
                case 16 -> 6;
                default -> 0;
            };
            if (hostCustomer > 0) {
                int table = hostCustomer % 2 == 1 ? 1 : 2;
                String error = controller.executeHostChoice(new SeatChoice(hostCustomer, table));
                if (error != null) {
                    throw new AssertionError("Turn " + turn + " Host error: " + error);
                }
            }
            WaiterChoice waiterChoice = switch (turn) {
                case 1, 7, 14 -> new TakeOrderChoice(1);
                case 4, 10, 17 -> new TakeOrderChoice(2);
                case 2 -> new ServeChoice(1);
                case 5 -> new ServeChoice(2);
                case 9 -> new ServeChoice(3);
                case 13 -> new ServeChoice(4);
                case 16 -> new ServeChoice(5);
                default -> new WaitChoice();
            };
            String waiterError = controller.executeWaiterChoice(waiterChoice);
            if (waiterError != null) {
                throw new AssertionError("Turn " + turn + " Waiter error: " + waiterError);
            }
            int chefOrderId = switch (turn) {
                case 1 -> 1;
                case 4 -> 2;
                case 7, 8 -> 3;
                case 10, 11, 12 -> 4;
                case 14, 15 -> 5;
                case 17, 18 -> 6;
                default -> 0;
            };
            if (chefOrderId > 0) {
                String chefError = controller.executeChefChoice(chefOrderId);
                if (chefError != null) {
                    throw new AssertionError("Turn " + turn + " Chef error: " + chefError);
                }
            }
            controller.endTurn();
        }
        GameState state = controller.getGameState();
        assert controller.isGameOver();
        assert state.getCustomers().size() == 6;
        assert state.getPaidCount() == 5;
        assert state.getRevenueCents() == 7794;
        assert state.getAveragePaidSatisfaction() == 83.8;
        assert state.getUnhappyDepartures() == 0;
        assert controller.checkResult() == GameResult.VICTORY;
        System.out.println("Successful Stage 3 victory simulation passed!");
    }
}
