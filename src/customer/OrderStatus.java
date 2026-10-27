package customer;

import menu.MenuItem;

/** Status of an order (Person 2 scope). */
public enum OrderStatus {
    PLACED,
    PREPARING,
    READY,
    SERVED,
    PAID,
    CANCELLED
}
