package restaurantrush.entity;

import java.util.Objects;

public final class Host extends Staff {
    public void sitCustomer(Customer customer, Table table) {

        requireAvailable();
        
        Objects.requireNonNull(table).assign(Objects.requireNonNull(customer));
        customer.setStatus(CustomerStatus.SEATED);
        markActed();
    }
}
