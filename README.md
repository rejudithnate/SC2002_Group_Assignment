
# Restaurant Rush — SC2002 Group Assignment

**Repository:** https://github.com/rejudithnate/SC2002_Group_Assignment

Restaurant Rush is a turn-based restaurant management game written in Java. The manager chooses which customers the staff should prioritise. The game was developed in three runnable stages to demonstrate object-oriented design and extension.

## 1. Requirements and game rules

- 18 turns, 2 tables and 6 scheduled arrivals (turns 1, 4, 7, 10, 13 and 16).
- Each turn: Host (Stage 3) can seat one customer; Waiter can perform one task (seat, take order, serve or wait); Chef can prepare one unit of ordered food; Cashier automatically processes one eligible payment.
- A prepared order becomes **READY**, then the Waiter can **SERVE** it in a later turn. The customer spends a turn eating, then becomes **READY_TO_PAY**. Payment then happens in a subsequent turn.
- Customers lose satisfaction while waiting for a table or food. At 0 satisfaction, they leave without paying.
- **Victory:** at least 4 paid customers, at least $45 revenue, and average satisfaction of paid customers at least 60 after turn 18. Otherwise the result is **Defeat**.

## 2. Running the game

Requires **JDK 17+**. No third-party libraries or network access are needed.

From the repository root, in **VS Code PowerShell**:

```powershell
New-Item -ItemType Directory -Force out | Out-Null
javac -d out (Get-ChildItem src/main/java -Recurse -Filter *.java).FullName
java -cp out restaurantrush.Main
```

At each prompt, choose a numbered action for the Host, Waiter and Chef. Enter `Y` or `N` when asked whether to activate Happy Hour. The Cashier acts automatically.

## 3. Features added at each stage

| Stage | Main features | Git tag |
|-------|---------------|---------|
| 1 | Regular customers, menu, Waiter, Chef, Cashier, tables, full turn/payment sequence, win/lose outcomes | `stage-1` |
| 2 | VIP and Critic customers, different waiting penalties and pricing/preferences | `stage-2` |
| 3 | Host, ComboMeal and Happy Hour | `stage-3` |

**Stage 2 behaviour:** Regular customers lose 8 satisfaction per waiting turn; VIPs lose 5 and receive a 10% discount; Critics lose 12 and prefer Pasta.

**Stage 3 behaviour:** The Host can seat one customer independently before Waiter. Salad + Pasta Combo costs $24.30 and takes 3 preparation units. Every fourth customer chooses a combo, except Critics who prefer Pasta. Happy Hour can be activated once, lasts two turns, applies 20% off new orders, and restores 15 satisfaction at the start of each active turn to eligible waiting customers (maximum 100). VIP discount is applied before Happy Hour. Order prices are fixed when placed.

## 4. Code structure and design

```text
src/main/java/restaurantrush/
  Main.java
  boundary/GameUI.java
  control/GameController.java, TurnController.java
  entity/Restaurant.java, Customer.java, RegularCustomer.java,
         VIPCustomer.java, CriticCustomer.java, Staff.java,
         Host.java, Waiter.java, Chef.java, Cashier.java,
         Table.java, Order.java, Menu.java, MenuItem.java,
         SingleDish.java, ComboMeal.java, HappyHourPolicy.java,
         Money.java, CustomerStatus.java, OrderStatus.java

tests/
  RestaurantRushTests.java
  inputs/ (fixed-input gameplay runs)
```

The design follows a **Boundary–Control–Entity** separation. `GameUI` handles display and input; `GameController` checks the overall game result; `TurnController` coordinates each turn; and entity classes implement customer, order, staff and pricing rules.

Key OOP choices include:
- **Encapsulation:** state changes are controlled by domain methods rather than by the UI.
- **Inheritance and polymorphism:** customer subclasses override waiting penalties, pricing or menu preferences; staff share action tracking through `Staff`.
- **Composition:** `ComboMeal` is built from two `SingleDish` objects; `Restaurant` manages its tables, menu, staff and queues.

The final class diagram, sequence diagram, design trade-offs and relevant SOLID discussion are in the PDF report.

## 5. Development history and GitHub comparisons

### Stage 1 → Stage 2

[Compare `stage-1` and `stage-2`](https://github.com/rejudithnate/SC2002_Group_Assignment/compare/stage-1...stage-2)

- **Added:** `RegularCustomer`, `VIPCustomer`, `CriticCustomer` to provide separate customer behaviours.
- **Changed:** `Customer` for shared and overridable behaviour; `Restaurant` and `TurnController` for varied arrivals and gameplay; `Order` for customer billing information; `GameUI` for displaying customer information; `README.md` for documentation.

### Stage 2 → Stage 3

[Compare `stage-2` and `stage-3`](https://github.com/rejudithnate/SC2002_Group_Assignment/compare/stage-2...stage-3) *(link works after the final `stage-3` tag is pushed)*

- **Added:** `Host` (independent seating), `Staff` (common task tracking), `SingleDish` and `ComboMeal` (menu extension), `HappyHourPolicy` (two-turn policy), `Money` (cent-based calculations).
- **Changed:** `Restaurant` and `TurnController` to coordinate the Host, pricing and turn-start recovery; `Menu`, `MenuItem`, `Order` and customer subclasses for dish selection and locked prices; `Waiter`, `Chef`, `Cashier` and `Table` to integrate and enforce staff/order rules; `GameController`, `Main` and `GameUI` for game setup, result checks and clearer console output; `Customer` for service and recovery behaviour.
- **Deleted source files:** none.

These summaries describe the significant Java changes; see the GitHub compare links for the complete file-by-file history.

## 6. Testing and reproducible runs

Automated tests verify both complete 18-turn outcomes and important Stage 2/3 rules. The tests use only Java's standard library.

```powershell
# Run from the repository root after compiling the main game:
javac -cp out -d out tests/RestaurantRushTests.java
java -cp out RestaurantRushTests
```

Expected output for this reviewed version: **`RESULT: 8 passed, 0 failed`**.

The eight tests cover: Stage 3 victory, Stage 3 defeat, VIP/Critic behaviour, combo price/preparation, Happy Hour pricing/duration, Happy Hour satisfaction recovery, Host/Waiter independent seating and payment timing.

The saved input files contain the exact keystrokes entered at the game's prompts. Reproduce the Stage 3 results with:

```powershell
Get-Content tests/inputs/victory.txt | java -cp out restaurantrush.Main
Get-Content tests/inputs/defeat.txt | java -cp out restaurantrush.Main
```

| Version | Fixed input | Expected result | Paid | Revenue | Average paid satisfaction |
|---------|-------------|-----------------|-----:|--------:|--------------------------:|
| Stage 1 | `stage1-victory.txt` | Victory | 5 | $69.00 | 74.4 |
| Stage 1 | `stage1-defeat.txt` | Defeat | 0 | $0.00 | 0.0 |
| Stage 2 | `stage2-victory.txt` | Victory | 4 | $62.10 | 70.25 |
| Stage 2 | `stage2-defeat.txt` | Defeat | 0 | $0.00 | N/A |
| Stage 3 | `victory.txt` | Victory | 5 | $77.40 | 83.0 |
| Stage 3 | `defeat.txt` | Defeat | 0 | $0.00 | N/A |

To replay a historical stage, **first copy its inputs outside the repository** and switch to the corresponding tag:

```powershell
Copy-Item tests/inputs/stage1-victory.txt ../stage1-victory.txt
git switch --detach stage-1
javac -d out (Get-ChildItem src/main/java -Recurse -Filter *.java).FullName
Get-Content ../stage1-victory.txt | java -cp out restaurantrush.Main
# Return to your working branch afterwards
```

Repeat with `stage1-defeat.txt`, or use `stage-2` with its matching `stage2-*` inputs.  In addition to these automated checks, manually test invalid text and out-of-range menu choices to show that the user can retry.

## 7. Video evidence

Fill in the actual timestamps after recording:

| Required segment | Timestamp |
|------------------|-----------|
| Launching the submitted project |  |
| Manager choices and staff constraints |  |
| READY → SERVED → eating → payment |  |
| VIP or Critic behaviour |  |
| Stage 3 Host, ComboMeal or Happy Hour |  |
| Victory and defeat demonstrations |  |
| Stage comparisons, tags and UML diagrams |  |

## 8. Before submission

- [ ] Main game compiles and runs offline, all 18 turns.
- [ ] Fixed-input victory/defeat runs and automated tests pass.
- [ ] Git tags `stage-1`, `stage-2`, `stage-3` identify runnable commits.
- [ ] Class diagram matches final code; sequence diagram and design discussion are in the PDF report.
- [ ] Video timestamps and stage comparison links are complete.
- [ ] Include source code, README, PDF report, video and required original-work / GAI declarations in the submission archive.
