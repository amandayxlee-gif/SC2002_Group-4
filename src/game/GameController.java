package game;

import customer.Customer;
import customer.CustomerChoiceGenerator;
import menu.Menu;
import staff.Cashier;
import staff.Chef;
import staff.Waiter;

/**
 * Control class that sequences the turn and exposes the API used by the CLI
 * (Person 1 scope). Owns GameState and TurnProcessor by composition.
 *
 * TURN FLOW (design_choices.txt section 2): the manager's decisions are
 * SEQUENTIAL. Each decision is validated and executed immediately, one at a
 * time, before the next decision is made. An invalid choice changes no state
 * and does not use up the staff task (assignment doc section 3.4); the
 * manager redecides. The manager never submits a batch of choices at once.
 */
public class GameController {

    /** Scheduled arrivals (assignment doc section 2.1): turns 1, 4, 7, 10, 13, 16. */
    private static final int[] ARRIVAL_TURNS = {1, 4, 7, 10, 13, 16};

    private final GameState gameState;
    private final TurnProcessor turnProcessor;
    private final Waiter waiter;
    private final Chef chef;
    private final Cashier cashier;
    private final Menu menu;
    private final CustomerChoiceGenerator choiceGenerator;

    public GameController(Menu menu) {
        this.gameState = new GameState();
        this.waiter = new Waiter();
        this.chef = new Chef();
        this.cashier = new Cashier();
        this.choiceGenerator = new CustomerChoiceGenerator();
        this.turnProcessor = new TurnProcessor(gameState, cashier);
        this.menu = menu;
    }

    /** Resets the game to its starting state (turn 0, both tables free). */
    public void startGame() {
        // GameState starts at turn 0 with 2 tables; a new GameController
        // therefore starts a fresh game. Nothing else to reset for now.
    }

    /**
     * Starts a turn: the turn counter advances and the scheduled arrival, if
     * any, joins the waiting queue with satisfaction 100
     * (assignment doc section 3.2, step 1).
     */
    public void startTurn() {
        gameState.advanceTurn();
        processArrivals();
    }

    /**
     * One manager decision directed at the Waiter. Validates first; on an
     * invalid choice returns the failure reason (for the CLI to show via
     * displayError) without changing state and without using up the Waiter's
     * task (assignment doc section 3.4) — the manager redecides. On a valid
     * choice the action executes immediately.
     *
     * @return null if the choice was executed; otherwise the reason it failed
     */
    public String executeWaiterChoice(WaiterChoice choice) {
        String error = validateWaiterChoice(choice);
        if (error != null) {
            return error;
        }
        executeValidWaiterChoice(choice);
        return null;
    }

    /**
     * One manager decision directed at the Chef: advance the order with this
     * id by one preparation unit. Same validate-then-execute-immediately
     * contract as executeWaiterChoice. (A plain int orderId per the current
     * UML; may become a ChefChoice hierarchy later.)
     *
     * @return null if the choice was executed; otherwise the reason it failed
     */
    public String executeChefChoice(int orderId) {
        String error = validateChefChoice(orderId);
        if (error != null) {
            return error;
        }
        // GAP: dispatch to chef.prepare(order, currentTurn). The Chef stub's
        // prepare() body is a TODO.
        return null;
    }

    /**
     * Ends decision-making and runs the automated part of the turn
     * (eating, payment, satisfaction loss, abandonment) via TurnProcessor
     * (assignment doc section 3.2, steps 4 and 5).
     */
    public void endTurn() {
        turnProcessor.runEndOfTurn(gameState.getCurrentTurn());
    }

    /**
     * True once all 18 turns have been played; checkResult() is only
     * meaningful then (assignment doc section 3.2, step 6).
     */
    public boolean isGameOver() {
        return gameState.isGameOver();
    }

    /** Evaluates victory/defeat after the final turn. Delegates to GameState. */
    public GameResult checkResult() {
        return gameState.checkResult();
    }

    public GameState getGameState() {
        return gameState;
    }

    /** Arrival step: the scheduled customer joins the waiting queue (step 1). */
    private void processArrivals() {
        int turn = gameState.getCurrentTurn();
        for (int arrivalTurn : ARRIVAL_TURNS) {
            if (arrivalTurn == turn) {
                Customer arrival = new Customer(gameState.getCustomers().size() + 1);
                gameState.addCustomer(arrival);
            }
        }
    }

    /**
     * GAP (validation): task-specific rules go here, returning the failure
     * reason without changing state, e.g. SeatChoice needs a queued customer
     * and a free table; TakeOrderChoice needs a seated customer without an
     * order; ServeChoice needs a READY order with currentTurn > readyTurn
     * (assignment doc section 3.2 step 3). STUB for now: returns null (always
     * valid) so the turn-sequence skeleton can run.
     *
     * @return null if valid; otherwise the reason the choice fails
     */

    private String validateWaiterChoice(WaiterChoice choice) {
    if (choice == null) {
        return "No waiter choice provided";
    }

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

        for (staff.Table table : gameState.getTables()) {
            if (table.getTableId() == seatChoice.getTableId()) {
                if (!table.isAvailable()) {
                    return "Table is already occupied";
                }
                return null;
            }
        }

        return "Table does not exist";
    }

    // TODO: Validate TAKE_ORDER and SERVE.
    return null;
}

    /**
     * GAP (execution): dispatch on choice.getAction() to the Waiter:
     * WAIT does nothing; SEAT calls waiter.seat(customer, table);
     * TAKE_ORDER calls waiter.takeOrder(customer, item chosen via
     * CustomerChoiceGenerator); SERVE calls waiter.serve(order, customer,
     * currentTurn). STUB for now: no state change (Waiter body is a TODO).
     */

private void executeValidWaiterChoice(WaiterChoice choice) {
    if (choice instanceof SeatChoice seatChoice) {
        Customer customer = null;
        staff.Table table = null;

        for (Customer c : gameState.getWaitingList()) {
            if (c.getCustomerId() == seatChoice.getCustomerId()) {
                customer = c;
                break;
            }
        }

        for (staff.Table t : gameState.getTables()) {
            if (t.getTableId() == seatChoice.getTableId()) {
                table = t;
                break;
            }
        }

        waiter.seat(customer, table);

        // Remove seated customer from waiting queue
        gameState.removeWaitingCustomer(customer);
    }
}

    // TODO: Execute WAIT, TAKE_ORDER and SERVE.

    /**
     * GAP (validation): Chef rules go here, e.g. the order must exist, be
     * taken this or an earlier turn, and have preparation units remaining.
     * STUB for now: returns null (always valid).
     *
     * @return null if valid; otherwise the reason the choice fails
     */
    private String validateChefChoice(int orderId) {
        return null; // TODO: replace with real validation (Chef/kitchen scope)
    }
}
