package cli;

import game.GameController;
import game.GameResult;
import game.WaiterChoice;

/**
 * STUB (Person 5 scope): the CLI only collects the manager's decisions and
 * displays state — game rules stay in the domain and control classes
 * (assignment doc section 2.2).
 *
 * The decision loop this CLI must drive (design_choices.txt section 2):
 * show the start-of-turn state, then repeat — the manager makes ONE choice,
 * the controller validates and executes it immediately (or explains why it
 * failed, without using up the staff task), and the manager either redecides
 * or moves on to the next decision or ends decision-making. Choices are
 * never collected in a batch.
 */
public class RestaurantCLI {

    private final GameController controller;

    public RestaurantCLI(GameController controller) {
        this.controller = controller;
    }

    /** TODO: start-of-turn display (assignment doc section 3.1). */
    public void displayState() {
        // Intentionally empty.
    }

    /** TODO: read one Waiter choice, selecting action and target by number. */
    public WaiterChoice getWaiterChoice() {
        return null; // TODO
    }

    /** TODO: read the Chef's order choice, selecting by number. */
    public int getChefOrderChoice() {
        return -1; // TODO
    }

    /** TODO: explain why an invalid choice failed; the manager chooses again. */
    public void displayError(String message) {
        // Intentionally empty.
    }

    /** TODO: final result display, including N/A average when nobody paid. */
    public void displayResult(GameResult result) {
        // Intentionally empty.
    }

    /** TODO: the sequential decision loop described in the class comment. */
    public void run() {
        // Intentionally empty.
    }
}
