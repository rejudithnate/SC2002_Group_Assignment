package restaurantrush.entity;
import java.util.Objects;

public final class Chef {

    //track chef action
    private boolean hasActed;

    // Check if chef is available to act
    public boolean isAvailable() { return !hasActed; }

    // reset chef action at the start of each round
    public void resetForNewTurn() { hasActed = false; }

    // prevents chef from acting if it has already acted
    private void requireAvailable() {
        if (hasActed) { 
            throw new IllegalStateException("Chef already acted this turn"); }
    }

    // prepares one unit of the given order and sets chef as acted 
    public void prepareOrder(Order order) {
        requireAvailable();
        Objects.requireNonNull(order).prepareUnit();
        hasActed = true;
    }

    // skip the turn 
    public void waitTurn() {
        requireAvailable();
        hasActed = true;
    }
}