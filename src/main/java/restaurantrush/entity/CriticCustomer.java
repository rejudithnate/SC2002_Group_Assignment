package restaurantrush.entity;

import java.util.Objects;

/**
 * CriticCustomer represents a food critic.
 *
 * - Loses 12 satisfaction points per waiting turn.
 * - Always chooses Pasta as the preferred dish.
 * - Pays full price.
 */
public final class CriticCustomer extends Customer {

    public CriticCustomer(int customerId) {
        super(customerId);
    }

    /**
     * Override the normal satisfaction loss.
     * Critics lose 12 points per waiting turn.
     */
    @Override
    public int getSatisfactionLossPerTurn() {
        return 12;
    }

    /**
     * Override the normal food selection.
     *
     * Special Critic behaviour:
     * Critics always prefer Pasta.
     */
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

    /**
     * Return the customer type for the UI.
     */
    @Override
    public String getCustomerType() {
        return "Critic";
    }
}