
package staff;

import customer.Customer;

/**
 * Stage 3 Host: seats waiting customers.
 * The controller limits the Host to one seating per turn.
 */
public class Host implements Seater {

    public void seat(Customer customer, Table table) {
        if (customer == null || table == null) {
            throw new IllegalArgumentException(
                "Customer and table cannot be null"
            );
        }

        customer.seatAt(table);
    }
}
