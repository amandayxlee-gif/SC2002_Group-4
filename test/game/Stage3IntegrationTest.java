package game;

import customer.CriticCustomer;
import customer.Customer;
import customer.CustomerChoiceGenerator;
import customer.Order;
import customer.OrderStatus;
import menu.ComboMeal;
import menu.Menu;

public final class Stage3IntegrationTest {
    private static void check(boolean condition, String description) {
        if (!condition) {
            throw new AssertionError(description);
        }
    }

    public static void main(String[] args) {
        comboAndSelection();
        hostAndHappyHour();
        stackedVipDiscount();
        comboHappyHourAndPreparation();
        System.out.println("ALL STAGE 3 INTEGRATION CHECKS PASSED");
    }

    private static void comboAndSelection() {
        Menu menu = new Menu();
        ComboMeal combo = menu.getComboMeal();
        check(combo.getPriceCents() == 2430, "Combo is 10% below $27.00");
        check(combo.getPreparationUnits() == 3, "Combo requires 3 preparation units");
        check(combo.getFirstItem() == menu.getItem("Salad"), "Combo contains the Salad item");
        check(combo.getSecondItem() == menu.getItem("Pasta"), "Combo contains the Pasta item");
        check(menu.getItems().size() == 3, "Stage 2 base menu is retained");
        check(menu.getAllItems().size() == 4, "Combo appears in available menu");
        CustomerChoiceGenerator generator = new CustomerChoiceGenerator();
        check(generator.chooseItem(menu, new Customer(4)) == combo, "C4 chooses combo");
        check("Burger".equals(generator.chooseItem(menu, new Customer(5)).getName()),
                "Other customers preserve the regular rotation");
        check("Pasta".equals(generator.chooseItem(menu, new CriticCustomer(5)).getName()),
                "Critic's Stage 2 preference remains intact");
        Order order = new Order(1, 4, combo);
        order.advance(10);
        check(order.getStatus() == OrderStatus.PREPARING && order.getRemainingUnits() == 2,
                "First prep unit consumed");
        order.advance(11);
        order.advance(12);
        check(order.getStatus() == OrderStatus.READY && order.getRemainingUnits() == 0,
                "Combo requires all three prep units");
        check(!order.canBeServedOn(12) && order.canBeServedOn(13),
                "Combo cannot be served on its completion turn");
        System.out.println("PASS ComboMeal composition, pricing, units, selection and timing");
    }

    private static void hostAndHappyHour() {
        GameController controller = new GameController(new Menu());
        controller.startTurn();
        check(controller.activateHappyHour(), "Activate Happy Hour at turn start");
        check(controller.getGameState().getHappyHour().getRemainingTurns(1) == 2,
                "Happy Hour has 2 turns remaining on turn 1");
        check(controller.executeHostChoice(new SeatChoice(1, 1)) == null,
                "Host seats C1 independently");
        check(controller.executeWaiterChoice(new TakeOrderChoice(1)) == null,
                "Waiter can take the order after Host seats");
        check(controller.getGameState().findCustomer(1).getOrder().getFinalPriceCents() == 720,
                "Happy Hour discounts a $9 Salad to $7.20");
        check(!controller.activateHappyHour(), "Cannot reactivate after staff actions");
        controller.endTurn();
        controller.startTurn();
        check(controller.getGameState().getHappyHour().getRemainingTurns(2) == 1,
                "Happy Hour has 1 turn remaining on turn 2");
        check(controller.getGameState().findCustomer(1).getSatisfaction() == 100,
                "Happy Hour restores eligible seated customer to satisfaction 100");
        check(controller.getGameState().findCustomer(1).getOrder().getFinalPriceCents() == 720,
                "Earlier order's price remains locked");
        controller.endTurn();
        controller.startTurn();
        check(!controller.getGameState().getHappyHour().isActive(3),
                "Happy Hour expires after 2 turns");
        check(!controller.activateHappyHour(), "Happy Hour can only be used once");
        controller.endTurn();
        System.out.println("PASS Host independence, Happy Hour recovery, duration and price lock");
    }

    private static void stackedVipDiscount() {
        GameController controller = new GameController(new Menu());
        for (int turn = 1; turn <= 3; turn++) {
            controller.startTurn();
            controller.endTurn();
        }
        controller.startTurn();
        check(controller.activateHappyHour(), "Happy Hour activates on VIP arrival turn");
        check(controller.executeHostChoice(new SeatChoice(2, 1)) == null,
                "Host seats VIP C2");
        check(controller.executeWaiterChoice(new TakeOrderChoice(1)) == null,
                "Waiter takes VIP order");
        Order order = controller.getGameState().findCustomer(2).getOrder();
        check(order.getItem().getPriceCents() == 1500, "VIP orders original $15 Burger");
        check(order.getFinalPriceCents() == 1080,
                "VIP 10% discount followed by Happy Hour 20% gives $10.80");
        controller.endTurn();
        System.out.println("PASS VIP discount then Happy Hour discount");
    }

    private static void comboHappyHourAndPreparation() {
        GameController controller = new GameController(new Menu());
        for (int turn = 1; turn <= 9; turn++) {
            controller.startTurn();
            controller.endTurn();
        }
        controller.startTurn();
        check(controller.activateHappyHour(), "Happy Hour activates for C4 combo");
        check(controller.executeHostChoice(new SeatChoice(4, 1)) == null,
                "Host seats C4 on combo turn");
        check(controller.executeWaiterChoice(new TakeOrderChoice(1)) == null,
                "Waiter takes combo order");
        Order order = controller.getGameState().findCustomer(4).getOrder();
        check(order.getItem() instanceof ComboMeal, "C4 receives ComboMeal");
        check(order.getFinalPriceCents() == 1944,
                "Combo $24.30 gets 20% Happy Hour reduction to $19.44");
        check(order.getRemainingUnits() == 3, "Combo starts with 3 preparation units");
        check(controller.executeChefChoice(order.getOrderId()) == null,
                "Chef can prepare one unit on the order placement turn");
        check(order.getRemainingUnits() == 2, "Remaining units reflect one Chef action");
        controller.endTurn();
        System.out.println("PASS Combo with Happy Hour and Chef preparation");
    }
}
