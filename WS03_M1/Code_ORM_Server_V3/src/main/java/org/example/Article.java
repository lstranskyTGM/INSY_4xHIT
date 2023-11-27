package org.example;

public class Article {

    // Attributes
    private int id;
    private String description;
    private int price;
    private int amount;

    // Constructors
    public Article() {
    }

    public Article(String description, int price, int amount) {
        this.description = description;
        this.price = price;
        this.amount = amount;
    }

    // getters and setters
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

}
