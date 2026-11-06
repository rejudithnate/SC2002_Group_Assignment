package restaurantrush.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import restaurantrush.boundary.GameUI;
import restaurantrush.entity.Customer;
import restaurantrush.entity.CustomerStatus;
import restaurantrush.entity.Order;
import restaurantrush.entity.OrderStatus;
import restaurantrush.entity.Restaurant;

//TurnController class coordinates phases. Note that manager choices have to be validated before staff member acts
public final class TurnController {
    //defines possible staff actions 
    public enum Task { WAIT, SEAT, TAKE_ORDER, SERVE, PREPARE }

    //use special record class to store data: type of action performed and customer involved
    public record Action(Task task, int customerId) {
        public Action {
            Objects.requireNonNull(task);
            if (task == Task.WAIT ? customerId != 0 : customerId < 1) {
                throw new IllegalArgumentException("An action needs a valid customer ID");
            }
        }
        public static Action waitAction() { return new Action(Task.WAIT, 0); }
    }

    private final Restaurant restaurant;
    private int completedTurns;
    private int activeTurn;

    //create turncontroller for specific restaurant
    public TurnController(Restaurant restaurant) {
        this.restaurant = Objects.requireNonNull(restaurant);
    }

    public Restaurant getRestaurant() { 
        return restaurant;
    }
    public int getCompletedTurns() { 
        return completedTurns;
    }

    //starts next turn in sequential order, prevents turns from being started twice so that players cannot abuse feature to reset staff
    public void beginTurn(int turn) {
        if (activeTurn != 0 || turn != completedTurns + 1 || turn > GameController.MAX_TURN) {
            throw new IllegalStateException("Turns must run once, in order, from 1 to 18");
        }
        //add a new wiating customer on turns 1,4,7 etc.
        if ((turn - 1) % 3 == 0) {
            restaurant.addWaitingCustomer(new Customer((turn - 1) / 3 + 1));
        }
        //get waiter and chef actions in new turn
        restaurant.getWaiter().resetForNewTurn();
        restaurant.getChef().resetForNewTurn();
        activeTurn = turn;
    }

    //return all valid actions the waiter can perform this turn --> depends on customer status and table avail
    public List<Action> getWaiterActions() {
        requireActiveTurn();
        List<Action> actions = new ArrayList<>();
        actions.add(Action.waitAction());
        //check each customer to determine available waiter actions
        for (Customer customer : restaurant.getCustomers()) {
            int id = customer.getCustomerId();
            //seat a waiting customer if a table is avail
            if (customer.getStatus() == CustomerStatus.WAITING && restaurant.getAvailableTable() != null) {
                actions.add(new Action(Task.SEAT, id));
                //take order from seated customer
            } else if (customer.getStatus() == CustomerStatus.SEATED) {
                actions.add(new Action(Task.TAKE_ORDER, id));
                //serve customer whose order has been prepared
            } else if (customer.getStatus() == CustomerStatus.ORDERED
                    && customer.getOrder().getStatus() == OrderStatus.READY) {
                actions.add(new Action(Task.SERVE, id));
            }
        }
        //unmodifiable list
        return List.copyOf(actions);
    }

    //returns all valid chef actions this turn + orders alr being prepared and any new order created by waiter's action
    public List<Action> getChefActions(Action waiterAction) {
        if (!getWaiterActions().contains(waiterAction)) {
            throw new IllegalArgumentException("That waiter action is not available");
        }
        List<Action> actions = new ArrayList<>();
        actions.add(Action.waitAction());
        //check customers for order the chef can prepare
        for (Customer customer : restaurant.getCustomers()) {
            Order order = customer.getOrder();
            if ((order != null && order.getStatus() == OrderStatus.PREPARING)
                    || (waiterAction.task() == Task.TAKE_ORDER
                        && waiterAction.customerId() == customer.getCustomerId())) {
                actions.add(new Action(Task.PREPARE, customer.getCustomerId()));
            }
        }
        //unmodifiable list
        return List.copyOf(actions);
    }

    //executes staff actions + process eating, payments and customer departures + updates turn count after
    public void completeTurn(Action waiterAction, Action chefAction) {
        requireActiveTurn();

        if (!getChefActions(waiterAction).contains(chefAction)) {
            throw new IllegalArgumentException("That chef action is not available");
        }
        //execute the waiter's selected action
        switch (waiterAction.task()) {
            case WAIT -> restaurant.getWaiter().waitTurn();
            case SEAT -> restaurant.seatCustomer(waiterAction.customerId());
            case TAKE_ORDER -> restaurant.takeOrder(waiterAction.customerId());
            case SERVE -> restaurant.serveCustomer(waiterAction.customerId(), activeTurn);
            default -> throw new IllegalArgumentException("Invalid waiter task");
        }
        //execute chef's action after waiter
        if (chefAction.task() == Task.WAIT) {
            restaurant.getChef().waitTurn();
        } else {
            restaurant.getChef().prepareOrder(restaurant.getCustomer(chefAction.customerId()).getOrder());
        }

        //update customer status who hv finished eating
        restaurant.finishEating(activeTurn);
        //process payment for eligible customers
        Customer paying = restaurant.getEligiblePayingCustomer(activeTurn);
        if (paying != null) { restaurant.getCashier().processPayment(paying, restaurant); }
        //update waiting customers and process departure
        restaurant.applyWaitingAndDepartures();
        //mark current turn as completed and clear active turn
        completedTurns = activeTurn;
        activeTurn = 0;
    }

    //runs one complete turn using gui. displays game state, collects staff actions and executes them.
    public boolean runTurn(int turn, GameUI ui) {
        Objects.requireNonNull(ui);
        // Start the turn and display the current restaurant state
        beginTurn(turn);
        ui.displayGameState(restaurant, turn, GameController.MAX_TURN);
        // Get the player's selected waiter action
        Optional<Action> waiterAction = ui.readAction("Waiter", getWaiterActions());
        // Stop if input ends before a waiter action is selected
        if (waiterAction.isEmpty()) { return false; }
        // Get the chef's available actions based on the waiter choice
        Optional<Action> chefAction = ui.readAction("Chef", getChefActions(waiterAction.get()));
        // Stop if input ends before a chef action is selected
        if (chefAction.isEmpty()) { 
            return false;
        }
        // Execute both staff actions and complete the turn
        completeTurn(waiterAction.get(), chefAction.get());
        // Display food selection details if an order was taken
        if (waiterAction.get().task() == Task.TAKE_ORDER) {
            ui.displayFoodSelection(restaurant.getCustomer(waiterAction.get().customerId()));
        }
        // Display the updated game state after the turn
        ui.displayTurnSummary(restaurant, turn, GameController.MAX_TURN);
        return true;
    }
    
    //this ensures turn is active before staff actions are accessed
    private void requireActiveTurn() {
        if (activeTurn == 0) { throw new IllegalStateException("Start a turn before choosing actions"); }
    }
}