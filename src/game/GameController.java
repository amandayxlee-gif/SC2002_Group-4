package game;

import customer.Customer;
import customer.CustomerChoiceGenerator;
import customer.CustomerStatus;
import customer.Order;
import customer.OrderStatus;
import menu.Menu;
import menu.MenuItem;
import staff.Cashier;
import staff.Chef;
import staff.Table;
import staff.Waiter;

/**
 * Controls the Restaurant Rush game.
 *
 * Manager decisions are validated before execution.
 * Invalid choices return an error without changing game state.
 */
public class GameController {

    private static final int[] ARRIVAL_TURNS = {1, 4, 7, 10, 13, 16};

    private final GameState gameState;
    private final TurnProcessor turnProcessor;
    private final Waiter waiter;
    private final Chef chef;
    private final Cashier cashier;
    private final Menu menu;
    private final CustomerChoiceGenerator choiceGenerator;

    // Track whether each staff member has acted this turn
    private boolean waiterActedThisTurn;
    private boolean chefActedThisTurn;

    // Track whether a turn is currently active
    private boolean turnActive = false;

    public GameController(Menu menu) {
        this.gameState = new GameState();
        this.waiter = new Waiter();
        this.chef = new Chef();
        this.cashier = new Cashier();
        this.choiceGenerator = new CustomerChoiceGenerator();
        this.turnProcessor = new TurnProcessor(gameState, cashier);
        this.menu = menu;
    }

    /**
     * The game starts at turn 0 with two empty tables.
     */
    public void startGame() {
        // No additional setup needed.
    }

    /**
     * Starts a new turn and processes customer arrivals.
     */
    public void startTurn() {

        if (turnActive) {
            throw new IllegalStateException(
                "Cannot start a new turn before ending the current turn"
            );
        }

        if (gameState.isGameOver()) {
            throw new IllegalStateException(
                "Game is already over"
            );
        }

        gameState.advanceTurn();

        waiterActedThisTurn = false;
        chefActedThisTurn = false;

        turnActive = true;

        processArrivals();
    }

    /**
     * Validates and executes the Waiter's chosen action.
     *
     * Returns null if successful, otherwise an error message.
     */
    public String executeWaiterChoice(WaiterChoice choice) {

        if (!turnActive) {
            return "No active turn";
        }

        if (waiterActedThisTurn) {
            return "Waiter has already acted this turn";
        }

        String error = validateWaiterChoice(choice);

        if (error != null) {
            return error;
        }

        executeValidWaiterChoice(choice);

        waiterActedThisTurn = true;

        return null;
    }

    /**
     * Validates and executes the Chef's chosen action.
     */
    public String executeChefChoice(int orderId) {

        if (!turnActive) {
            return "No active turn";
        }

        if (chefActedThisTurn) {
            return "Chef has already acted this turn";
        }

        String error = validateChefChoice(orderId);

        if (error != null) {
            return error;
        }

        for (Order order : gameState.getOrders()) {

            if (order.getOrderId() == orderId) {

                chef.prepare(order, gameState.getCurrentTurn());

                chefActedThisTurn = true;

                return null;
            }
        }

        return "Order does not exist";
    }

    /**
     * Runs automatic end-of-turn processes.
     */
    public void endTurn() {

        if (!turnActive) {
            throw new IllegalStateException(
                "No active turn to end"
            );
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

    /**
     * Adds a new customer on scheduled arrival turns.
     */
    private void processArrivals() {

        int currentTurn = gameState.getCurrentTurn();

        for (int arrivalTurn : ARRIVAL_TURNS) {

            if (arrivalTurn == currentTurn) {

                int customerId = gameState.getCustomers().size() + 1;

                Customer customer = new Customer(customerId);

                gameState.addCustomer(customer);
            }
        }
    }

    /**
     * Checks whether a Waiter action is valid.
     */
    private String validateWaiterChoice(WaiterChoice choice) {

        if (choice == null) {
            return "No waiter choice provided";
        }

        // SEAT
        if (choice instanceof SeatChoice seatChoice) {

            Customer customer = null;

            for (Customer c : gameState.getWaitingList()) {

                if (c.getCustomerId() == seatChoice.getCustomerId()) {
                    customer = c;
                    break;
                }
            }

            if (customer == null) {
                return "Customer is not in the waiting queue";
            }

            for (Table table : gameState.getTables()) {

                if (table.getTableId() == seatChoice.getTableId()) {

                    if (!table.isAvailable()) {
                        return "Table is already occupied";
                    }

                    return null;
                }
            }

            return "Table does not exist";
        }

        // TAKE_ORDER
        if (choice instanceof TakeOrderChoice takeOrderChoice) {

            for (Table table : gameState.getTables()) {

                if (table.getTableId() == takeOrderChoice.getTableId()) {

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

                    return null;
                }
            }

            return "Table does not exist";
        }

        // SERVE
        if (choice instanceof ServeChoice serveChoice) {

            for (Order order : gameState.getOrders()) {

                if (order.getOrderId() == serveChoice.getOrderId()) {

                    if (order.getStatus() != OrderStatus.READY) {
                        return "Order is not ready";
                    }

                    if (gameState.getCurrentTurn() <= order.getReadyTurn()) {
                        return "Cannot serve on the same turn food becomes ready";
                    }

                    for (Customer customer : gameState.getCustomers()) {

                        if (customer.getCustomerId() == order.getCustomerId()) {

                            if (customer.getStatus() != CustomerStatus.SEATED) {
                                return "Customer is not seated";
                            }

                            if (customer.getOrder() != order) {
                                return "Order does not belong to customer";
                            }

                            return null;
                        }
                    }

                    return "Customer does not exist";
                }
            }

            return "Order does not exist";
        }

        // WAIT
        if (choice instanceof WaitChoice) {
            return null;
        }

        return "Unsupported waiter choice";
    }

    /**
     * Executes a Waiter action that has passed validation.
     */
    private void executeValidWaiterChoice(WaiterChoice choice) {

        // SEAT
        if (choice instanceof SeatChoice seatChoice) {

            Customer customer = null;
            Table table = null;

            for (Customer c : gameState.getWaitingList()) {

                if (c.getCustomerId() == seatChoice.getCustomerId()) {
                    customer = c;
                    break;
                }
            }

            for (Table t : gameState.getTables()) {

                if (t.getTableId() == seatChoice.getTableId()) {
                    table = t;
                    break;
                }
            }

            waiter.seat(customer, table);

            gameState.removeWaitingCustomer(customer);

            return;
        }

        // TAKE_ORDER
        if (choice instanceof TakeOrderChoice takeOrderChoice) {

            for (Table table : gameState.getTables()) {

                if (table.getTableId() == takeOrderChoice.getTableId()) {

                    Customer customer = table.getOccupant();

                    MenuItem item = choiceGenerator.chooseItem(
                        menu,
                        customer
                    );

                    int orderId = gameState.getOrders().size() + 1;

                    Order order = new Order(
                        orderId,
                        customer.getCustomerId(),
                        item
                    );

                    waiter.takeOrder(customer, order);

                    gameState.addOrder(order);

                    return;
                }
            }
        }

        // SERVE
        if (choice instanceof ServeChoice serveChoice) {

            for (Order order : gameState.getOrders()) {

                if (order.getOrderId() == serveChoice.getOrderId()) {

                    for (Customer customer : gameState.getCustomers()) {

                        if (customer.getCustomerId() == order.getCustomerId()) {

                            waiter.serve(
                                order,
                                customer,
                                gameState.getCurrentTurn()
                            );

                            gameState.recordServedCustomer();

                            return;
                        }
                    }
                }
            }
        }

        // WAIT requires no action.
    }

    /**
     * Checks whether the Chef can prepare the selected order.
     */
    private String validateChefChoice(int orderId) {

        for (Order order : gameState.getOrders()) {

            if (order.getOrderId() == orderId) {

                if (order.getStatus() != OrderStatus.PLACED
                        && order.getStatus() != OrderStatus.PREPARING) {

                    return "Order cannot be prepared";
                }

                if (order.getRemainingUnits() <= 0) {
                    return "Order has no remaining preparation units";
                }

                return null;
            }
        }

        return "Order does not exist";
    }
}