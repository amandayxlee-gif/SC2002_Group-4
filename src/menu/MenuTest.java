package menu;

public class MenuTest {
    public static void main(String[] args) {
        Menu menu = new Menu();

        // Test menu items and prices
        assert menu.getItems().size() == 3;
        assert menu.getItem("Salad").getPriceCents() == 900;
        assert menu.getItem("Burger").getPriceCents() == 1500;
        assert menu.getItem("Pasta").getPriceCents() == 1800;

        // Test preparation units
        assert menu.getItem("Salad").getPreparationUnits() == 1;
        assert menu.getItem("Burger").getPreparationUnits() == 1;
        assert menu.getItem("Pasta").getPreparationUnits() == 2;

        // Test item names and invalid lookups
        assert menu.getItem("Salad").getName().equals("Salad");
        assert menu.getItem("Pizza") == null;

        // Test that the menu list cannot be modified externally
        boolean modificationBlocked = false;

        try {
            menu.getItems().clear();
        } catch (UnsupportedOperationException e) {
            modificationBlocked = true;
        }

        assert modificationBlocked;
        assert menu.getItems().size() == 3;

        // Test invalid menu item names
        try {
            new MenuItem("", 900, 1);
            throw new AssertionError("Empty name was accepted");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        // Test negative prices
        try {
            new MenuItem("Burger", -100, 1);
            throw new AssertionError("Negative price was accepted");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        // Test zero preparation units
        try {
            new MenuItem("Pasta", 1800, 0);
            throw new AssertionError("Zero preparation units were accepted");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        System.out.println("All menu tests passed!");
    }
}