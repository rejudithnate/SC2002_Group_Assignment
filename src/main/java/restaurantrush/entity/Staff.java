package restaurantrush.entity;

// each staff instance has some similar state and behavior, but the specific actions are different for each type of staff member
public abstract class Staff {
    private boolean hasActed;

    public final boolean isAvailable() { return !hasActed; }
    public final void resetForNewTurn() { hasActed = false; }
    protected final void requireAvailable() {
        if (hasActed) { throw new IllegalStateException("Staff member already acted this turn"); }
    }
    protected final void markActed() { requireAvailable(); hasActed = true; }
    public final void waitTurn() { markActed(); }
}
