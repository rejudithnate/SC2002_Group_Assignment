package restaurantrush.entity;

public abstract class Staff {
    // Each staff instance has its own allowance for the current turn.
    private boolean hasActed;

    public final boolean isAvailable() {
        return !hasActed;
    }

    public final void resetForNewTurn() {
        hasActed = false;
    }

    protected final void requireAvailable() {
        if (hasActed) {
            throw new IllegalStateException(getClass().getSimpleName() + " already acted this turn");
        }
    }

    protected final void markActed() {
        requireAvailable();
        hasActed = true;
    }

    // Choosing Wait still spends this staff member's task.
    public final void waitTurn() {
        markActed();
    }
}
