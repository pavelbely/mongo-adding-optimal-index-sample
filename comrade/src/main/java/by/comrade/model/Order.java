package by.comrade.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;
import java.util.LinkedHashSet;

@Document
public class Order {

    @Id
    private String id;

    private String customer;

    private Collection<Item> items = new LinkedHashSet<>();

    private boolean bonusPoint;

    private boolean givenAsBonus;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public Collection<Item> getItems() {
        return items;
    }

    public void setItems(Collection<Item> items) {
        this.items = items;
    }

    public boolean isBonusPoint() {
        return bonusPoint;
    }

    public void setBonusPoint(boolean bonusPoint) {
        this.bonusPoint = bonusPoint;
    }

    public boolean isGivenAsBonus() {
        return givenAsBonus;
    }

    public void setGivenAsBonus(boolean givenAsBonus) {
        this.givenAsBonus = givenAsBonus;
    }
}
