# SC2002_Group-4

SC2002 Restaurant Rush Group Assignment — a Java command-line, turn-based
restaurant management game (see the assignment brief for full rules).

## Status

Stage 1 (Basic Operation) is **in progress**. The turn-sequence core is
implemented; the customer, staff, menu and CLI work packages are still stubs
(see `src/` — every stub is marked with `STUB` / `GAP` / `TODO` comments).
Scope decisions and rules references are tracked in `.context-files/`.

## Game overview (base setting)

- 18 turns, 2 tables, 1 Waiter / 1 Chef / 1 Cashier (one task each per turn)
- Customer arrivals on turns 1, 4, 7, 10, 13, 16 (six in total)
- Menu: Salad $9.00 (1 unit), Burger $15.00 (1 unit), Pasta $18.00 (2 units)
- Win after turn 18: >= 4 paid customers AND revenue >= $45 AND average paid
  satisfaction >= 60; otherwise defeat
- The manager's decisions are sequential: each staff choice is validated and
  executed immediately (or explained and rejected without using up the task),
  then the manager redecides or moves on

## Project structure

Packages mirror the UML packages / Person 1–5 split:

| Package    | Contents | Status |
|------------|----------|--------|
| `game`     | `GameController` (turn sequencing), `TurnProcessor` (automatic end-of-turn steps), `GameState` (state + victory/defeat check), `WaiterChoice` hierarchy | **implemented** (TurnProcessor step bodies are TODOs) |
| `customer` | `Customer`, `Order`, statuses, `CustomerChoiceGenerator` | stub |
| `staff`    | `Waiter`, `Chef`, `Cashier`, `Table` | stub (`Table` is functional) |
| `menu`     | `Menu`, `MenuItem` | stub (seeded with the three base items) |
| `cli`      | `RestaurantCLI`, `Main` | stub |
| `harness`  | `Stage1TurnSequenceHarness` (temporary, see below) | test only |

Any mismatch between the code and the v1 Stage 1 UML class diagram
(`.context-files/v1 stage1 uml class diagram.txt`) is documented with
rationale in `deviation_from_v1_s1_uml.txt` — intentional design deviations in
section A, artefacts of the stub pass in section B.

## Build and run

Requires a JDK (plain `javac`/`java`, no build tool).

```bash
# compile into out/ (gitignored)
javac -d out $(find src -name '*.java')

# run the Stage 1 turn-sequence harness
java -cp out harness.Stage1TurnSequenceHarness
```

`cli.Main` exists but only prints a placeholder until the CLI is implemented.

## Test harness

`src/harness/Stage1TurnSequenceHarness.java` is a temporary testing feature,
**not part of the game** and not the CLI. It verifies the Stage 1 core as it
stands:

- the 18-turn loop runs end to end;
- a customer arrives exactly on turns 1, 4, 7, 10, 13, 16 (waiting count
  increments on those turns only);
- each turn runs the sequential decision point (one validated, immediately
  executed choice at a time — the harness issues a `WaitChoice` since
  seat/order/serve execution is still stubbed), then the automatic
  end-of-turn steps via `TurnProcessor`;
- after turn 18 the result is evaluated; the simple
  `if (result == GameResult.DEFEAT)` check then prints the missed victory
  targets via `GameState.getDefeatCauses()`.

Expected output today: 18 turn lines, ending in `DEFEAT` with all three
targets missed (0 paid, revenue 0) — correct while the stubs are empty. Once
the Waiter/Chef/kitchen and payment logic land, the harness should be updated
to script a reproducible victory run and a reproducible defeat run (both are
required by the assignment for Stage 1 test notes). The harness can be deleted
or replaced once the real CLI exists.
