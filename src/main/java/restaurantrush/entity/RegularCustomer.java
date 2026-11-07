package restaurantrush.entity;

//regular customer is just a normal customer, follows the same as Stage 1: lose 8 satisfaction points per wait, pays full price and use default food selection. (inherited behaviours from Customer class)
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
