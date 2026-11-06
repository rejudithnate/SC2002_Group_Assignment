
package restaurantrush.entity;

import java.util.List;

public final class Order {

    private final int tableId;
    private final List<MenuItem> menuItems;
    private final int requiredUnits;
    private int cookedUnit;
    private OrderStatus status = OrderStatus.PREPARING;

    public Order(int tableId, List<MenuItem> menuItems) {

        if (tableId < 1) {
            throw new IllegalArgumentException("Table ID must be positive");
        }

        this.tableId = tableId;
        this.menuItems = List.copyOf(menuItems);

        if (this.menuItems.isEmpty()) {
            throw new IllegalArgumentException("An order must contain an item");
        }

        requiredUnits = this.menuItems.stream()
                .mapToInt(MenuItem::getPreparationUnits)
                .reduce(0, Math::addExact);
    }

    public int getTableId() {
        return tableId;
    }

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    public int getCookedUnit() {
        return cookedUnit;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void prepareUnit() {
        requireStatus(OrderStatus.PREPARING);

        cookedUnit++;

        if (cookedUnit == requiredUnits) {
            status = OrderStatus.READY;
        }
    }

    public void markServed() {
        requireStatus(OrderStatus.READY);
        status = OrderStatus.SERVED;
    }

    public void markPaid() {
        requireStatus(OrderStatus.SERVED);
        status = OrderStatus.PAID;
    }

    public void cancel() {
        if (status != OrderStatus.PREPARING && status != OrderStatus.READY) {
            throw new IllegalStateException("Order is already closed");
        }

        status = OrderStatus.CANCELLED;
    }

    public double getPrice() {
        return getPriceCents() / 100.0;
    }

    public int getPriceCents() {
        return menuItems.stream().mapToInt(MenuItem::getPriceCents).reduce(0, Math::addExact);
    }

    private void requireStatus(OrderStatus expected) {
        if (status != expected) {
            throw new IllegalStateException(
                "Expected order status " + expected
            );
        }
    }
}
