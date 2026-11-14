package restaurantrush.entity;

import java.util.Objects;

public final class Waiter extends Staff {
    public void sitCustomer(Customer customer, Table table) {
        requireAvailable();

        Objects.requireNonNull(customer);
        Objects.requireNonNull(table);

        if (customer.getStatus() != CustomerStatus.WAITING) {
            throw new IllegalStateException("Customer must be waiting");
        }

        table.assign(customer);
        customer.setStatus(CustomerStatus.SEATED);
        markActed();
    }

    // Restaurant creates and attaches the order before the Waiter records it.
    public void takeOrder(Customer customer) {
        requireAvailable();

        Objects.requireNonNull(customer);

        if (customer.getStatus() != CustomerStatus.SEATED || customer.getOrder() == null
                || customer.getOrder().getStatus() != OrderStatus.PREPARING) {
            throw new IllegalStateException("Customer must be seated with a new order");
        }

        customer.setStatus(CustomerStatus.ORDERED);
        markActed();
    }

    public void serveOrder(Order order) {
        requireAvailable();

        Objects.requireNonNull(order).markServed();
        markActed();
    }
}
