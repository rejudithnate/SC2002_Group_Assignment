package restaurantrush.entity;

import java.util.List;

public final class Order {
    private final int tableId;
    private final List<MenuItem> menuItems;
    private final int requiredUnits;
    private int cookedUnit;
    private OrderStatus status = OrderStatus.PREPARING;
    // Discounts are settled at ordering time, not recalculated at payment.
    private final int lockedPriceCents;

    public Order(int tableId, List<MenuItem> menuItems) {
        this(tableId, menuItems, totalPriceCents(menuItems));
    }

    public Order(int tableId, List<MenuItem> menuItems, int finalPriceCents) {
        if (tableId < 1) {
            throw new IllegalArgumentException("Table ID must be positive");
        }

        this.tableId = tableId;
        this.menuItems = List.copyOf(menuItems);

        if (this.menuItems.isEmpty()) {
            throw new IllegalArgumentException("An order must contain an item");
        }

        int fullPriceCents = totalPriceCents(this.menuItems);

        if (finalPriceCents < 0 || finalPriceCents > fullPriceCents) {
            throw new IllegalArgumentException(
                "Final price must be between zero and full price"
            );
        }

        this.lockedPriceCents = finalPriceCents;

        int totalUnits = 0;

        for (MenuItem item : this.menuItems) {
            totalUnits += item.getPreparationUnits();
        }

        requiredUnits = totalUnits;
    }

    private static int totalPriceCents(List<MenuItem> items) {
        int total = 0;

        for (MenuItem item : items) {
            total += item.getPriceCents();
        }

        return total;
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

    // READY is reached only after every required preparation unit is complete.
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
        return lockedPriceCents;
    }

    private void requireStatus(OrderStatus expected) {
        if (status != expected) {
            throw new IllegalStateException(
                "Expected order status " + expected
            );
        }
    }
}
