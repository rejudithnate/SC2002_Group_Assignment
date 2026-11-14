package restaurantrush.entity;

import java.util.Objects;

public final class Chef extends Staff {
    // One task advances an order by exactly one preparation unit.
    public void prepareOrder(Order order) {
        requireAvailable();
        Objects.requireNonNull(order).prepareUnit();
        markActed();
    }
}
