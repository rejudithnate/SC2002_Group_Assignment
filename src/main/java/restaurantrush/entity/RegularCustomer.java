package restaurantrush.entity;

// Uses the default food choice and full-price bill, losing 8 satisfaction per wait.
public final class RegularCustomer extends Customer {
    public RegularCustomer(int customerId) {
        super(customerId);
    }

    @Override
    protected int getSatisfactionLossPerTurn() {
        return 8;
    }

    @Override
    public String getCustomerType() {
        return "Regular";
    }
}
