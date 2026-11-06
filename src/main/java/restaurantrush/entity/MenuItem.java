package restaurantrush.entity;

import java.math.BigDecimal;
import java.util.Objects;

public final class MenuItem {
    private final String itemName;
    private final int priceCents;
    private final int preparationUnits;

    public MenuItem(String itemName, double price, int preparationUnits) {
        this.itemName = Objects.requireNonNull(itemName);
        if (itemName.isBlank() || !Double.isFinite(price) || price < 0 || preparationUnits < 1) {
            throw new IllegalArgumentException("Expected a name, nonnegative price and positive preparation units");
        }
        try {
            priceCents = BigDecimal.valueOf(price).movePointRight(2).intValueExact();
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("Price must fit integer cents with at most two decimals", exception);
        }
        this.preparationUnits = preparationUnits;
    }

    public String getName() { return itemName; }
    public int getPriceCents() { return priceCents; }
    public double getPrice() { return priceCents / 100.0; }
    public int getPreparationUnits() { return preparationUnits; }
}
