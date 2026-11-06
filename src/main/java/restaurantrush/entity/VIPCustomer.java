package restaurantrush.entity;

/**
 * VIPCustomer represents a VIP customer.
 *
 * - Loses 5 satisfaction points per waiting turn.
 * - Receives a 10% discount on orders.
 * - Uses the normal food selection rule.
 */
public final class VIPCustomer extends Customer {

    public VIPCustomer(int customerId) {
        super(customerId);
    }

    /**
     * Override the normal satisfaction loss.
     * VIP customers lose only 5 points per turn.
     */
    @Override
    public int getSatisfactionLossPerTurn() {
        return 5;
    }

    /**
     * Override the normal bill calculation.
     * VIP customers receive a 10% discount.
     *
     * Prices are stored in integer cents.
     */
    @Override
    public int calculateBillCents(int basePriceCents) {
        if (basePriceCents < 0) {
            throw new IllegalArgumentException(
                "Price cannot be negative"
            );
        }

        // Apply 10% discount with cent rounding.
        return Math.toIntExact(
            (basePriceCents * 9L + 5L) / 10L
        );
    }

    /**
     * Return the customer type for the UI.
     */
    @Override
    public String getCustomerType() {
        return "VIP";
    }
}