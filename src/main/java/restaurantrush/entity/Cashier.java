package restaurantrush.entity;

import java.util.Objects;

public final class Cashier {
    public void processPayment(Customer customer, Restaurant restaurant) {
        // Restaurant validates the queue and updates payment records together.
        Objects.requireNonNull(restaurant).completePayment(customer);
    }
}
