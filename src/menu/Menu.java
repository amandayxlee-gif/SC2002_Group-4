package menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * STUB (Person 4 scope): holds the menu items. Seeded here with the three
 * base items (assignment doc section 2.1) so the game can run; treat the
 * seeding as a placeholder for now.
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
