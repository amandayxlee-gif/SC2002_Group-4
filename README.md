# SC2002 Group 4 - Restaurant Rush

Restaurant Rush is a Java command-line, turn-based restaurant management game developed for the NTU SC2002 Object-Oriented Design and Programming group assignment.

## Project Status

Stage 1 - Basic Operation: Implemented and tested.

Stage 2 - Customer Variety: Implemented and tested, including VIP and Critic customers, customer-specific satisfaction and pricing rules, and an updated CLI.

Stage 3 - Advanced Management: Implemented and tested in the Stage 3 snapshot (Host, ComboMeal and Happy Hour).

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

## Stage 2 Customer Variety

All six customer arrivals still occur at turns 1, 4, 7, 10, 13 and 16.

| Customer | Arrival | Satisfaction loss per waiting/seated turn | Bill | Food preference |
| --- | --- | --- | --- | --- |
| Regular | C1, C3, C4, C6 | 8 | Full menu price | Deterministic menu rotation |
| VIP | C2, turn 4 | 5 | 10% off | Deterministic menu rotation |
| Critic | C5, turn 13 | 12 | Full menu price | Pasta when available |

The customer determines its own patience, menu preference and bill using overridden methods. At order placement the final price is locked into the `Order`, so the automatic Cashier collects the discounted amount without checking customer types. Customer labels and the discounted menu price appear in the CLI.

## Menu

| Item | Price | Preparation Units |
|------|-------|-------------------|
| Salad | $9.00 | 1 |
| Burger | $15.00 | 1 |
| Pasta | $18.00 | 2 |

Regular and VIP customers select food using a deterministic rule based on customer ID.

The selection sequence is Salad, Burger, Pasta, then repeats. Critic customers instead prefer Pasta.

## Stage 1 → Stage 2 Transition

Compare the two stages:
`https://github.com/amandayxlee-gif/SC2002_Group-4/compare/stage-1...stage-2`

### New files

| File | Responsibility |
| --- | --- |
| `src/customer/VIPCustomer.java` | VIP customer: 5-point patience loss per waiting turn, 10% off the bill |
| `src/customer/CriticCustomer.java` | Critic customer: 12-point patience loss, always orders Pasta |
| `test/cli/Stage2IntegrationGateTest.java` | Automated gate over Stage 2 rules, arrivals, timing and billing |
| `test/cli/Stage2CLIPresentationTest.java` | Verifies the CLI shows customer types and discounted prices |
| `evidence/stage2/` | Reproducible victory, defeat, invalid-input and Critic scenarios |

### Existing files changed, and why

| File | Change | Reason |
| --- | --- | --- |
| `src/customer/Customer.java` | `satisfaction` changed from `private` to `protected`; `chooseMenuItem`, `calculateBillCents` and `getCustomerTypeLabel` introduced as overridable hooks; the discounted price is locked into the order in `assignOrder` | Stage 2 behaviour must be dispatched through customer objects (polymorphism), not through type checks in the turn controller (docx §4.2). The bill differs per customer type, so the price is finalised by the customer when the order is placed. `protected` `satisfaction` lets subclasses decrement it in their overrides; the alternative of keeping it private behind a guarded mutator was considered and traded off for override simplicity |
| `src/customer/CustomerChoiceGenerator.java` | Now a thin delegation to `customer.chooseMenuItem(...)` | Menu choice becomes polymorphic; the empty-menu guard moved into `Customer`. The generator remains the single swap point if the deterministic rule is ever replaced by seeded randomness (docx §2.2), keeping the controller unchanged |
| `src/customer/Order.java` | `setFinalPriceCents` added, writeable only while the order is `PLACED` | Locks the customer-specific price at placement, so the Cashier charges the stored amount without knowing customer types, and Happy-Hour-style later policies have a single price-freeze point |
| `src/game/GameController.java` | Scheduled arrivals built from a factory list: VIP at turn 4, Critic at turn 13 | The reproducible base-setting run must include at least one VIP and one Critic while keeping all six arrivals (docx §4.2) |
| `src/cli/RestaurantCLI.java` | Customer type labels in queue, tables and orders; orders display the charged price against the menu price; updated welcome text | The manager must see which customer type they are serving and what the dish will actually cost |
| `test/game/ControllerSimulationTest.java` | Simulation expectations updated | VIP patience and pricing change the numbers a full 18-turn run produces |

### What still works

All 10 Stage 1 regression test groups and 11 edge-case tests pass unchanged on the Stage 2 tree, and the `evidence/stage1/` scripts remain replayable (their expected numbers shift because VIP pricing and patience change the outcomes). The timing rules are re-verified by these suites — during the customer rewrite, the guard that forbids paying on the same turn a customer finished eating was briefly lost, and the restored regression coverage is what surfaced it before integration. The victory and defeat criteria, turn structure and staff limits are untouched.

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

Run the Stage 2-specific integration and CLI presentation checks:

```bash
java -ea -cp out cli.Stage2IntegrationGateTest --strict
java -ea -cp out cli.Stage2CLIPresentationTest
```

The same three `java -ea -cp out ...` commands work on macOS/Linux.

### Reproducible CLI evidence

The `evidence/stage1/` folder archives the original Stage 1 scripted victory, defeat,
and invalid-input recovery scenarios. To reproduce their original numerical outcomes, check out the preserved Stage 1 Git tag. The files can also be replayed on the current branch, but Stage 2 VIP pricing and patience will change the results. On macOS/Linux:

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

The Stage 2 victory script ends with 4 paid customers, $49.50 revenue and 71.0 average paid satisfaction. Its no-action defeat script ends with 1 walkout, since VIP customers lose patience more slowly. The original Stage 1 regression test entry points are retained, with simulation expectations updated to reflect Stage 2 behavior.

### Stage 2 functional evidence

```bash
NO_COLOR=1 java -cp out cli.Main < evidence/stage2/victory_input.txt
NO_COLOR=1 java -cp out cli.Main < evidence/stage2/defeat_input.txt
NO_COLOR=1 java -cp out cli.Main < evidence/stage2/invalid_input.txt
NO_COLOR=1 java -cp out cli.Main < evidence/stage2/critic_input.txt
```

See [evidence/stage2/README.md](evidence/stage2/README.md) for verified expected results and screenshot guidance.

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

`cli.RestaurantCLI` is the playable interface for all three stages, launched through `cli.Main`.

The current source code and automated tests reflect Stage 3. The earlier stages should remain available through the runnable `stage-1` and `stage-2` Git tags.

## Stage 3 — Host, ComboMeal and Happy Hour

Stage 3 adds the Host, ComboMeal and Happy Hour features to the Stage 2 game. The Host can seat one customer per turn without using the Waiter's action. Happy Hour can be activated once, before staff actions, and lasts for the current turn and the next. During active turns, eligible unserved customers recover up to 15 satisfaction points (maximum 100), and new orders receive 20% off. The CLI shows the remaining Happy Hour duration.

`ComboMeal` uses the existing Salad and Pasta menu items. Together they cost $27.00, so the combo costs $24.30 after the 10% discount and takes three Chef preparation units. C4 selects the combo in the fixed Stage 3 scenario; other customers keep their Stage 2 food choices. `Menu.getItems()` still returns the three original dishes, while `Menu.getAllItems()` also includes the combo.

Order prices are fixed when the order is placed. A combo ordered during Happy Hour costs $19.44. A VIP Burger costs $10.80 during Happy Hour after both discounts. The Cashier collects the saved order price. The CLI displays the Host and Happy Hour controls, combo items, prices and preparation progress.

### Stage 3 turn controls

1. On turns before Happy Hour has been activated: enter `1` to activate it immediately or `0` to skip.
2. At the Host station: enter `1` and supply a queued customer ID and free table ID, or `0` to wait.
3. At the Waiter station: `1` seats, `2` takes an order, `3` serves a ready order, `0` waits.
4. At the Chef station: enter an active order ID to prepare one unit or `0` to wait.

The Host can seat a customer and the Waiter can take that customer's order in the same turn. The game remains 18 turns and retains the original victory requirements.

### Stage 3 tests and evidence

From the repository root:

```bash
javac -d out $(find src test -name '*.java')
java -ea -cp out game.Stage3IntegrationTest
java -ea -cp out cli.Stage3CLITest
java -ea -cp out game.HostStage3Test
java -ea -cp out cli.CLISmokeTest
java -ea -cp out cli.Stage2IntegrationGateTest --strict
java -ea -cp out game.Stage1RegressionTest
java -ea -cp out game.Stage1EdgeCaseTest
```

Reproducible runs are documented in `evidence/stage3/README.md`.

### Stage 2 → Stage 3 change summary

| File | Change | Reason |
| --- | --- | --- |
| `src/menu/ComboMeal.java` | New `MenuItem` subclass containing Salad and Pasta | Calculates the combo price and preparation units from the two items |
| `src/menu/Menu.java` | Registers a ComboMeal alongside the three base items | Exposes the combo without changing the Stage 2 base-menu rotation |
| `src/customer/CustomerChoiceGenerator.java` | C4 chooses the ComboMeal; others preserve original choice rule | Reproducible new-menu customer selection |
| `src/cli/RestaurantCLI.java` | Adds Host and Happy Hour choices, status displays and combo details | Lets the player use Stage 3 features through the CLI |
| `test/game/ControllerSimulationTest.java` | Updates the full victory path to include the Host and a three-unit combo | Earlier turn/payment rules remain tested with Stage 3 actions |
| `test/cli/CLISmokeTest.java` | Updates tests for the new Host and Happy Hour prompts | Keeps CLI input behaviour covered by tests |
| `test/game/Stage3IntegrationTest.java` | New domain/controller tests | Checks composition, combo pricing/units, discounts, timing, Host independence and Happy Hour |
| `test/cli/Stage3CLITest.java` | New end-to-end CLI tests | Replays winning, losing, VIP discount and invalid-input scenarios |
| `evidence/stage3/` | Reproducible scripted cases and instructions | Supports the Stage 3 report/video functional evidence |

The Host and Happy Hour backend code was already present when this work began. This update adds ComboMeal and connects the existing Stage 3 features to the CLI. Keep the `stage-1` and `stage-2` Git tags pointing to their runnable earlier versions.
