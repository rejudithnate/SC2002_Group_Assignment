package restaurantrush.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import restaurantrush.boundary.GameUI;
import restaurantrush.entity.CriticCustomer;
import restaurantrush.entity.Customer;
import restaurantrush.entity.CustomerStatus;
import restaurantrush.entity.Order;
import restaurantrush.entity.OrderStatus;
import restaurantrush.entity.RegularCustomer;
import restaurantrush.entity.Restaurant;
import restaurantrush.entity.Staff;
import restaurantrush.entity.VIPCustomer;

// Coordinates arrivals, staff actions, payment, and waiting for each turn.
public final class TurnController {
    public enum Task { WAIT, SEAT, TAKE_ORDER, SERVE, PREPARE }

    // Customer ID 0 is reserved for the Wait action.
    public record Action(Task task, int customerId) {
        public Action {
            Objects.requireNonNull(task);
            if (task == Task.WAIT ? customerId != 0 : customerId < 1) {
                throw new IllegalArgumentException("An action needs a valid customer ID");
            }
        }

        public static Action waitAction() {
            return new Action(Task.WAIT, 0);
        }
    }

    private final Restaurant restaurant;
    private int completedTurns;
    private int activeTurn;

    public TurnController(Restaurant restaurant) {
        this.restaurant = Objects.requireNonNull(restaurant);
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public int getCompletedTurns() {
        return completedTurns;
    }

    // Reject repeated or overlapping turns before resetting the staff.
    public void beginTurn(int turn) {
        beginTurn(turn, false);
    }

    public void beginTurn(int turn, boolean activateHappyHour) {
        if (activeTurn != 0 || turn != completedTurns + 1
                || turn > GameController.MAX_TURN) {
            throw new IllegalStateException("Invalid turn");
        }

        if (activateHappyHour) {
            restaurant.activateHappyHour();
        }

        // Fixed arrivals keep the base setting reproducible.
        if ((turn - 1) % 3 == 0) {
            int customerId = (turn - 1) / 3 + 1;

            Customer arrivingCustomer = switch (customerId) {
                case 2 -> new VIPCustomer(customerId);
                case 3 -> new CriticCustomer(customerId);
                default -> new RegularCustomer(customerId);
            };

            restaurant.addWaitingCustomer(arrivingCustomer);
        }

        // Reset staff for the new turn
        for (Staff staff : List.of(
                restaurant.getHost(),
                restaurant.getWaiter(),
                restaurant.getChef())) {
            staff.resetForNewTurn();
        }

        // Recover after arrivals, before the manager chooses staff actions.
        restaurant.applyHappyHourRecovery();

        activeTurn = turn;
    }

    public List<Action> getHostActions() {
        requireActiveTurn();
        if (!restaurant.getHost().isAvailable()) {
            throw new IllegalStateException("Host already acted");
        }
        List<Action> actions = new ArrayList<>();
        actions.add(Action.waitAction());
        if (restaurant.getAvailableTable() != null) {
            for (Customer customer : restaurant.getWaitingCustomers()) {
                actions.add(new Action(Task.SEAT, customer.getCustomerId()));
            }
        }
        return List.copyOf(actions);
    }

    public void performHostAction(Action action) {
        if (!getHostActions().contains(action)) {
            throw new IllegalArgumentException("Invalid Host action");
        }

        if (action.task() == Task.WAIT) {
            restaurant.getHost().waitTurn();
        } else {
            restaurant.seatCustomerByHost(action.customerId());
        }
    }

    // Choices reflect any seating the Host has already completed.
    public List<Action> getWaiterActions() {

        requireActiveTurn();
        List<Action> actions = new ArrayList<>();
        actions.add(Action.waitAction());

        for (Customer customer : restaurant.getCustomers()) {
            int id = customer.getCustomerId();

            if (customer.getStatus() == CustomerStatus.WAITING && restaurant.getAvailableTable() != null) {
                actions.add(new Action(Task.SEAT, id));

            } else if (customer.getStatus() == CustomerStatus.SEATED) {
                actions.add(new Action(Task.TAKE_ORDER, id));

            } else if (customer.getStatus() == CustomerStatus.ORDERED
                    && customer.getOrder().getStatus() == OrderStatus.READY) {
                actions.add(new Action(Task.SERVE, id));
            }
        }
        return List.copyOf(actions);
    }

    // The Chef may also work on the order the Waiter is about to take.
    public List<Action> getChefActions(Action waiterAction) {

        if (!getWaiterActions().contains(waiterAction)) {
            
            throw new IllegalArgumentException("That waiter action is not available");
        }
        List<Action> actions = new ArrayList<>();
        actions.add(Action.waitAction());

        for (Customer customer : restaurant.getCustomers()) {
            Order order = customer.getOrder();

            if ((order != null && order.getStatus() == OrderStatus.PREPARING)
                    || (waiterAction.task() == Task.TAKE_ORDER
                            && waiterAction.customerId() == customer.getCustomerId())) {
                actions.add(new Action(Task.PREPARE, customer.getCustomerId()));
            }
        }

        return List.copyOf(actions);
    }

    // Validate both choices before spending the Waiter or Chef task.
    public void completeTurn(Action waiterAction, Action chefAction) {
        requireActiveTurn();

        if (!getChefActions(waiterAction).contains(chefAction)) {
            throw new IllegalArgumentException("That chef action is not available");
        }

        // Treat an omitted Host action as Wait.
        if (restaurant.getHost().isAvailable()) {
            performHostAction(Action.waitAction());
        }

        switch (waiterAction.task()) {
            case WAIT -> restaurant.getWaiter().waitTurn();
            case SEAT -> restaurant.seatCustomer(waiterAction.customerId());
            case TAKE_ORDER -> restaurant.takeOrder(waiterAction.customerId());
            case SERVE -> restaurant.serveCustomer(waiterAction.customerId(), activeTurn);
            default -> throw new IllegalArgumentException("Invalid waiter task");
        }

        // Food finished by the Chef cannot be served until a later turn.
        if (chefAction.task() == Task.WAIT) {

            restaurant.getChef().waitTurn();
        } else {

            restaurant.getChef().prepareOrder(restaurant.getCustomer(chefAction.customerId()).getOrder());
        }

        restaurant.finishEating(activeTurn);

        // Diners who became ready to pay this turn must wait until the next.
        Customer paying = restaurant.getEligiblePayingCustomer(activeTurn);

        if (paying != null) {
            restaurant.getCashier().processPayment(paying, restaurant);
        }

        restaurant.applyWaitingAndDepartures();
        restaurant.endHappyHourTurn();
        completedTurns = activeTurn;
        activeTurn = 0;
    }

    // Returns false if input ends before the turn is completed.
    public boolean runTurn(int turn, GameUI ui) {
        
        Objects.requireNonNull(ui);
        boolean activate = false;

        if (restaurant.getHappyHour().canActivate()) {
            Optional<Boolean> choice = ui.readHappyHourChoice(turn);

            if (choice.isEmpty()) {
                return false;
            }

            activate = choice.get();
        }

        beginTurn(turn, activate);
        ui.displayGameState(restaurant, turn, GameController.MAX_TURN);

        // Seat first so the Waiter can take the newly seated customer's order.
        Optional<Action> hostAction = ui.readAction("Host", getHostActions());
        if (hostAction.isEmpty()) {
            return false;
        }
        performHostAction(hostAction.get());

        Optional<Action> waiterAction = ui.readAction("Waiter", getWaiterActions());
        if (waiterAction.isEmpty()) {
            return false;
        }

        Optional<Action> chefAction = ui.readAction("Chef", getChefActions(waiterAction.get()));
        if (chefAction.isEmpty()) {
            return false;
        }

        completeTurn(waiterAction.get(), chefAction.get());
        if (waiterAction.get().task() == Task.TAKE_ORDER) {
            ui.displayFoodSelection(restaurant.getCustomer(waiterAction.get().customerId()));
        }

        ui.displayTurnSummary(restaurant, turn, GameController.MAX_TURN);
        return true;
    }

    private void requireActiveTurn() {
        if (activeTurn == 0) {
            throw new IllegalStateException("Start a turn before choosing actions");
        }
    }
}
