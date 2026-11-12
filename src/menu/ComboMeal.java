package menu;

import java.util.Objects;

public final class ComboMeal extends MenuItem {
    private static final int COMBO_DISCOUNT_PERCENT = 10;
    private final MenuItem firstItem;
    private final MenuItem secondItem;

    public ComboMeal(MenuItem firstItem, MenuItem secondItem) {
        super(nameOf(firstItem, secondItem), comboPrice(firstItem, secondItem),
                totalPreparation(firstItem, secondItem));
        this.firstItem = firstItem;
        this.secondItem = secondItem;
    }

    private static String nameOf(MenuItem first, MenuItem second) {
        return Objects.requireNonNull(first, "First item is required").getName()
                + " + " + Objects.requireNonNull(second, "Second item is required").getName()
                + " Combo";
    }

    private static int comboPrice(MenuItem first, MenuItem second) {
        long total = (long) Objects.requireNonNull(first).getPriceCents()
                + Objects.requireNonNull(second).getPriceCents();
        long discounted = (total * (100 - COMBO_DISCOUNT_PERCENT) + 50) / 100;
        return Math.toIntExact(discounted);
    }

    private static int totalPreparation(MenuItem first, MenuItem second) {
        return Math.addExact(Objects.requireNonNull(first).getPreparationUnits(),
                Objects.requireNonNull(second).getPreparationUnits());
    }

    public MenuItem getFirstItem() {
        return firstItem;
    }

    public MenuItem getSecondItem() {
        return secondItem;
    }
}
