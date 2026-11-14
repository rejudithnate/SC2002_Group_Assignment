package restaurantrush.entity;

import java.util.Objects;

public abstract class Customer {
    private final int customerId;
    private int satisfaction = 100;
    private CustomerStatus status = CustomerStatus.WAITING;
    private Order order;
    private Table table;
    private int servedTurn;
    private int readyToPayTurn;

    public Customer(int customerId) {
        if (customerId < 1) {
            throw new IllegalArgumentException("Customer ID must be positive");
        }
        this.customerId = customerId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public int getSatisfaction() {
        return satisfaction;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    public Order getOrder() {
        return order;
    }

    public Table getTable() {
        return table;
    }

    public int getServedTurn() {
        return servedTurn;
    }

    public int getReadyToPayTurn() {
        return readyToPayTurn;
    }

    // Keep status changes inside the entity package and reject invalid transitions.
    void setStatus(CustomerStatus next) {
        Objects.requireNonNull(next);
        boolean allowed = switch (status) {
            case WAITING -> next == CustomerStatus.SEATED || next == CustomerStatus.CANCELLED;
            case SEATED -> next == CustomerStatus.ORDERED || next == CustomerStatus.CANCELLED;
            case ORDERED -> next == CustomerStatus.READY_TO_PAY || next == CustomerStatus.CANCELLED;
            case READY_TO_PAY -> next == CustomerStatus.PAID;
            case PAID, CANCELLED -> false;
        };
        if (!allowed) {
            throw new IllegalStateException("Invalid customer transition: " + status + " -> " + next);
        }
        status = next;
    }

    // Table.assign() and unAssign() update both sides of the table association.
    void setTable(Table table) {
        this.table = table;
    }

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

    // Remember when service happened so eating takes a full turn.
    void recordService(int turn) {
        servedTurn = turn;
    }

    void finishEating(int turn) {
        if (status != CustomerStatus.ORDERED || order == null
                || order.getStatus() != OrderStatus.SERVED || servedTurn < 1 || turn <= servedTurn) {
            throw new IllegalStateException("Customer must eat for a full turn");
        }
        setStatus(CustomerStatus.READY_TO_PAY);
        readyToPayTurn = turn;
    }

    // READY food still counts as waiting until the Waiter serves it.
    public boolean isWaitingForService() {
        return status == CustomerStatus.WAITING || status == CustomerStatus.SEATED
                || (status == CustomerStatus.ORDERED && order.getStatus() != OrderStatus.SERVED);
    }

    protected abstract int getSatisfactionLossPerTurn();

    // Full price by default; VIP customers override this calculation.
    public int calculateBillCents(int basePriceCents) {
        if (basePriceCents < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        return basePriceCents;
    }

    // Customer subclasses may override the menu preference.
    public MenuItem chooseMenuItem(Menu menu) {
        return Objects.requireNonNull(menu).chooseForCustomer(customerId);
    }

    public String getCustomerType() {
        return "Regular";
    }

    public void decreaseSatisfaction() {
        decreaseSatisfaction(getSatisfactionLossPerTurn());
    }

    public void decreaseSatisfaction(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Decay cannot be negative");
        }
        satisfaction = Math.max(0, satisfaction - amount);
    }

    public void recoverSatisfaction(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException(
                "Recovery cannot be negative"
            );
        }

        satisfaction = Math.min(100, satisfaction + amount);
    }
}
