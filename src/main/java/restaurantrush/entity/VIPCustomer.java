package restaurantrush.entity;

// Loses 5 satisfaction per waiting turn and receives a 10% bill discount.
public final class VIPCustomer extends Customer {
    public VIPCustomer(int customerId) {
        super(customerId);
    }

    @Override
    protected int getSatisfactionLossPerTurn() {
        return 5;
    }

    @Override
    public int calculateBillCents(int basePriceCents) {
        if (basePriceCents < 0) {
            throw new IllegalArgumentException(
                "Price cannot be negative"
            );
        }

        // Round the discounted bill to the nearest cent.
        return Math.toIntExact(
            (basePriceCents * 9L + 5L) / 10L
        );
    }

    @Override
    public String getCustomerType() {
        return "VIP";
    }
}
