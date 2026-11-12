package game;

import customer.CriticCustomer;
import customer.Customer;
import customer.CustomerChoiceGenerator;
import customer.CustomerStatus;
import customer.Order;
import customer.OrderStatus;
import customer.VIPCustomer;
import java.util.List;
import java.util.function.IntFunction;
import menu.Menu;
import menu.MenuItem;
import staff.Cashier;
import staff.Chef;
import staff.Table;
import staff.Waiter;
import staff.Host;
import staff.Seater;

public class GameController {

    private static final int[] ARRIVAL_TURNS = { 1, 4, 7, 10, 13, 16 };
    private static final List<IntFunction<Customer>> ARRIVAL_FACTORIES = List.of(
            Customer::new, VIPCustomer::new, Customer::new,
            Customer::new, CriticCustomer::new, Customer::new);

    private final GameState gameState;
    private final TurnProcessor turnProcessor;
    private final Waiter waiter;
    private final Host host;
    private final Chef chef;
    private final Cashier cashier;
    private final Menu menu;
    private final CustomerChoiceGenerator choiceGenerator;

    private boolean waiterActedThisTurn;
    private boolean hostActedThisTurn;
    private boolean chefActedThisTurn;

    private boolean turnActive = false;
    private boolean managerActionStarted = false;

    public GameController(Menu menu) {
        this.gameState = new GameState();
        this.waiter = new Waiter();
        this.host = new Host();
        this.chef = new Chef();
        this.cashier = new Cashier();
        this.choiceGenerator = new CustomerChoiceGenerator();
        this.turnProcessor = new TurnProcessor(gameState, cashier);
        this.menu = menu;
    }

    public void startTurn() {

        if (turnActive) {
            throw new IllegalStateException(
                    "Cannot start a new turn before ending the current turn");
        }

        if (gameState.isGameOver()) {
            throw new IllegalStateException(
                    "Game is already over");
        }

        gameState.advanceTurn();

        waiterActedThisTurn = false;
        hostActedThisTurn = false;
        chefActedThisTurn = false;
        managerActionStarted = false;

        turnActive = true;

        processArrivals();
        /* Restore satisfaction during the second Happy Hour turn */
        if (gameState.getHappyHour().isActive(gameState.getCurrentTurn())) {
            turnProcessor.applyHappyHourRecovery();
        }
    }

    public boolean activateHappyHour() {

        if (!turnActive || managerActionStarted) {
            return false;
        }

        HappyHour happyHour = gameState.getHappyHour();

        if (!happyHour.activate(gameState.getCurrentTurn())) {
            return false;
        }

        turnProcessor.applyHappyHourRecovery();

        return true;
    }

    public String getHappyHourActivationError() {
        if (!turnActive) {
            return "Cannot activate Happy Hour outside an active turn.";
        }

        if (managerActionStarted) {
            return "Happy Hour must be activated before staff actions.";
        }

        if (gameState.getHappyHour().hasBeenUsed()) {
            return "Happy Hour has already been used.";
        }

        return null;
    }

    /**
     * Stage 3: Host seats one waiting customer per turn.
     * This does not consume the Waiter's action.
     *
     * Returns null on success, or an error message on failure.
     */
    public String executeHostChoice(SeatChoice choice) {

        if (!turnActive) {
            return "No active turn";
        }

        if (hostActedThisTurn) {
            return "Host has already acted this turn";
        }

        if (choice == null) {
            return "No host choice provided";
        }

        String error = seatFromChoice(choice, host);
        if (error != null) {
            return error;
        }

        hostActedThisTurn = true;
        managerActionStarted = true;

        return null;
    }

    /**
     * Shared seating path for the Host and the Waiter: validates the choice,
     * seats the customer through the acting staff role and removes them from
     * the waiting queue. Assumes the caller has checked turn/role limits and
     * that the choice is non-null.
     *
     * Returns null on success, or an error message on failure.
     */
    private String seatFromChoice(SeatChoice choice, Seater seater) {

        Customer customer = gameState.findWaitingCustomer(choice.getCustomerId());

        if (customer == null) {
            return "Customer is not in the waiting queue";
        }

        Table table = gameState.findTable(choice.getTableId());

        if (table == null) {
            return "Table does not exist";
        }

        if (!table.isAvailable()) {
            return "Table is already occupied";
        }

        seater.seat(customer, table);
        gameState.removeWaitingCustomer(customer);

        return null;
    }

    public String executeWaiterChoice(WaiterChoice choice) {

        if (!turnActive) {
            return "No active turn";
        }

        if (waiterActedThisTurn) {
            return "Waiter has already acted this turn";
        }

        if (choice == null) {
            return "No waiter choice provided";
        }

        if (choice instanceof SeatChoice seatChoice) {

            String error = seatFromChoice(seatChoice, waiter);
            if (error != null) {
                return error;
            }
        }

        else if (choice instanceof TakeOrderChoice takeOrderChoice) {

            Table table = gameState.findTable(takeOrderChoice.getTableId());

            if (table == null) {
                return "Table does not exist";
            }

            Customer customer = table.getOccupant();

            if (customer == null) {
                return "Table has no customer";
            }

            if (customer.getStatus() != CustomerStatus.SEATED) {
                return "Customer is not seated";
            }

            if (customer.getOrder() != null) {
                return "Customer already has an order";
            }

            MenuItem item = choiceGenerator.chooseItem(menu, customer);
            Order order = new Order(
                    gameState.nextOrderId(),
                    customer.getCustomerId(),
                    item);

            waiter.takeOrder(customer, order);

            int finalPrice = gameState.getHappyHour().applyDiscount(
                    order.getFinalPriceCents(),
                    gameState.getCurrentTurn());

            order.setFinalPriceCents(finalPrice);

            gameState.addOrder(order);
        }

        else if (choice instanceof ServeChoice serveChoice) {

            Order order = gameState.findOrder(serveChoice.getOrderId());

            if (order == null) {
                return "Order does not exist";
            }

            if (order.getStatus() != OrderStatus.READY) {
                return "Order is not ready";
            }

            if (!order.canBeServedOn(gameState.getCurrentTurn())) {
                return "Cannot serve on the same turn food becomes ready";
            }

            Customer customer = gameState.findCustomer(order.getCustomerId());

            if (customer == null) {
                return "Customer does not exist";
            }

            if (customer.getStatus() != CustomerStatus.SEATED) {
                return "Customer is not seated";
            }

            if (customer.getOrder() != order) {
                return "Order does not belong to customer";
            }

            waiter.serve(order, customer, gameState.getCurrentTurn());
            gameState.recordServedCustomer();
        }

        else if (!(choice instanceof WaitChoice)) {
            return "Unsupported waiter choice";
        }

        waiterActedThisTurn = true;
        managerActionStarted = true;

        return null;
    }

    public String executeChefChoice(int orderId) {

        if (!turnActive) {
            return "No active turn";
        }

        if (chefActedThisTurn) {
            return "Chef has already acted this turn";
        }

        Order order = gameState.findOrder(orderId);

        if (order == null) {
            return "Order does not exist";
        }

        if (order.getStatus() != OrderStatus.PLACED
                && order.getStatus() != OrderStatus.PREPARING) {

            return "Order cannot be prepared";
        }

        if (order.getRemainingUnits() <= 0) {
            return "Order has no remaining preparation units";
        }

        chef.prepare(order, gameState.getCurrentTurn());

        chefActedThisTurn = true;
        managerActionStarted = true;

        return null;
    }

    public void endTurn() {

        if (!turnActive) {
            throw new IllegalStateException(
                    "No active turn to end");
        }

        turnProcessor.runEndOfTurn(gameState.getCurrentTurn());

        turnActive = false;
    }

    public boolean isGameOver() {
        return gameState.isGameOver();
    }

    public GameResult checkResult() {
        return gameState.checkResult();
    }

    public GameState getGameState() {
        return gameState;
    }

    private void processArrivals() {

        int currentTurn = gameState.getCurrentTurn();

        for (int i = 0; i < ARRIVAL_TURNS.length; i++) {
            if (ARRIVAL_TURNS[i] == currentTurn) {
                int customerId = gameState.nextCustomerId();
                Customer customer = ARRIVAL_FACTORIES.get(i).apply(customerId);
                gameState.addCustomer(customer);
            }
        }
    }
}
