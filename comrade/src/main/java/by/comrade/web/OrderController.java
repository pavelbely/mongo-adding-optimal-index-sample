package by.comrade.web;

import by.comrade.model.Bill;
import by.comrade.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController {

    private static final int BONUS_POINTS_REQUIRED_FOR_FREE_LUNCH = 10;

    @Autowired
    private MongoTemplate mongo;

    @RequestMapping(value="/{customer}", method=RequestMethod.GET)
    public Long countExistingBonusPoints(@PathVariable String customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer can't be null");
        }
        return mongo.count(
                Query.query(
                        Criteria.where("customer").is(customer)
                                .and("bonusPoint").is(true)),
                Order.class);
    }

    @RequestMapping(method= RequestMethod.POST, consumes = "application/json")
    public Bill save(@RequestBody Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order can't be null");
        }
        int bonusPointCount = processOrder(order);
        return serveBill(order, bonusPointCount);
    }

    private synchronized int processOrder(Order order) {
        String customer = order.getCustomer();
        int bonusPointCount = countExistingBonusPoints(customer).intValue() + 1;
        boolean isEligibleForBonus = bonusPointCount >= BONUS_POINTS_REQUIRED_FOR_FREE_LUNCH;
        if (isEligibleForBonus) {
            spendBonusPoints(customer);
            bonusPointCount = 0;
        }
        populateOrder(order, isEligibleForBonus);
        mongo.save(order, "order");
        return bonusPointCount;
    }

    private void populateOrder(Order order, boolean isEligibleForBonus) {
        order.setGivenAsBonus(isEligibleForBonus);
        order.setBonusPoint(!isEligibleForBonus);
    }

    private void spendBonusPoints(String customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer can't be null");
        }
        mongo.updateMulti(
                Query.query(
                        Criteria.where("customer").is(customer)
                                .and("bonusPoint").is(true)),
                Update.update("bonusPoint", false),
                Order.class);
    }

    private Bill serveBill(Order order, int bonusPointCount) {
        Bill bill = new Bill();
        bill.setOrderId(order.getId());
        bill.setTotal(countTotal(order));
        bill.setComment(buildComment(order));
        bill.setBonusPointsCount(bonusPointCount);
        return bill;
    }

    private String buildComment(Order order) {
        String prefix = order.isGivenAsBonus() ? "Enjoy your FREE meal" : "Bon appetite";
        return prefix + ", comrade!";
    }

    private double countTotal(Order order) {
        if (order.isGivenAsBonus() || order.getItems() == null || order.getItems().isEmpty()) {
            return 0;
        }
        return order.getItems().stream()
                .mapToDouble(e -> e.getPrice() * e.getQuantity())
                .sum();
    }
}
