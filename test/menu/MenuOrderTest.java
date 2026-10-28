package menu;

import customer.Order;

public class MenuOrderTest {
    public static void main(String[] args) {
        Menu menu = new Menu();

        MenuItem burger = menu.getItem("Burger");
        Order order = new Order(1, 1, burger);

        assert order.getItem() == burger;
        assert order.getFinalPriceCents() == 1500;
        assert order.getCustomerId() == 1;

        System.out.println("All menu-order integration tests passed!");
    }
}