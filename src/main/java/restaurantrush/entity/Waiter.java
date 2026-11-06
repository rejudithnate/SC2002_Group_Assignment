package restaurantrush.entity;
import java.util.Objects;

public final class Waiter {

    // track if waiter has acted
    private boolean hasActed;

    // check if waiter is available to act
    public boolean isAvailable() { 
        return !hasActed;
    }

    // resets the waiters' action status at the start of each turn
    public void resetForNewTurn() { 
        hasActed = false; 
    }

    // prevents waiter from performing more than one action
    private void requireAvailable() {
        if (hasActed) { 
            throw new IllegalStateException("Waiter already acted this turn"); 
        }
    }

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
        hasActed = true;
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
        hasActed = true;
    }

    public void serveOrder(Order order) {

        requireAvailable();

        // mark order as served
        Objects.requireNonNull(order).markServed();
        hasActed = true;
    }

    // skip waiter turn
    public void waitTurn() {

        requireAvailable();
        hasActed = true;
    }
}