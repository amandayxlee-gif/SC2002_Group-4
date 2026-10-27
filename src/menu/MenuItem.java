package menu;

/** One menu item. Price and preparation units are exposed via getters only. */
public class MenuItem {

    private final String name;
    private final int priceCents;
    private final int preparationUnits;

public MenuItem(String name, int priceCents, int preparationUnits) {
    if (name == null || name.isBlank()) {
        throw new IllegalArgumentException("Item name cannot be empty");
    }

    if (priceCents < 0) {
        throw new IllegalArgumentException("Price cannot be negative");
    }

    if (preparationUnits <= 0) {
        throw new IllegalArgumentException("Preparation units must be positive");
    }

    this.name = name;
    this.priceCents = priceCents;
    this.preparationUnits = preparationUnits;
}

    public String getName() {
        return name;
    }

    public int getPriceCents() {
        return priceCents;
    }

    public int getPreparationUnits() {
        return preparationUnits;
    }
}
