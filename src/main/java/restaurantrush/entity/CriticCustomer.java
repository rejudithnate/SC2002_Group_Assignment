package restaurantrush.entity;

import java.util.Objects;

// Loses 12 satisfaction per waiting turn and always chooses Pasta.
public final class CriticCustomer extends Customer {
    public CriticCustomer(int customerId) {
        super(customerId);
    }

    @Override
    protected int getSatisfactionLossPerTurn() {
        return 12;
    }

    @Override
    public MenuItem chooseMenuItem(Menu menu) {
        return Objects.requireNonNull(menu).getPasta();
    }

    @Override
    public String getCustomerType() {
        return "Critic";
    }
}
