# Stage 1 CLI Functional Evidence

These scripts exercise the playable `cli.Main` entry point against the real
`GameController`. They are regular keyboard inputs, not mock backend responses.

## Prepare

From the repository root in a macOS/Linux/VS Code terminal:

```bash
javac -d out $(find src test -name '*.java')
java -ea -cp out cli.CLISmokeTest
java -ea -cp out game.Stage1RegressionTest
java -ea -cp out game.Stage1EdgeCaseTest
```

## Reproduce the three CLI scenarios

```bash
NO_COLOR=1 java -cp out cli.Main < evidence/stage1/victory_input.txt
NO_COLOR=1 java -cp out cli.Main < evidence/stage1/defeat_input.txt
NO_COLOR=1 java -cp out cli.Main < evidence/stage1/invalid_input.txt
```

| Script | What it demonstrates | Expected result |
| --- | --- | --- |
| `victory_input.txt` | Seating, order taking, Chef preparation, serving, payment, HUD refresh and dish pricing | `VICTORY`; 4 paid; `$51.00` revenue; `68.0` average paid satisfaction |
| `defeat_input.txt` | Both staff wait (`0`) every turn; satisfaction decreases and customers abandon | `DEFEAT`; 0 paid; `$0.00` revenue; `N/A` average; 2 walkouts with event notifications |
| `invalid_input.txt` | Reject text input, invalid menu selection, invalid customer ID and invalid Chef order; recover without consuming a staff action | An accepted seating action after retries; then player quits intentionally |

To capture plain-text logs for manual review without committing generated terminal
output, append `> victory_output.txt`, etc., to the commands above. Add those
generated logs to your report only if useful; the input scripts are sufficient
to reproduce the results.

Suggested report screenshots:

1. The initial Restaurant Rush dashboard, with satisfaction meters and queue.
2. A successful `TAKE ORDER` **action in the menu** followed by the refreshed
   kitchen listing with its dish name and price. (No raw Java enum names should
   appear in the player-facing statuses.)
3. A Chef completing preparation with the message that serving starts next turn.
4. A walkout notification and its updated counter (defeat scenario).
5. The final victory and defeat summaries.

The controller's rules are unchanged: Waiter acts first, Chef acts second,
the Cashier runs automatically, and only accepted actions consume staff turns.
