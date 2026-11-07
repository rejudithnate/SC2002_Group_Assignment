package restaurantrush.entity;

import java.util.Objects;
//CriticCustomer is a food critic that loses 12 satisfaction points per waiting turn, and has a special trait of always choosing Pasta as preferred dish. CriticCustomer pays full price, no discounts.

public final class CriticCustomer extends Customer {

    public CriticCustomer(int customerId) {
        super(customerId);
    }

    
    //this ovverides the normal satisfaction loss to account for the special loss of 12 points per wait
    @Override
    protected int getSatisfactionLossPerTurn() {
        return 12;
    }

    //this overrides normal food selection to always prefer Pasta
    @Override
    public MenuItem chooseMenuItem(Menu menu) {
        return Objects.requireNonNull(menu)
                .getMenuItems()
                .stream()
                .filter(item ->
                    item.getName().equals("Pasta")
                )
                .findFirst()
                .orElseThrow(() ->
                    new IllegalStateException(
                        "Critic's preferred item is not on the menu"
                    )
                );
    }

    //returns customer type of game ui
    @Override
    public String getCustomerType() {
        return "Critic";
    }
}
