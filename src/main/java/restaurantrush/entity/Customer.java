package restaurantrush.entity;

import java.util.Objects;

public final class Customer {
    private final int customerId;
    private int satisfaction = 100;
    private CustomerStatus status = CustomerStatus.WAITING;
    private Order order;
    private Table table;
    private int servedTurn;
    private int readyToPayTurn;

    public Customer(int customerId) {
        if (customerId < 1) { throw new IllegalArgumentException("Customer ID must be positive"); }
        this.customerId = customerId;
    }

    public int getCustomerId() { return customerId; }
    public int getSatisfaction() { return satisfaction; }
    public CustomerStatus getStatus() { return status; }
    public Order getOrder() { return order; }
    public Table getTable() { return table; }
    public int getServedTurn() { return servedTurn; }
    public int getReadyToPayTurn() { return readyToPayTurn; }

    // customer owns both ends of association withh status, but only restaurant can change it
    void setStatus(CustomerStatus next) {
        Objects.requireNonNull(next);
        boolean allowed = switch (status) {
            case WAITING -> next == CustomerStatus.SEATED || next == CustomerStatus.CANCELLED;
            case SEATED -> next == CustomerStatus.ORDERED || next == CustomerStatus.CANCELLED;
            case ORDERED -> next == CustomerStatus.READY_TO_PAY || next == CustomerStatus.CANCELLED;
            case READY_TO_PAY -> next == CustomerStatus.PAID;
            case PAID, CANCELLED -> false;
        };
        if (!allowed) { throw new IllegalStateException("Invalid customer transition: " + status + " -> " + next); }
        status = next;
    }

    // table owns both ends of association with customer, but only restaurant can change it
    void setTable(Table table) { this.table = table; }

    public void setOrder(Order order) {
        Objects.requireNonNull(order);
        if (this.order != null || status != CustomerStatus.SEATED) {
            throw new IllegalStateException("Only a seated customer without an order can order");
        }
        if (table == null || order.getTableId() != table.getTableId()
                || order.getStatus() != OrderStatus.PREPARING) {
            throw new IllegalArgumentException("Expected a new order for the customer's table");
        }
        this.order = order;
    }

    void recordService(int turn) { servedTurn = turn; }

    void finishEating(int turn) {
        if (status != CustomerStatus.ORDERED || order == null
                || order.getStatus() != OrderStatus.SERVED || servedTurn < 1 || turn <= servedTurn) {
            throw new IllegalStateException("Customer must eat for a full turn");
        }
        setStatus(CustomerStatus.READY_TO_PAY);
        readyToPayTurn = turn;
    }

    public boolean isWaitingForService() {
        return status == CustomerStatus.WAITING || status == CustomerStatus.SEATED
                || (status == CustomerStatus.ORDERED && order.getStatus() != OrderStatus.SERVED);
    }

    public void decreaseSatisfaction() { decreaseSatisfaction(8); }

    public void decreaseSatisfaction(int amount) {
        if (amount < 0) { throw new IllegalArgumentException("Decay cannot be negative"); }
        satisfaction = Math.max(0, satisfaction - amount);
    }
}
