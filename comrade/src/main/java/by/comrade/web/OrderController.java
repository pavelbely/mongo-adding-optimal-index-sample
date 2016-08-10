package by.comrade.web;

import by.comrade.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private MongoTemplate mongo;

    @RequestMapping(value="/{customer}", method=RequestMethod.GET)
    public Long countBonusPoints(@PathVariable String customer) {
        return mongo.count(
                Query.query(
                        Criteria.where("customer").is(customer)
                                .and("bonusPoint").is(true)),
                Order.class);
    }

    @RequestMapping(method= RequestMethod.POST, consumes = "application/json")
    public String save(@RequestBody Order order) {
        mongo.save(order, "order");
        return order.getId();
    }
}
