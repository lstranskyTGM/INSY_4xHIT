package tgm.leo;

import java.sql.Timestamp;
import java.util.Set;

public class Order {
    private int id;

    private Timestamp createdAt;

    private Client client;

    private Set<OrderLine> orderLines;

    public Set<OrderLine> getOrderLines() {
        return orderLines;
    }

    public void setOrderLines(Set<OrderLine> orderLines) {
        this.orderLines = orderLines;
    }

    public Order() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Order(int id, Timestamp createdAt, Client client) {
        this.id = id;
        this.createdAt = createdAt;
        this.client = client;
    }

    // Constructors, getters, and setters
}
