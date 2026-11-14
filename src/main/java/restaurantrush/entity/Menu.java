package restaurantrush.entity;

import java.util.List;

public final class Menu {
    // Base menu prices are stored in cents.
    private final SingleDish salad = new SingleDish("Salad", 900, 1);
    private final SingleDish burger = new SingleDish("Burger", 1500, 1);
    private final SingleDish pasta = new SingleDish("Pasta", 1800, 2);

    private final ComboMeal combo = new ComboMeal("Salad + Pasta Combo", salad, pasta);

    private final List<MenuItem> menuItems = List.of(salad, burger, pasta, combo);

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    public SingleDish getPasta() {
        return pasta;
    }

    public MenuItem chooseForCustomer(int customerId) {
        if (customerId < 1) {
            throw new IllegalArgumentException("Customer ID must be positive");
        }

        // A fixed rule makes the same customer choose the same food each game.
        if (customerId % 4 == 0) {
            return combo;
        }

        // Other IDs follow the Pasta, Salad, Burger cycle; Critics override this.
        int index = (customerId - 1 + 2) % 3;
        return menuItems.get(index);
    }
}
