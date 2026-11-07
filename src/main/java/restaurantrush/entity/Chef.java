package restaurantrush.entity;
import java.util.Objects;

public final class Chef extends Staff {
    // prepares one unit of the given order and sets chef as acted 
    public void prepareOrder(Order order) {
        requireAvailable();
        Objects.requireNonNull(order).prepareUnit();
        markActed();
    }

}