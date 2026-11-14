package restaurantrush.entity;

import java.util.Objects;

public final class ComboMeal extends MenuItem {
    private final String name;
    private final SingleDish firstDish;
    private final SingleDish secondDish;

    public ComboMeal(String name, SingleDish firstDish, SingleDish secondDish) {
        this.name = Objects.requireNonNull(name);

        if (name.isBlank()) {
            throw new IllegalArgumentException("Combo needs a name");
        }

        this.firstDish = Objects.requireNonNull(firstDish);
        this.secondDish = Objects.requireNonNull(secondDish);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getPriceCents() {
        long totalPrice = (long) firstDish.getPriceCents() + secondDish.getPriceCents();

        // Keep 90% of the combined price, rounded to the nearest cent.
        return Math.toIntExact((totalPrice * 9L + 5L) / 10L);
    }

    @Override
    public int getPreparationUnits() {
        // The Chef must complete the preparation units of both components.
        return firstDish.getPreparationUnits() + secondDish.getPreparationUnits();
    }
}
