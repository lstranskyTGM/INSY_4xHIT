package PersistentClass;

import jakarta.persistence.*;

public class Article {

    private int id;

    private String description;
    private int price;
    private int amount;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public Article(int id, String description, int price, int amount) {
        this.id = id;
        this.description = description;
        this.price = price;
        this.amount = amount;
    }

    // Constructors, getters, and setters
}
