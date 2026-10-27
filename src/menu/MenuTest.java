package menu;

public class MenuTest {
    public static void main(String[] args) {
        Menu menu = new Menu();

        assert menu.getItems().size() == 3;
        assert menu.getItem("Salad").getPriceCents() == 900;
        assert menu.getItem("Burger").getPriceCents() == 1500;
        assert menu.getItem("Pasta").getPriceCents() == 1800;
        assert menu.getItem("Pasta").getPreparationUnits() == 2;
        assert menu.getItem("Pizza") == null;

        System.out.println("All menu tests passed!");
    }
}