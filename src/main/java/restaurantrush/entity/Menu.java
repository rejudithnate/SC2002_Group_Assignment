package restaurantrush.entity;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class Menu {

    // Available dishes and their prices (in cents)
    private final SingleDish salad = new SingleDish("Salad", 900, 1);
    private final SingleDish burger = new SingleDish("Burger", 1500, 1);
    private final SingleDish pasta = new SingleDish("Pasta", 1800, 2);

    // Combo consists of two existing dishes
    private final ComboMeal combo = new ComboMeal("Salad + Pasta Combo", salad, pasta);

    private final List<MenuItem> menuItems = List.of(salad, burger, pasta, combo);

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    public SingleDish getPasta() {
        return pasta;
    }

    public MenuItem chooseForCustomer(int customerId) {

        // Customer ID must be positive
        if (customerId < 1) {
            throw new IllegalArgumentException("Customer ID must be positive");
        }

        // Each selection has a 25% chance of being a combo meal
        if (ThreadLocalRandom.current().nextInt(4) == 0) {
            return combo;
        }

        // Other customers alternate between Pasta, Salad and Burger
        int index = (customerId - 1 + 2) % 3;
        return menuItems.get(index);
    }
}
