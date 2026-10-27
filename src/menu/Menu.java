package menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stage 1 restaurant menu containing the three base dishes.
 * Menu items are fixed and exposed through a read-only list.
 */

public class Menu {

    private final List<MenuItem> items = new ArrayList<>();

    public Menu() {
        // Base setting (assignment doc section 2.1)
        items.add(new MenuItem("Salad", 900, 1));
        items.add(new MenuItem("Burger", 1500, 1));
        items.add(new MenuItem("Pasta", 1800, 2));
    }

    /** Unmodifiable view (UML note on GameState: read-only collection getters). */
    public List<MenuItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /** Returns the item with this name, or null. */
    public MenuItem getItem(String name) {
        for (MenuItem item : items) {
            if (item.getName().equals(name)) {
                return item;
            }
        }
        return null;
    }
}
