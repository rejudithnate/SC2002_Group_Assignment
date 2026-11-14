package restaurantrush.boundary;

import java.io.PrintStream;
import java.util.Locale;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;

import restaurantrush.control.TurnController.Action;
import restaurantrush.entity.Customer;
import restaurantrush.entity.MenuItem;
import restaurantrush.entity.Order;
import restaurantrush.entity.Restaurant;
import restaurantrush.entity.Table;

// Displays state and reads choices; the controller supplies the available actions.
public final class GameUI {
    private static final int LINE_WIDTH = 72;

    private final Scanner scanner;
    private final PrintStream output;

    public GameUI(Scanner scanner, PrintStream output) {
        this.scanner = Objects.requireNonNull(scanner);
        this.output = Objects.requireNonNull(output);
    }

   public void displayGameState(Restaurant restaurant, int turn, int maxTurns) {
        printBanner(String.format("RESTAURANT RUSH  |  TURN %02d / %02d", turn, maxTurns));

        displayMetrics(restaurant);
        displayHappyHour(restaurant);

        printSection("WAITING QUEUE");

        List<Customer> waiting = restaurant.getWaitingCustomers();

        if (waiting.isEmpty()) {

            output.println("  No customers waiting.");
        } else {

            for (Customer customer : waiting) {
                displayCustomer(customer);
            }
        }

        printSection("TABLES");

        for (Table table : restaurant.getTables()) {
            Customer occupant = table.getCustomer();

            output.printf("  Table %d - %s%n",
                    table.getTableId(),
                    occupant == null ? "Available" : "Occupied");

            if (occupant != null) {
                displayCustomer(occupant);
            } else {
                output.println();
            }
        }
    }

    private void displayMetrics(Restaurant restaurant) {
        String average = restaurant.getPaidCustomerCount() == 0
                ? "N/A"
                : String.format(Locale.US, "%.1f", restaurant.getAvgSatisfaction());

        printSection("GAME PROGRESS");

        output.printf(Locale.US,
                "  Revenue: $%.2f     (Goal: $45.00)%n",
                restaurant.getRevenue());

        output.printf(
                "  Paid customers: %d     (Goal: 4)%n",
                restaurant.getPaidCustomerCount());

        output.printf(
                "  Average satisfaction: %s     (Goal: 60)%n",
                average);

        output.println();

        output.printf(
                "  Served: %d     Unhappy departures: %d%n",
                restaurant.getServedCustomers(),
                restaurant.getUnhappyCustomers());
    }

    private void displayHappyHour(Restaurant restaurant) {
        printSection("HAPPY HOUR");

        if (restaurant.isHappyHourActive()) {
            output.printf(
                    "  ACTIVE | %d turn(s) remaining%n",
                    restaurant.getHappyHourRemainingTurns());

        } else if (restaurant.canActivateHappyHour()) {
            output.println("  Available - Can be activated once for 2 turns");

        } else {
            output.println("  Already used");
        }
    }

    private void displayCustomer(Customer customer) {
        output.printf(
                "    #%d %-7s  Satisfaction: %3d  Status: %s%n",
                customer.getCustomerId(),
                customer.getCustomerType(),
                customer.getSatisfaction(),
                customer.getStatus());

        Order order = customer.getOrder();

        if (order != null) {
            int totalUnits = 0;

            for (MenuItem item : order.getMenuItems()) {
                totalUnits += item.getPreparationUnits();
            }

            output.printf(Locale.US,
                    "       Food: %s  |  Bill: $%.2f%n",
                    order.getMenuItems().get(0).getName(),
                    order.getPrice());

            output.printf(
                    "       Kitchen: %-10s  |  Prepared: %d/%d units%n",
                    order.getStatus(),
                    order.getCookedUnit(),
                    totalUnits);
        }

        output.println();
    }

    public Optional<Action> readAction(String staff, List<Action> actions) {
        Objects.requireNonNull(staff);
        Objects.requireNonNull(actions);

        if (actions.isEmpty()) {
            throw new IllegalArgumentException(
                    "An action list must include Wait");
        }

        printSection(staff.toUpperCase(Locale.ROOT) + " - CHOOSE ONE TASK");

        for (int index = 0; index < actions.size(); index++) {
            output.printf(
                    "    [%d] %s%n",
                    index,
                    actionLabel(actions.get(index)));
        }

        output.println();

        while (true) {
            output.printf(
                    "  %s choice (0-%d): ",
                    staff,
                    actions.size() - 1);

            if (!scanner.hasNextLine()) {
                return Optional.empty();
            }

            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());

                if (choice >= 0 && choice < actions.size()) {
                    output.printf(
                            "  Selected: %s%n%n",
                            actionLabel(actions.get(choice)));

                    return Optional.of(actions.get(choice));
                }

                output.println(
                        "  Invalid option. Choose one of the displayed numbers.");

            } catch (NumberFormatException exception) {
                output.println("  Please enter a whole number.");
            }
        }
    }

    private String actionLabel(Action action) {
        return switch (action.task()) {
            case WAIT -> "Wait (no task this turn)";
            case SEAT -> "Seat customer #" + action.customerId();
            case TAKE_ORDER -> "Take customer #" + action.customerId() + "'s order";
            case SERVE -> "Serve customer #" + action.customerId();
            case PREPARE -> "Prepare one unit for customer #" + action.customerId();
        };
    }

    public Optional<Boolean> readHappyHourChoice(int turn) {
        printBanner(String.format("TURN %02d  |  HAPPY HOUR DECISION", turn));

        output.println("  Once per game: 20% off new orders for 2 turns.");
        output.println("  Waiting customers recover 15 satisfaction at turn start.");
        output.println();

        while (true) {
            output.print("  Activate now? [Y/N]: ");

            if (!scanner.hasNextLine()) {
                return Optional.empty();
            }

            String choice = scanner.nextLine().trim();

            if (choice.equalsIgnoreCase("Y")) {
                output.println("  Happy Hour activated!\n");
                return Optional.of(true);
            }

            if (choice.equalsIgnoreCase("N")) {
                output.println();
                return Optional.of(false);
            }

            output.println("  Please enter Y or N.");
        }
    }

    public void displayFoodSelection(Customer customer) {
        Order order = Objects.requireNonNull(customer.getOrder());

        printSection("NEW ORDER");

        output.printf(Locale.US,
                "  Customer #%d chose %s | Price locked at $%.2f%n%n",
                customer.getCustomerId(),
                order.getMenuItems().get(0).getName(),
                order.getPrice());
    }

    public void displayTurnSummary(Restaurant restaurant, int turn, int maxTurns) {
        printBanner(String.format("END OF TURN %02d / %02d", turn, maxTurns));

        displayMetrics(restaurant);
        output.println();
    }

    public void displayResult(boolean victory, Restaurant restaurant) {
        printBanner(victory ? "GAME OVER  |  VICTORY!" : "GAME OVER  |  DEFEAT");

        displayMetrics(restaurant);

        printSection("VICTORY REQUIREMENTS");

        output.println("  At least 4 paid customers");
        output.println("  At least $45.00 in revenue");
        output.println("  Average satisfaction of paid customers >= 60");
        output.println();
    }

    public void displayInputEnded() {
        output.println();

        printBanner("INPUT ENDED  |  GAME STOPPED");

        output.println("  Game stopped before all 18 turns; no final result.\n");
    }

    private void printBanner(String heading) {
        output.println();
        output.println("=".repeat(LINE_WIDTH));
        output.println("  " + heading);
        output.println("=".repeat(LINE_WIDTH));
    }

    private void printSection(String heading) {
        output.println();
        output.println("  " + heading);
        output.println("  " + "-".repeat(LINE_WIDTH - 4));
        output.println();
    }
}