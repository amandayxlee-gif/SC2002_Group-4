# Stage 3 reproducible gameplay evidence

Compile from the repository root:

```bash
javac -d out $(find src test -name '*.java')
```

Run Stage 3 evidence:

```bash
NO_COLOR=1 java -cp out cli.Main < evidence/stage3/victory_input.txt
NO_COLOR=1 java -cp out cli.Main < evidence/stage3/vip_happyhour_input.txt
NO_COLOR=1 java -cp out cli.Main < evidence/stage3/defeat_input.txt
NO_COLOR=1 java -cp out cli.Main < evidence/stage3/invalid_input.txt
```

The standard victory schedule uses the Host to seat customers independently of Waiter actions and activates Happy Hour on turn 10, when customer C4 places a Salad + Pasta Combo order. The combo's base price is $24.30 (3 preparation units), reduced to $19.44 during Happy Hour. The same turn's activation restores up to 15 satisfaction points to currently eligible customers. The policy remains active for turn 11 and then ends.

The separate VIP scenario activates Happy Hour on turn 4. VIP C2's Burger is discounted first by 10%, then by a further 20%, reaching $10.80, recorded at order placement and charged at payment. The defeat script chooses no actions throughout all 18 turns. The invalid-input scenario tests rejected Happy Hour, Host, Waiter and Chef inputs and a successful Host retry.

The Stage 3 combo-selection rule is deterministic: Customer C4 orders the Salad + Pasta Combo; every other customer retains the Stage 2 food selection rule, including Critic C5's Pasta preference. This deliberately preserves Stage 2 selections for all the other customers.
