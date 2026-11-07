package restaurantrush.entity;

import java.util.Objects;

public final class Host extends Staff {
    public void seatCustomer(Customer customer, Table table) {
        requireAvailable();
        Objects.requireNonNull(customer);
        Objects.requireNonNull(table).assign(customer);
        customer.setStatus(CustomerStatus.SEATED);
        markActed(); // after seating customer, set host as acted
    }
}
