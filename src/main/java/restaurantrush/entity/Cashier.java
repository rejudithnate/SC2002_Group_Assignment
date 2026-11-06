package restaurantrush.entity;
import java.util.Objects;

public final class Cashier {

    // handles payment for customer
    public void processPayment(Customer customer, Restaurant restaurant) {

        // Check that is not null
        // Restuarnt handles updating the revenue and customer status
        Objects.requireNonNull(restaurant).completePayment(customer);
    }
}