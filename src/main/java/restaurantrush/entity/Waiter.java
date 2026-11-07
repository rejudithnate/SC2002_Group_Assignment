package restaurantrush.entity;
import java.util.Objects;

public final class Waiter extends Staff {

    // seats customer to table
    public void sitCustomer(Customer customer, Table table) {
        requireAvailable();

        Objects.requireNonNull(customer);
        Objects.requireNonNull(table);

        // only sit weating customers
        if (customer.getStatus() != CustomerStatus.WAITING) {
            throw new IllegalStateException("Customer must be waiting");
        }

        // assign the table to the customer and update the status
        table.assign(customer);
        customer.setStatus(CustomerStatus.SEATED);
        markActed();
    }

    // take order from seated customer
    public void takeOrder(Customer customer) {
        requireAvailable();

        Objects.requireNonNull(customer);

        if (customer.getStatus() != CustomerStatus.SEATED || customer.getOrder() == null
                || customer.getOrder().getStatus() != OrderStatus.PREPARING) {
            throw new IllegalStateException("Customer must be seated with a new order");
        }

        // update customer status 
        customer.setStatus(CustomerStatus.ORDERED);
        markActed();
    }

    public void serveOrder(Order order) {

        requireAvailable();

        // mark order as served
        Objects.requireNonNull(order).markServed();
        markActed();
    }
}