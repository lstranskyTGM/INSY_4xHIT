package PersistentClass;

import jakarta.persistence.*;

public class OrderLine {

    private int id;

    private Article article;

    private Order order;

    private int amount;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public OrderLine(int id, Article article, Order order, int amount) {
        this.id = id;
        this.article = article;
        this.order = order;
        this.amount = amount;
    }

    // Constructors, getters, and setters
}
