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
        assert state.getUnhappyDepartures() == 2;
        assert controller.checkResult() == GameResult.DEFEAT;

        assert state.getCustomers().get(0).getStatus()
                == CustomerStatus.LEFT;
        assert state.getCustomers().get(1).getStatus()
                == CustomerStatus.LEFT;

        System.out.println("No-action defeat simulation passed!");
    }

    private static void testSuccessfulGame() {
        GameController controller = new GameController(new Menu());

        for (int turn = 1; turn <= 18; turn++) {
            controller.startTurn();

            String waiterResult = null;
            String chefResult = null;

            switch (turn) {
                case 1:
                    waiterResult = controller.executeWaiterChoice(
                            new SeatChoice(1, 1));
                    break;
                case 2:
                    waiterResult = controller.executeWaiterChoice(
                            new TakeOrderChoice(1));
                    break;
                case 3:
                    chefResult = controller.executeChefChoice(1);
                    break;
                case 4:
                    waiterResult = controller.executeWaiterChoice(
                            new ServeChoice(1));
                    break;
                case 5:
                    waiterResult = controller.executeWaiterChoice(
                            new SeatChoice(2, 2));
                    break;
                case 6:
                    waiterResult = controller.executeWaiterChoice(
                            new TakeOrderChoice(2));
                    break;
                case 7:
                    waiterResult = controller.executeWaiterChoice(
                            new SeatChoice(3, 1));
                    chefResult = controller.executeChefChoice(2);
                    break;
                case 8:
                    waiterResult = controller.executeWaiterChoice(
                            new ServeChoice(2));
                    break;
                case 9:
                    waiterResult = controller.executeWaiterChoice(
                            new TakeOrderChoice(1));
                    break;
                case 10:
                    chefResult = controller.executeChefChoice(3);
                    break;
                case 11:
                    waiterResult = controller.executeWaiterChoice(
                            new SeatChoice(4, 2));
                    chefResult = controller.executeChefChoice(3);
                    break;
                case 12:
                    waiterResult = controller.executeWaiterChoice(
                            new ServeChoice(3));
                    break;
                case 13:
                    waiterResult = controller.executeWaiterChoice(
                            new TakeOrderChoice(2));
                    chefResult = controller.executeChefChoice(4);
                    break;
                case 14:
                    waiterResult = controller.executeWaiterChoice(
                            new ServeChoice(4));
                    break;
                default:
                    break;
            }

            if (waiterResult != null) {
                throw new AssertionError(
                        "Turn " + turn + " waiter error: " + waiterResult);
            }

            if (chefResult != null) {
                throw new AssertionError(
                        "Turn " + turn + " chef error: " + chefResult);
            }

            controller.endTurn();

            GameState state = controller.getGameState();

            System.out.printf(
                    "Turn %2d | Paid: %d | Revenue: $%.2f | Avg satisfaction: %.2f%n",
                    turn,
                    state.getPaidCount(),
                    state.getRevenueCents() / 100.0,
                    state.getAverageSatisfaction());
        }

        GameState state = controller.getGameState();

        assert controller.isGameOver();
        assert state.getCustomers().size() == 6;
        assert state.getPaidCount() == 4;
        assert state.getRevenueCents() == 5100;
        assert state.getAverageSatisfaction() >= 60;
        assert controller.checkResult() == GameResult.VICTORY;

        System.out.println("Successful victory simulation passed!");
    }
}