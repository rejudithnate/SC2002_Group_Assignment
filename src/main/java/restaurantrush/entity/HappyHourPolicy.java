
package restaurantrush.entity;

public final class HappyHourPolicy {
    private boolean activated = false;
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

