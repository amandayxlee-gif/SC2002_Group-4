package staff;

import customer.Customer;

/**
 * Common contract for staff roles that can seat a waiting customer, ie. waiter and host
 */
public interface Seater {

    /** Seats the waiting customer at an available table. */
    void seat(Customer customer, Table table);
}
