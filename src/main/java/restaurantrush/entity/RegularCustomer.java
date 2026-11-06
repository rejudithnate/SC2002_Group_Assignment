package restaurantrush.entity;

/**
 * RegularCustomer represents a normal customer.
 *
 * - Loses 8 satisfaction points per waiting turn.
 * - Pays full price.
 * - Uses the default food selection rule.
 *
 * All behaviours are inherited from Customer.
 */
public final class RegularCustomer extends Customer {

    public RegularCustomer(int customerId) {
        super(customerId);
    }
}
