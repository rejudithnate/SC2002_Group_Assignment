package restaurantrush.entity;

import java.util.*;

public final class Restaurant {
    public static final int TABLE_COUNT = 2;
    public static final int MAX_ORDERS = 6;

    private int revenueCents = 0;

    private final List<Table> tables = new ArrayList<>();
    private final List<Order> orders = new ArrayList<>();
    // Keep departed customers for lookup; insertion order preserves arrival order.
    private final Map<Integer, Customer> customers = new LinkedHashMap<>();

    private final Waiter waiter = new Waiter();
    private final Chef chef = new Chef();
    private final Cashier cashier = new Cashier();
    private final Host host = new Host();
    private final Menu menu = new Menu();

    private final Queue<Customer> waitingQueue = new LinkedList<>();
    private final Queue<Customer> paymentQueue = new LinkedList<>();
    private final HappyHourPolicy happyHour = new HappyHourPolicy();

    private int unhappyDepartures = 0;
    private int servedCustomers = 0;
    private int paidCustomers = 0;
    private int totalPaidSatisfaction = 0;

    public Restaurant() {
        tables.add(new Table(1));
        tables.add(new Table(2));
    }

    public double getRevenue() {
        return revenueCents / 100.0;
    }

    public int getRevenueCents() {
        return revenueCents;
    }

    public List<Table> getTables() {
        return List.copyOf(tables);
    }

    public List<Order> getOrders() {
        return List.copyOf(orders);
    }

    public Host getHost() {
        return host;
    }

    public Waiter getWaiter() {
        return waiter;
    }

    public Chef getChef() {
        return chef;
    }

    public Cashier getCashier() {
        return cashier;
    }

    public List<Customer> getWaitingCustomers() {
        return new ArrayList<>(waitingQueue);
    }

    public List<Customer> getPayingCustomers() {
        return new ArrayList<>(paymentQueue);
    }

    public int getArrivalCount() {
        return customers.size();
    }

    public int getUnhappyCustomers() {
        return unhappyDepartures;
    }

    public int getServedCustomers() {
        return servedCustomers;
    }

    public int getPaidCustomerCount() {
        return paidCustomers;
    }

    public double getAvgSatisfaction() {
        if (paidCustomers == 0) {
            return 0;
        }

        return (double) totalPaidSatisfaction / paidCustomers;
    }

    public HappyHourPolicy getHappyHour() {
        return happyHour;
    }

    // Exclude paid and departed customers from active-turn updates.
    public List<Customer> getCustomers() {
        List<Customer> currentCustomers = new ArrayList<>();

        for (Customer customer : customers.values()) {
            if (customer.getStatus() != CustomerStatus.PAID && customer.getStatus() != CustomerStatus.CANCELLED) {
                currentCustomers.add(customer);
            }
        }

        return currentCustomers;
    }

    public Customer getCustomer(int id) {
        return customers.get(id);
    }

    public void addWaitingCustomer(Customer customer) {
        if (customer == null || customers.size() >= 6) {
            return;
        }
        if (getCustomer(customer.getCustomerId()) != null) {
            return;
        }

        if (customer.getStatus() != CustomerStatus.WAITING || customer.getTable() != null) {
            return;
        }

        customers.put(customer.getCustomerId(), customer);
        waitingQueue.add(customer);
    }

    public Table getAvailableTable() {
        for (Table table : tables) {
            if (table.isAvailable()) {
                return table;
            }
        }
        return null;
    }

    // Seating here spends the Waiter's task.
    public void seatCustomer(int id) {
        Customer customer = getCustomer(id);
        Table table = getAvailableTable();

        if (customer == null || table == null
                || !waitingQueue.contains(customer)) {
            throw new IllegalArgumentException(
                "Customer cannot be seated"
            );
        }

        waiter.sitCustomer(customer, table);
        waitingQueue.remove(customer);
    }

    // Host seating leaves the Waiter's task available.
    public void seatCustomerByHost(int id) {
        Customer customer = getCustomer(id);
        Table table = getAvailableTable();

        if (customer == null || table == null
                || !waitingQueue.contains(customer)) {
            throw new IllegalArgumentException(
                "Customer cannot be seated"
            );
        }

        host.sitCustomer(customer, table);
        waitingQueue.remove(customer);
    }

    public List<Order> getPendingOrders() {
        List<Order> pending = new ArrayList<>();

        for (Order order : orders) {
            if (order.getStatus() == OrderStatus.PREPARING) {
                pending.add(order);
            }
        }

        return pending;
    }

    public Order takeOrder(int id) {
        Customer customer = getCustomer(id);

        if (customer == null || !waiter.isAvailable()) {
            return null;
        }

        if (customer.getStatus() != CustomerStatus.SEATED
                || customer.getOrder() != null
                || customer.getTable() == null
                || orders.size() >= MAX_ORDERS) {
            return null;
        }

        MenuItem selected = customer.chooseMenuItem(menu);

        int priceCents = customer.calculateBillCents(
                selected.getPriceCents()
        );

        // Apply Happy Hour after the customer discount, then lock the bill in the order.
        int finalPrice = happyHour.applyDiscount(priceCents);

        Order order = new Order(
                customer.getTable().getTableId(),
                List.of(selected),
                finalPrice
        );

        customer.setOrder(order);
        orders.add(order);
        waiter.takeOrder(customer);

        return order;
    }

    public void serveCustomer(int id, int turn) {
        Customer customer = getCustomer(id);

        if (customer == null || turn < 1) {
            return;
        }

        if (customer.getStatus() != CustomerStatus.ORDERED
                || customer.getOrder() == null
                || !orders.contains(customer.getOrder())) {
            return;
        }

        waiter.serveOrder(customer.getOrder());
        customer.recordService(turn);
        servedCustomers++;
    }

    // A customer served this turn has not yet had a full turn to eat.
    public void finishEating(int turn) {
        for (Customer customer : getCustomers()) {
            if (customer.getStatus() == CustomerStatus.ORDERED
                    && customer.getOrder() != null
                    && customer.getOrder().getStatus() == OrderStatus.SERVED
                    && customer.getServedTurn() < turn) {
                customer.finishEating(turn);
                paymentQueue.add(customer);
            }
        }
    }

    // Only the oldest queued customer can pay, after their ready-to-pay turn.
    public Customer getEligiblePayingCustomer(int turn) {
        Customer customer = paymentQueue.peek();

        if (customer != null && customer.getReadyToPayTurn() < turn) {
            return customer;
        }

        return null;
    }

    public Customer getNextPayingCustomer() {
        return paymentQueue.peek();
    }

    // Unpaid orders never contribute to revenue or average satisfaction.
    public void completePayment(Customer customer) {
        if (customer == null || paymentQueue.peek() != customer) {
            return;
        }

        if (getCustomer(customer.getCustomerId()) != customer
                || customer.getStatus() != CustomerStatus.READY_TO_PAY
                || customer.getOrder() == null
                || customer.getTable() == null) {
            return;
        }

        Order order = customer.getOrder();

        if (!orders.contains(order)
                || order.getStatus() != OrderStatus.SERVED
                || customer.getTable().getCustomer() != customer) {
            return;
        }

        revenueCents += order.getPriceCents();

        order.markPaid();
        customer.setStatus(CustomerStatus.PAID);

        paidCustomers++;

        // Snapshot satisfaction at payment for the final average.
        totalPaidSatisfaction += customer.getSatisfaction();

        paymentQueue.remove();
        customer.getTable().unAssign();
        orders.remove(order);
    }

    // Served customers stop losing satisfaction, even while waiting to pay.
    public void applyWaitingAndDepartures() {
        for (Customer customer : getCustomers()) {
            if (customer.isWaitingForService()) {
                customer.decreaseSatisfaction();

                if (customer.getSatisfaction() == 0) {
                    abandon(customer);
                }
            }
        }
    }

    // Abandonment cancels unfinished food and releases the table without payment.
    private void abandon(Customer customer) {
        Order order = customer.getOrder();

        if (order != null) {
            order.cancel();
            orders.remove(order);
        }

        waitingQueue.remove(customer);

        if (customer.getTable() != null) {
            customer.getTable().unAssign();
        }

        customer.setStatus(CustomerStatus.CANCELLED);
        unhappyDepartures++;
    }

    public void activateHappyHour() {
        happyHour.activate();
    }

    // Recover only customers still waiting for a table or food.
    public void applyHappyHourRecovery() {
        if (!happyHour.isActive()) {
            return;
        }

        for (Customer customer : getCustomers()) {
            if (customer.isWaitingForService()) {
                customer.recoverSatisfaction(15);
            }
        }
    }

    public boolean canActivateHappyHour() {
        return happyHour.canActivate();
    }

    public boolean isHappyHourActive() {
        return happyHour.isActive();
    }

    public int getHappyHourRemainingTurns() {
        return happyHour.getRemainingTurns();
    }

    public void endHappyHourTurn() {
        happyHour.endTurn();
    }
}
