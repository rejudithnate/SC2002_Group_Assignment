import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

import restaurantrush.boundary.GameUI;
import restaurantrush.control.GameController;
import restaurantrush.control.TurnController;
import restaurantrush.control.TurnController.Action;
import restaurantrush.control.TurnController.Task;
import restaurantrush.entity.*;

// Small regression suite for Restaurant Rush. 
public class RestaurantRushTests {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        run("Full game: victory", RestaurantRushTests::testVictory);
        run("Full game: defeat", RestaurantRushTests::testDefeat);
        run("VIP and Critic behaviour", RestaurantRushTests::testCustomers);
        run("ComboMeal price and preparation", RestaurantRushTests::testCombo);
        run("Happy Hour pricing and duration", RestaurantRushTests::testHappyHour);
        run("Happy Hour satisfaction recovery", RestaurantRushTests::testRecovery);
        run("Host and Waiter seat independently", RestaurantRushTests::testSeating);
        run("Order, eating and payment timing", RestaurantRushTests::testPayment);

        System.out.printf("%nRESULT: %d passed, %d failed%n", passed, failed);
        if (failed > 0) System.exit(1);
    }

    private static void run(String name, Runnable test) {
        try {
            test.run();
            passed++;
            System.out.println("PASS  " + name);
        } catch (Exception | AssertionError error) {
            failed++;
            System.out.println("FAIL  " + name + ": " + error.getMessage());
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    // Send saved keystrokes through the real CLI and collect the final game state.
    private record GameRun(Restaurant restaurant, GameController game, String output) { }

    private static GameRun replay(String filename) {
        try {
            String input = Files.readString(Path.of("tests", "inputs", filename));
            Restaurant restaurant = new Restaurant();
            TurnController turns = new TurnController(restaurant);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            try (Scanner scanner = new Scanner(input);
                 PrintStream print = new PrintStream(buffer, true, StandardCharsets.UTF_8)) {
                GameController game = new GameController(
                        restaurant, turns, new GameUI(scanner, print));
                game.startGame();
                return new GameRun(restaurant, game, buffer.toString(StandardCharsets.UTF_8));
            }
        } catch (Exception error) {
            throw new AssertionError("Could not replay " + filename, error);
        }
    }

    private static void testVictory() {
        GameRun result = replay("victory.txt");
        check(result.game().isVictory(), "Expected victory");
        check(result.game().getCurrentTurn() == 18, "Must play 18 turns");
        check(result.restaurant().getArrivalCount() == 6, "Expected 6 arrivals");
        check(result.restaurant().getPaidCustomerCount() == 5, "Expected 5 paid customers");
        check(result.restaurant().getRevenueCents() == 7740, "Expected $77.40");
        check(result.restaurant().getAvgSatisfaction() == 83.0, "Expected average 83");
    }

    private static void testDefeat() {
        GameRun result = replay("defeat.txt");
        check(!result.game().isVictory(), "Expected defeat");
        check(result.game().getCurrentTurn() == 18, "Game must still finish turn 18");
        check(result.restaurant().getRevenueCents() == 0, "No customer paid");
        check(result.restaurant().getUnhappyCustomers() == 2, "Expected 2 abandonments");
    }

    private static void testCustomers() {
        Customer regular = new RegularCustomer(1);
        Customer vip = new VIPCustomer(2);
        Customer critic = new CriticCustomer(3);
        regular.decreaseSatisfaction();
        vip.decreaseSatisfaction();
        critic.decreaseSatisfaction();

        check(regular.getSatisfaction() == 92, "Regular loses 8");
        check(vip.getSatisfaction() == 95, "VIP loses 5");
        check(critic.getSatisfaction() == 88, "Critic loses 12");
        check(vip.calculateBillCents(1500) == 1350, "VIP Burger costs $13.50");
        check(critic.chooseMenuItem(new Menu()).getName().equals("Pasta"),
                "Critic should prefer Pasta");
    }

    private static void testCombo() {
        Menu menu = new Menu();
        MenuItem combo = menu.chooseForCustomer(4);
        check(combo instanceof ComboMeal, "Every fourth customer chooses a combo");
        check(combo.getPriceCents() == 2430, "Combo costs $24.30");
        check(combo.getPreparationUnits() == 3, "Combo takes 3 units");

        Order order = new Order(1, List.of(combo));
        order.prepareUnit();
        order.prepareUnit();
        check(order.getStatus() == OrderStatus.PREPARING, "Not ready after 2 units");
        order.prepareUnit();
        check(order.getStatus() == OrderStatus.READY, "Ready after 3 units");
    }

    private static void testHappyHour() {
        HappyHourPolicy happyHour = new HappyHourPolicy();
        VIPCustomer vip = new VIPCustomer(2);
        happyHour.activate();
        int bill = happyHour.applyDiscount(vip.calculateBillCents(1500));
        check(bill == 1080, "VIP discount then Happy Hour = $10.80");
        Order order = new Order(1, List.of(new SingleDish("Burger", 1500, 1)), bill);
        check(happyHour.getRemainingTurns() == 2, "Starts with 2 turns");
        happyHour.endTurn();
        check(happyHour.getRemainingTurns() == 1, "One turn remains");
        happyHour.endTurn();
        check(!happyHour.isActive(), "Ends after 2 turns");
        check(order.getPriceCents() == 1080, "Existing order price stays locked");
        check(!happyHour.canActivate(), "Cannot activate a second time");
    }

    private static void testRecovery() {
        Restaurant restaurant = new Restaurant();
        TurnController turns = new TurnController(restaurant);

        turns.beginTurn(1);
        turns.performHostAction(new Action(Task.SEAT, 1));
        turns.completeTurn(new Action(Task.TAKE_ORDER, 1), new Action(Task.PREPARE, 1));
        turns.beginTurn(2);
        turns.completeTurn(Action.waitAction(), new Action(Task.PREPARE, 1));
        turns.beginTurn(3);
        turns.completeTurn(new Action(Task.SERVE, 1), Action.waitAction());
        check(restaurant.getCustomer(1).getSatisfaction() == 84, "Served customer at 84");

        turns.beginTurn(4, true);
        check(restaurant.getCustomer(1).getSatisfaction() == 84,
                "Already served customers must not recover");
        turns.completeTurn(Action.waitAction(), Action.waitAction());
        check(restaurant.getCustomer(2).getSatisfaction() == 95, "VIP waiting penalty");
        turns.beginTurn(5);
        check(restaurant.getCustomer(2).getSatisfaction() == 100,
                "Waiting VIP recovers 15, capped at 100");
        turns.completeTurn(Action.waitAction(), Action.waitAction());
    }

    private static void testSeating() {
        Restaurant restaurant = new Restaurant();
        TurnController turns = new TurnController(restaurant);
        for (int turn = 1; turn <= 3; turn++) {
            turns.beginTurn(turn);
            turns.completeTurn(Action.waitAction(), Action.waitAction());
        }
        turns.beginTurn(4); // Customer 2 arrives here.
        turns.performHostAction(new Action(Task.SEAT, 1));
        check(turns.getWaiterActions().contains(new Action(Task.SEAT, 2)),
                "Waiter must still be allowed to seat customer 2");
        turns.completeTurn(new Action(Task.SEAT, 2), Action.waitAction());
        check(restaurant.getTables().get(0).getCustomer().getCustomerId() == 1,
                "Host seats customer 1");
        check(restaurant.getTables().get(1).getCustomer().getCustomerId() == 2,
                "Waiter seats customer 2");
    }

    private static void testPayment() {
        Restaurant restaurant = new Restaurant();
        TurnController turns = new TurnController(restaurant);
        turns.beginTurn(1);
        turns.performHostAction(new Action(Task.SEAT, 1));
        turns.completeTurn(new Action(Task.TAKE_ORDER, 1), new Action(Task.PREPARE, 1));
        turns.beginTurn(2);
        check(!turns.getWaiterActions().contains(new Action(Task.SERVE, 1)),
                "Cannot serve an unprepared order");
        turns.completeTurn(Action.waitAction(), new Action(Task.PREPARE, 1));
        turns.beginTurn(3);
        turns.completeTurn(new Action(Task.SERVE, 1), Action.waitAction());
        check(restaurant.getRevenueCents() == 0, "Cannot pay on serving turn");
        turns.beginTurn(4);
        turns.completeTurn(Action.waitAction(), Action.waitAction());
        check(restaurant.getRevenueCents() == 0, "Cannot pay on eating turn");
        turns.beginTurn(5);
        turns.completeTurn(Action.waitAction(), Action.waitAction());
        check(restaurant.getRevenueCents() == 1800, "Pasta payment is $18.00");
        check(restaurant.getCustomer(1).getStatus() == CustomerStatus.PAID,
                "Customer should be marked PAID");
        check(restaurant.getTables().get(0).isAvailable(), "Table is freed after payment");
    }
}
