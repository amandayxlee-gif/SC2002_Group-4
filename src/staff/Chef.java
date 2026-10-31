package staff;

import customer.Order;
import customer.OrderStatus;

/**
 * The Chef prepares one unit of an eligible order when
 * instructed by the GameController.
 *
 * The guards here are an intentional second line of defence. The
 * GameController validates every choice before calling, so these checks
 * should never fire in normal play.
 */
public class Chef {

    public void prepare(Order order, int currentTurn) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }

        if (order.getStatus() != OrderStatus.PLACED
                && order.getStatus() != OrderStatus.PREPARING) {
            throw new IllegalStateException(
                "Only placed or preparing orders can be prepared"
            );
        }

        order.advance(currentTurn);
    }
}