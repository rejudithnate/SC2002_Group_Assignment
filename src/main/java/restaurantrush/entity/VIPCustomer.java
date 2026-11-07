package restaurantrush.entity;

//this is a class for VIP customer, which loses 5 satisfaction points per waiting turn, and uses the normal food selction. However, it receives a 10% discount
public final class VIPCustomer extends Customer {

    public VIPCustomer(int customerId) {
        super(customerId);
    }

    //this overrides normal satisfaction loss to account for loss of 5 points per wait
    @Override
    protected int getSatisfactionLossPerTurn() {
        return 5;
    }

    //this overrides normal bill calculation for VIp customers to receive 10% discount. prcies should be stored in integer cents
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

    //returns customer type for game UI
    @Override
    public String getCustomerType() {
        return "VIP";
    }
}
