package restaurantrush.entity;

import java.util.List;

public final class Menu {
    private final List<MenuItem> menuItems = List.of(
            new MenuItem("Salad", 9, 1),
            new MenuItem("Burger", 15, 1),
            new MenuItem("Pasta", 18, 2));

    public List<MenuItem> getMenuItems() { return menuItems; }

    /** Repeatable customer choice: Pasta, Salad, Burger, then repeat by customer ID. */
    public MenuItem chooseForCustomer(int customerId) {
        if (customerId < 1) { throw new IllegalArgumentException("Customer ID must be positive"); }
        return menuItems.get((customerId - 1 + 2) % menuItems.size());
    }
}
