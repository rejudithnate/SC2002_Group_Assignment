package restaurantrush.entity;

public abstract class Staff {
    private boolean hasActed;

    public final boolean isAvailable() { return !hasActed; }

    public final void resetForNewTurn() { hasActed = false; }

    protected final void requireAvailable() {
        if (hasActed) {
            throw new IllegalStateException(getClass().getSimpleName() + " already acted this turn");
        }
    }

    protected final void markActed() {
        requireAvailable();
        hasActed = true;
    }

    public final void waitTurn() { 
        markActed(); 
    }
}
