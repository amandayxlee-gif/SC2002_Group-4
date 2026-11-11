package menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Menu {
    private final List<MenuItem> items = new ArrayList<>();
    private final ComboMeal comboMeal;

    public Menu() {
        items.add(new MenuItem("Salad", 900, 1));
        items.add(new MenuItem("Burger", 1500, 1));
        items.add(new MenuItem("Pasta", 1800, 2));
        comboMeal = new ComboMeal(items.get(0), items.get(2));
    }

    public List<MenuItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public ComboMeal getComboMeal() {
        return comboMeal;
    }

    public List<MenuItem> getAllItems() {
        List<MenuItem> available = new ArrayList<>(items);
        available.add(comboMeal);
        return Collections.unmodifiableList(available);
    }

    public MenuItem getItem(String name) {
        for (MenuItem item : getAllItems()) {
            if (item.getName().equals(name)) {
                return item;
            }
        }
        return null;
    }
}
