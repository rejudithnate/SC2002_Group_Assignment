package restaurantrush.entity;

import java.util.Objects;

public final class SingleDish extends MenuItem {

    private final String name;
    private final int priceCents;
    private final int preparationUnits;

    public SingleDish(String name, int priceCents, int preparationUnits) {
        this.name = Objects.requireNonNull(name);

        // Check that the dish has a valid name, price and preparation time
        if (name.isBlank() || priceCents < 0 || preparationUnits < 1) {
            throw new IllegalArgumentException("Invalid dish name, price or preparation units");
        }

        this.priceCents = priceCents;
        this.preparationUnits = preparationUnits;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getPriceCents() {
        return priceCents;
    }

    @Override
    public int getPreparationUnits() {
        return preparationUnits;
    }
}