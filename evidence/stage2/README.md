# Stage 2 gameplay evidence

## Integrated rules

- Scheduled arrivals remain turns 1, 4, 7, 10, 13 and 16.
- Customer 2 is a VIP: loses 5 satisfaction per waiting or seated turn and pays 90% of menu price.
- Customer 5 is a Critic: loses 12 satisfaction per waiting or seated turn and prefers Pasta when available.
- All other customers are Regular: lose 8 satisfaction per waiting or seated turn and pay menu price.
- Menu choices, satisfaction loss and bill calculations use customer polymorphism.
- The Waiter locks the customer's discounted price into the order when the order is taken. The Cashier charges that stored amount after the customer becomes eligible to pay.
- A customer cannot pay on the same turn they finish eating.
- The game still uses 18 turns, two tables and the same three victory thresholds.

## Build and automated checks

From the repository root on macOS or Linux:

```bash
javac -d out $(find src test -name '*.java')
java -ea -cp out cli.Stage2IntegrationGateTest --strict
java -ea -cp out cli.Stage2CLIPresentationTest
java -ea -cp out cli.CLISmokeTest
java -ea -cp out game.Stage1RegressionTest
java -ea -cp out game.Stage1EdgeCaseTest
```

All five checks passed on the integrated Stage 2 snapshot. The original Stage 1 regression test names are retained, with expected results adjusted for the VIP discount and patience difference.

## Reproducible Stage 2 terminal scenarios

```bash
NO_COLOR=1 java -cp out cli.Main < evidence/stage2/victory_input.txt
NO_COLOR=1 java -cp out cli.Main < evidence/stage2/defeat_input.txt
NO_COLOR=1 java -cp out cli.Main < evidence/stage2/invalid_input.txt
NO_COLOR=1 java -cp out cli.Main < evidence/stage2/critic_input.txt
```

| Input file | Expected evidence |
| --- | --- |
| `victory_input.txt` | VICTORY after turn 18, 4 paid, $49.50 revenue, 71.0 average paid satisfaction, 0 walkouts |
| `defeat_input.txt` | DEFEAT after turn 18, 0 paid, $0.00 revenue, 1 walkout; VIP remains waiting |
| `invalid_input.txt` | Invalid Waiter/Chef entries rejected, retries accepted, Q exits cleanly |
| `critic_input.txt` | Critic C5 arrives at turn 13, is seated, and chooses Pasta on turn 14 |

For visual evidence, run the scripts in a terminal without `NO_COLOR=1` to show the coloured dashboard. Capture screenshots of VIP arrival, the $13.50 (menu $15.00) Burger price, the Critic's Pasta order, the final victory report and an invalid-input retry.

This integration is Stage 2 only. Host, ComboMeal and Happy Hour belong to Stage 3 and must not be merged into the Stage 2 milestone.
