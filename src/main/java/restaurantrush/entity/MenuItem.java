package restaurantrush.entity;

public abstract class MenuItem {
    public abstract String getName();
    public abstract int getPriceCents();
    public double getPrice() {
        return getPriceCents() / 100.0;
    }

    public abstract int getPreparationUnits();
}
