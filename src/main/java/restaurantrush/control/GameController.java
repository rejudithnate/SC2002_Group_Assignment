package restaurantrush.control;

import java.util.Objects;
import restaurantrush.boundary.GameUI;
import restaurantrush.entity.Restaurant;

// will evaluates the result only after all 18 turns finish
public final class GameController {
    public static final int MAX_TURN = 18;
    // No. of completed turns, if 0 means the session hasnt completed a turn
    private int currentTurn;
    private boolean started;
    private final Restaurant restaurant;
    private final TurnController turnController;
    private final GameUI ui;

    public GameController(Restaurant restaurant, TurnController turnController, GameUI ui) {
        this.restaurant = Objects.requireNonNull(restaurant);
        this.turnController = Objects.requireNonNull(turnController);
        this.ui = Objects.requireNonNull(ui);
        //ensure both controllers use same fresh restaurant session
        if (turnController.getRestaurant() != restaurant || turnController.getCompletedTurns() != 0) {
            throw new IllegalArgumentException("Game and turn controllers must share a fresh restaurant session");
        }
    }
    public void startGame() {
        if (started) { throw new IllegalStateException("Create a new game to play again"); }
        started = true;
        //run the game for up to 18 turns only
        while (currentTurn < MAX_TURN) {
            if (!turnController.runTurn(currentTurn + 1, ui)) {
                ui.displayInputEnded();
                return;
            }
            currentTurn++;
        }
        ui.displayResult(isVictory(), restaurant);
    }

    public boolean isVictory() {
        return currentTurn == MAX_TURN
                && meetsVictoryTargets(restaurant.getPaidCustomerCount(),
                        restaurant.getRevenueCents(), restaurant.getAvgSatisfaction());
    }

    // revenue is expressed in integer cents!!
    public static boolean meetsVictoryTargets(int paidCustomers, int revenueCents, double averageSatisfaction) {
        return paidCustomers >= 4 && revenueCents >= 4500 && averageSatisfaction >= 60;
    }
    public int getCurrentTurn() { 
        return currentTurn; 
    }
}