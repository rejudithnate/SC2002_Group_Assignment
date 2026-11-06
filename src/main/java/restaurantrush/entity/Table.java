package restaurantrush.entity;

public final class Table {

    // Keyword final used to prevent variable from being reassigned
    private final int tableId; 

    // Customer currently occupying this table. null means the table is empty.
    private Customer customer;

    // Creates a table with a unique ID
    public Table(int tableId) {
        if (tableId < 1) { throw new IllegalArgumentException("Table ID must be positive"); }
        this.tableId = tableId;
        this.customer = null;
    }

    // return table ID
    public int getTableId() {
        return tableId;
    }

    // return customer currently seated at the table
    public Customer getCustomer() {
        return customer;
    }

    // Returns true if no customer
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

        // update table and customer
        this.customer = customer;
        customer.setTable(this);
    }

    // removes customer from table 
    public void unAssign() {
        if (customer != null) {
            
            Customer previousCustomer = customer;
            this.customer = null;

            // Set previous customer's table to nothing
            previousCustomer.setTable(null);
        }
    }
}