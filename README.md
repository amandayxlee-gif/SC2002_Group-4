# SC2002 Group 4 - Restaurant Rush

Restaurant Rush is a Java command-line, turn-based restaurant management game developed for the NTU SC2002 Object-Oriented Design and Programming group assignment.

## Project Status

Stage 1 - Basic Operation: Implemented and tested, including the single Restaurant Rush CLI.

The Stage 1 implementation includes customer arrivals, seating, food ordering, preparation, serving, eating, payment, customer satisfaction, abandonment, and victory/defeat evaluation, matching the assignment requirements

Stage 2 and Stage 3 are not included in this implementation.

## Stage 1 Game Rules
Following the assignment requirements:
- The game lasts exactly 18 turns.
- There are 2 restaurant tables.
- The restaurant has 1 Waiter, 1 Chef and 1 Cashier.
- Customers arrive at the start of turns 1, 4, 7, 10, 13 and 16.
- Every customer starts with 100 satisfaction.
- Waiting or seated customers who have not been served lose 8 satisfaction per turn.
- Customers leave when their satisfaction reaches 0.
- The Waiter can perform one valid action per turn:
  - Seat a customer.
  - Take an order.
  - Serve a prepared order.
  - Wait.
- The Chef can perform one preparation unit per turn or wait.
- The Cashier automatically processes at most one eligible payment per turn.
- Orders cannot be served on the same turn they become ready.
- Customers must finish eating before becoming ready to pay.
- Payment cannot occur on the same turn eating finishes.
- Payment frees the customer's table.

## Menu

| Item | Price | Preparation Units |
|------|-------|-------------------|
| Salad | $9.00 | 1 |
| Burger | $15.00 | 1 |
| Pasta | $18.00 | 2 |

Customers select their own food using a deterministic rule based on their customer ID.

For the Stage 1 menu, the selection sequence is Salad, Burger, Pasta, then repeats.

## Victory Conditions

Victory is evaluated after turn 18.

All three requirements must be met:

1. At least 4 paid customers.
2. Total revenue of at least $45.00.
3. Average satisfaction of paid customers of at least 60.

If any requirement is not met, the result is DEFEAT.

## Requirements

- Java Development Kit (JDK 17 or later)
- macOS/Linux terminal or Windows PowerShell
- No external libraries or build tools required

This setup runs entirely offline.

## Compile and Run

The supported entry point is **`cli.Main`**, which launches **`cli.RestaurantCLI`**.

### macOS / Linux / VS Code terminal

Open a terminal at the repository root (the folder containing `src/`).

```bash
javac -d out $(find src test -name '*.java')
java -cp out cli.Main
```

### Windows PowerShell

Open PowerShell in the repository root directory.

Compile the Java source files and tests:

```powershell
javac -d out (Get-ChildItem -Path src,test -Filter *.java -Recurse).FullName
```

Launch the interactive game:

```powershell
java -cp out cli.Main
```

The CLI displays the current game state and prompts the manager for Waiter and Chef decisions.

At the Waiter menu, use `1` to seat, `2` to take an order, `3` to serve food, or **`0` to wait**.
At the Chef station, enter an existing order ID to prepare one unit, or **`0` to wait**.
The Waiter gets one valid action per turn, followed by the Chef's one preparation unit.
Rejected choices do not consume an action. After a successful Waiter action, the current
customer queue, tables and kitchen orders are displayed again.

The dashboard shows order prices, human-readable statuses, satisfaction goals and walkout
counts. End-of-turn summaries announce payments and newly departed customers. A meal
that has just finished cooking cannot be served until the next turn.

For a plain-text transcript without colour escape sequences on macOS/Linux:

```bash
NO_COLOR=1 java -cp out cli.Main
```

Enter `Q` to exit the game.

## Run Automated Tests

If you have not already compiled both source and test files, use the command above.

```powershell
javac -d out (Get-ChildItem -Path src,test -Filter *.java -Recurse).FullName
```

Run the regression test suite:

```powershell
java -ea -cp out game.Stage1RegressionTest
```

Run the additional edge-case tests:

```powershell
java -ea -cp out game.Stage1EdgeCaseTest
```

Run the interactive CLI smoke tests:

```powershell
java -ea -cp out cli.CLISmokeTest
```

The same three `java -ea -cp out ...` commands work on macOS/Linux.

### Reproducible CLI evidence

The `evidence/stage1/` folder includes scripted input for a victory, defeat,
and invalid-input recovery scenario. From the repository root on macOS/Linux:

```bash
NO_COLOR=1 java -cp out cli.Main < evidence/stage1/victory_input.txt
NO_COLOR=1 java -cp out cli.Main < evidence/stage1/defeat_input.txt
NO_COLOR=1 java -cp out cli.Main < evidence/stage1/invalid_input.txt
```

See [evidence/stage1/README.md](evidence/stage1/README.md) for expected outcomes
and instructions for capturing screenshots or transcripts.

### Test Coverage

The regression suite runs 10 test groups covering:

- Controller seating
- Taking orders
- Complete order flow
- Action limits
- Turn sequencing
- End-of-turn processing
- Full 18-turn victory and defeat simulations
- Waiter behaviour
- Chef behaviour
- Cashier behaviour

The edge-case suite contains 11 additional tests covering:

- Initial game state
- Scheduled customer arrivals
- Invalid actions
- Waiter and Chef action limits
- Order preparation and serving timing
- Payment and table reuse
- Customer abandonment
- Collection protection
- Exact game duration
- Victory and defeat requirements

At the time of the Stage 1 review, all 21 tests passed.

The CLI smoke test additionally exercises the revised Waiter/Chef inputs, display
refresh, order pricing, player-friendly labels, walkout events, invalid inputs,
and full 18-turn victory and defeat scenarios.

## Project Structure

| Package | Responsibilities |
|---------|------------------|
| `game` | Game controller, game state, turn processing and player choices |
| `cli` | Player-facing Restaurant Rush CLI and entry point |
| `customer` | Customers, orders, statuses and deterministic food selection |
| `staff` | Waiter, Chef, Cashier and restaurant tables |
| `menu` | Menu items, prices and preparation requirements |

The automated tests are stored in the `test/` directory.

## Implementation Notes

- Manager decisions are processed sequentially by the CLI and validated by the controller.
- Invalid actions do not consume the relevant staff member's available task.
- Both the Waiter and Chef use `0` to wait in the CLI.
- The Waiter acts before the Chef.
- The Chef can prepare an order placed during the current turn.
- The Cashier processes eligible payments automatically during end-of-turn processing.
- The game uses deterministic customer food selection to support reproducible testing.

## Documentation

The original assignment brief provides the authoritative project requirements.

`cli.RestaurantCLI` is the sole playable interface for Stage 1, launched through `cli.Main`.

The source code and automated tests reflect the current Stage 1 implementation.
