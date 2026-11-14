package restaurantrush.entity;

public final class HappyHourPolicy {
    // Expiry does not reset this flag: activation is allowed once per game.
    private boolean activated = false;

    // Includes the current turn while Happy Hour is active.
    private int remainingTurns = 0;
    private final static int DURATION = 2;

    public boolean isActive() {
        return remainingTurns > 0;
    }

    public int getRemainingTurns() {
        return remainingTurns;
    }

    public void activate() {
        if (activated) {
            throw new IllegalStateException("Happy Hour already used");
        }

        activated = true;
        remainingTurns = DURATION;
    }

    // Applies to new bills only; existing orders keep their locked price.
    public int applyDiscount(int billCents) {
        if (billCents < 0) {
            throw new IllegalArgumentException("Negative bill");
        }

        if (isActive()) {
            return Money.percentage(billCents, 80);
        }

        return billCents;
    }

    public void endTurn() {
        if (remainingTurns > 0) {
            remainingTurns--;
        }
    }

    public boolean canActivate() {
        return !activated;
    }
}
