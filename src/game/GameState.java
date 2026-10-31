package game;

import customer.Customer;
import customer.Order;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import staff.Table;

/**
 * Holds all mutable game state
 *
 * Rules that only need the statistics collected here live in this class
 * (the victory/defeat check); the turn sequence lives in GameController.
 * Collection getters return unmodifiable views.
 */
public class GameState {

    /** Base setting (assignment doc section 2.1). */
    public static final int TOTAL_TURNS = 18;
    public static final int VICTORY_MIN_PAID_CUSTOMERS = 4;
    public static final int VICTORY_MIN_REVENUE_CENTS = 4500;
    public static final int VICTORY_MIN_AVERAGE_SATISFACTION = 60;

    private int currentTurn;
    private int revenueCents;
    private int paidCount;
    private int servedCount;
    private int unhappyDepartures;
    private int paidSatisfactionTotal;

    private final List<Customer> waitingList = new ArrayList<>();
    private final List<Customer> customers = new ArrayList<>();
    private final List<Order> orders = new ArrayList<>();
    private final List<Table> tables = new ArrayList<>();

    /** Creates the base setting: two tables (assignment doc section 2.1). */
    public GameState() {
        // no indication that there will be more tables for now, hardcoded this
        // if needed, can make it extensible (take number of tables as instantiation param)
        tables.add(new Table(1));
        tables.add(new Table(2));
    }

    public int getCurrentTurn() {
        return currentTurn;
    }

    public int getRevenueCents() {
        return revenueCents;
    }

    public int getPaidCount() {
        return paidCount;
    }

    public int getServedCount() {
        return servedCount;
    }

    public int getUnhappyDepartures() {
        return unhappyDepartures;
    }

    /**
     * Average satisfaction over paid customers, using the satisfaction recorded
     * at payment. Returns 0.0 when no customer has paid; the CLI displays that
     * as N/A (assignment doc section 3.5). Since (assuming) all payments > 0, there 
     * is no need to distinguish between 0.0 vs truly N/A
     */
    public double getAverageSatisfaction() {
        if (paidCount == 0) {
            return 0.0;
        }
        return (double) paidSatisfactionTotal / paidCount;
    }

    public List<Customer> getWaitingList() {
        return Collections.unmodifiableList(waitingList);
    }

    public List<Customer> getCustomers() {
        return Collections.unmodifiableList(customers);
    }

    public List<Table> getTables() {
        return Collections.unmodifiableList(tables);
    }

    public List<Order> getOrders() {
        return Collections.unmodifiableList(orders);
    }

    /** Returns a free table, or null if both are occupied. */
    public Table getAvailableTable() {
        for (Table table : tables) {
            if (table.isAvailable()) {
                return table;
            }
        }
        return null;
    }

    // Single location for lookups by id: for all the find loops that the
    // controller (validation and execution) and the CLI (display) all need.

    /** Returns the customer with this id, or null if none exists. */
    public Customer findCustomer(int customerId) {
        for (Customer customer : customers) {
            if (customer.getCustomerId() == customerId) {
                return customer;
            }
        }
        return null;
    }

    /** Returns the customer with this id if currently in the waiting queue, or null. */
    public Customer findWaitingCustomer(int customerId) {
        for (Customer customer : waitingList) {
            if (customer.getCustomerId() == customerId) {
                return customer;
            }
        }
        return null;
    }

    /** Returns the table with this id, or null if none exists. */
    public Table findTable(int tableId) {
        for (Table table : tables) {
            if (table.getTableId() == tableId) {
                return table;
            }
        }
        return null;
    }

    /** Returns the order with this id, or null if none exists. */
    public Order findOrder(int orderId) {
        for (Order order : orders) {
            if (order.getOrderId() == orderId) {
                return order;
            }
        }
        return null;
    }

    /** Adds a customer to the game. Newly arrived customers also wait. */
    public void addCustomer(Customer customer) {
        customers.add(customer);
        waitingList.add(customer);
    }

    public void removeWaitingCustomer(Customer customer) {
        waitingList.remove(customer);
    }

    public void addOrder(Order order) {
        orders.add(order);
    }

    public void recordServedCustomer() {
        servedCount++;
    }

    public void recordUnhappyDeparture() {
        unhappyDepartures++;
    }

    /**
     * Records a payment: adds the bill to revenue, marks one more customer as
     * paid and accumulates the satisfaction recorded at payment
     * (assignment doc section 3.2, step 4).
     */
    public void recordPayment(Customer customer, int amountCents) {
        revenueCents += amountCents;
        paidCount++;
        paidSatisfactionTotal += customer.getSatisfaction();
    }

    /** Moves the game to the next turn. */
    public void advanceTurn() {
        currentTurn++;
    }

    /** The game always runs all 18 turns; there is no early ending (section 3.2, step 6). */
    public boolean isGameOver() {
        return currentTurn >= TOTAL_TURNS;
    }

    /**
     * Victory/defeat check. Targets (assignment doc section 2.1): 
     * at least 4 paid customers, at least 4500
     * cents revenue, average paid satisfaction at least 60.
     */
    public GameResult checkResult() {
        boolean victory = hasEnoughPaidCustomers() && hasEnoughRevenue()
                && hasEnoughSatisfaction();
        return victory ? GameResult.VICTORY : GameResult.DEFEAT;
    }

    /**
     * Defeat detail: the victory targets that were missed, one human-readable
     * entry each, for the CLI to display after a DEFEAT result. Empty when the
     * game is won. Several targets can be missed in one run, so causes are a
     * list rather than a single label. Lives here because GameState owns the
     * statistics; GameResult itself stays a plain VICTORY/DEFEAT so callers
     * can keep writing simple "if defeat" checks.
     */
    public List<String> getDefeatCauses() {
        List<String> causes = new ArrayList<>();
        if (!hasEnoughPaidCustomers()) {
            causes.add(String.format(
                    "fewer than %d paid customers (%d)",
                    VICTORY_MIN_PAID_CUSTOMERS, paidCount));
        }
        if (!hasEnoughRevenue()) {
            causes.add(String.format(
                    "revenue below %d cents (%d)",
                    VICTORY_MIN_REVENUE_CENTS, revenueCents));
        }
        if (!hasEnoughSatisfaction()) {
            causes.add(String.format(
                    "average paid satisfaction below %d (%.1f)",
                    VICTORY_MIN_AVERAGE_SATISFACTION, getAverageSatisfaction()));
        }
        return causes;
    }

    private boolean hasEnoughPaidCustomers() {
        return paidCount >= VICTORY_MIN_PAID_CUSTOMERS;
    }

    private boolean hasEnoughRevenue() {
        return revenueCents >= VICTORY_MIN_REVENUE_CENTS;
    }

    private boolean hasEnoughSatisfaction() {
        return getAverageSatisfaction() >= VICTORY_MIN_AVERAGE_SATISFACTION;
    }
}
