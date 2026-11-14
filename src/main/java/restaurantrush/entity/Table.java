package restaurantrush.entity;

public final class Table {
    private final int tableId;

    // A null customer means this table is free.
    private Customer customer;

    public Table(int tableId) {
        if (tableId < 1) {
            throw new IllegalArgumentException("Table ID must be positive");
        }
        this.tableId = tableId;
        this.customer = null;
    }

    public int getTableId() {
        return tableId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public boolean isAvailable() {
        return customer == null;
    }

    public void assign(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null.");
        }
        if (!isAvailable()) {
            throw new IllegalStateException("Table is already occupied.");
        }
        if (customer.getTable() != null) {
            throw new IllegalStateException("Customer already has a table.");
        }
        if (customer.getStatus() != CustomerStatus.WAITING) {
            throw new IllegalStateException("Only waiting customers can be seated");
        }

        // Keep the table and customer references in step.
        this.customer = customer;
        customer.setTable(this);
    }

    // Called after payment or abandonment to release both references.
    public void unAssign() {
        if (customer != null) {
            Customer previousCustomer = customer;
            this.customer = null;

            previousCustomer.setTable(null);
        }
    }
}
