package restaurantrush.entity;

import java.util.Objects;

public final class ComboMeal extends MenuItem {

    private final String name;
    private final SingleDish firstDish;
    private final SingleDish secondDish;

    public ComboMeal(String name, SingleDish firstDish, SingleDish secondDish) {
        this.name = Objects.requireNonNull(name);

        // A combo meal must have a valid name
        if (name.isBlank()) {
            throw new IllegalArgumentException("Combo needs a name");
        }

        // Each combo consists of two dishes
        this.firstDish = Objects.requireNonNull(firstDish);
        this.secondDish = Objects.requireNonNull(secondDish);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getPriceCents() {
        // Add the prices of both dishes
        long totalPrice = (long) firstDish.getPriceCents() + secondDish.getPriceCents();

        // Apply a 10% discount, rounded to the nearest cent (same as VIP pricing)
        return Math.toIntExact((totalPrice * 9L + 5L) / 10L);
    }

    @Override
    public int getPreparationUnits() {
        // Both dishes need to be prepared
        return firstDish.getPreparationUnits() + secondDish.getPreparationUnits();
    }
}
