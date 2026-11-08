# Design Justification — Stage 1

A point-by-point defense of the architecture against common OOP review criticisms.
Each point maps a design decision to the principle it satisfies.

## 1. Centralized `executeWaiterChoice` dispatch is the GRASP Controller pattern

`GameController` is a textbook implementation of **GRASP's Controller pattern**: a
non-UI object that receives system operations, validates them, and coordinates
domain objects. Spring MVC controllers, transaction scripts in Fowler's *Patterns
of Enterprise Application Architecture*, and hexagonal architecture command
handlers all share this shape: intent enters through one funnel, gets validated,
then executes.

The OO standard satisfied here is separation of two distinct responsibilities
into two classes (**Single Responsibility Principle**): `WaiterChoice` subclasses
encapsulate *what the manager decided*; the controller encapsulates *whether and
how it may be executed*.

## 2. The choice hierarchy is the Command pattern; polymorphism is present where it belongs

`WaiterChoice` is a variant of the **Command pattern**: each player decision is
reified into an immutable first-class object with a type-safe payload. Each
concrete choice carries only the fields it needs — `SeatChoice` carries
`(customerId, tableId)`, `ServeChoice` carries only `orderId` — versus the
antipattern alternative of one class with an ambiguous `int targetId` whose
meaning depends on the action enum. Subtyping makes illegal states
unrepresentable, a stronger encapsulation guarantee than any dispatch mechanism.

The dispatch is also polymorphic: `instanceof` with pattern binding is the
idiomatic modern-Java (16+) construct for exhaustive handling of a closed type
hierarchy, preferable to hand-rolled visitor boilerplate when the subtype set is
small and stable — here, fixed by the spec (SEAT, TAKE_ORDER, SERVE, WAIT,
mirrored by the `WaiterAction` enum). Polymorphism is exercised elsewhere too:
exhaustive `switch` over enums in `RestaurantCLI.customerStatusLabel` /
`orderStatusLabel`, and method overriding throughout the domain model.

## 3. Moving execution into the subclasses would increase coupling

Today, `SeatChoice` depends on nothing outside `java.lang`. It is a pure
immutable value object, usable in a test or serializer with zero domain
knowledge. Under a polymorphic-dispatch design (an `execute` method on each
subclass), every choice class would need to import `GameState`, `Waiter`,
`Customer`, `Table`, `Order`, and `CustomerChoiceGenerator` — five new
dependencies per class, welding the choice layer to domain internals.

**Low Coupling** is an explicit GRASP principle. The current design scores
strictly better: the dispatch chain is a single point that knows the domain,
rather than every data class knowing the domain.

## 4. One funnel = one source of truth for the transaction contract

Validation carries an invariant that spans every choice: **a rejected choice must
not mutate state or consume the staff action**. That guarantee is enforced once,
structurally — every branch runs all checks before the first mutation — and the
shared preconditions (`turnActive`, `waiterActedThisTurn`, null check) live in
one place instead of being duplicated or inherited awkwardly into four
subclasses.

Scattering execution across subclasses fragments this: the no-mutation-on-
rejection discipline would be re-implemented and re-reviewed in four places.
The documented contract at the top of `GameController` (String error for player
input, `IllegalStateException` for lifecycle misuse) states the convention and
the code matches it — cohesion of the rules with their enforcement.

## 5. State protection lives at the right layer

OOP's encapsulation requirement is that objects protect their own invariants,
and they do: `Customer.seatAt` refuses to seat a non-waiting customer,
`Order.advance` owns its state machine transitions, `MenuItem` validates in its
constructor, `GameState` exposes unmodifiable collections. The controller only
coordinates; it cannot corrupt a domain object because the domain objects refuse
invalid operations.

The `Waiter`/`Chef`/`Cashier` guards are documented **defense in depth**: a
second, independent line of checking behind the controller's validation. The
anemic-domain-model criticism does not apply — the domain classes carry their
lifecycle transitions and business queries (`canPayOn`, `canBeServedOn`,
`hasAbandoned`).

## 6. Open/Closed: the honest trade-off, and why it is acceptable

The one real cost of centralized dispatch: adding a fifth waiter action means
editing `GameController`. That is a genuine Open/Closed tension — but the action
set is **closed by the specification** (four actions, matching the
`WaiterAction` enum). For a closed, stable, four-member hierarchy, exhaustive
dispatch has positive value: a reader sees every case in one screen, and there
is no "forgotten subclass" bug class to defend against. Visitor-style indirection
pays off when subtypes are open-ended or volatile; here it would be ceremony
without payoff (**YAGNI**). Refactoring from dispatch to polymorphic execution
later is mechanical precisely because the choices are already clean, immutable
commands (**Protected Variations** — information hiding, OCP's actual mechanism
per Larman).

## Summary

| Design choice | Principle satisfied |
|---|---|
| `GameController` as single entry point for choices | GRASP Controller; SRP; one source of truth for the transaction contract |
| `WaiterChoice` hierarchy with per-subclass payloads | Command pattern; encapsulation; illegal states unrepresentable |
| `instanceof` pattern matching dispatch | Idiomatic closed-hierarchy handling; Low Coupling (choices know nothing of the domain) |
| Domain objects guard their own state (`seatAt`, `advance`, constructor validation) | Information Expert; encapsulation of invariants |
| Staff guard classes | Defense in depth; Protected Variations |
| `GameState` / `TurnProcessor` as state + process holders | Pure Fabrication; High Cohesion |
| String errors for player input vs `IllegalStateException` for misuse | Separation of expected vs exceptional control flow; documented convention |
| Exhaustive switch over enums in the CLI | Polymorphic dispatch with compiler exhaustiveness checking |

One-line version: the design assigns each responsibility to exactly one class —
rules of execution to the controller, data of intent to the choices, state
invariants to the domain objects. Every "missing polymorphism" criticism is
really an argument to move a responsibility somewhere it couples worse.
