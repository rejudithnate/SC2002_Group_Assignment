package restaurantrush;

import java.util.Scanner;

import restaurantrush.boundary.GameUI;
import restaurantrush.control.GameController;
import restaurantrush.control.TurnController;
import restaurantrush.entity.Restaurant;

public final class Main {
    private Main() { }

    public static void main(String[] args) {
        Restaurant restaurant = new Restaurant();
        try (Scanner scanner = new Scanner(System.in)) {
            GameUI ui = new GameUI(scanner, System.out);
            GameController game = new GameController(restaurant, new TurnController(restaurant), ui);
            game.startGame();
        }
    }
}
