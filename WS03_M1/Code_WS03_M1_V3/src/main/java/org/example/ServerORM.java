package org.example;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.json.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.hibernate.query.Query;

/**
 * INSY Webshop Server
 */
public class ServerORM {

    /**
     * Port to bind to for HTTP service
     */
    private int port = 8000;
    private SessionFactory sessionFactory;

    /**
     * Connect to the database
     * @throws IOException
     */
    Session setupDB()  {
        //TODO Create hibernate.cfg.xml file with database properties
        //TODO Create SessionFactory on the first call of this method ONLY, return Session on EACH call.
        if (sessionFactory == null) {
            Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
            sessionFactory = configuration.buildSessionFactory();
        }
        return sessionFactory.openSession();
    }

    /**
     * Startup the Webserver
     * @throws IOException
     */
    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/articles", new ArticlesHandler());
        server.createContext("/clients", new ClientsHandler());
        server.createContext("/placeOrder", new PlaceOrderHandler());
        server.createContext("/orders", new OrdersHandler());
        server.createContext("/", new IndexHandler());

        server.start();
    }


    public static void main(String[] args) throws Throwable {
        ServerORM webshop = new ServerORM();
        webshop.start();
        System.out.println("Webshop running at http://127.0.0.1:" + webshop.port);
    }


    /**
     * Handler for listing all articles
     */
    class ArticlesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            try (Session session = setupDB()) {
                JSONArray res = new JSONArray();
            
                //TODO read all articles and add them to res
                Query<Article> query = session.createQuery("FROM org.example.Article", Article.class);
                List<Article> articles = query.list();

                for (Article article : articles) {
                    JSONObject jsonArticle = new JSONObject();
                    jsonArticle.put("id", article.getId());
                    jsonArticle.put("description", article.getDescription());
                    jsonArticle.put("price", article.getPrice());
                    jsonArticle.put("amount", article.getAmount());
                    res.put(jsonArticle);
                }

                answerRequest(t, res.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Handler for listing all clients
     */
    class ClientsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            try (Session session = setupDB()) {
                JSONArray res = new JSONArray();

                //TODO read all clients and add them to res
                Query<Client> query = session.createQuery("FROM org.example.Client", Client.class);
                List<Client> clients = query.list();

                for (Client client : clients) {
                    JSONObject jsonClient = new JSONObject();
                    jsonClient.put("id", client.getId());
                    jsonClient.put("name", client.getName());
                    jsonClient.put("address", client.getAddress());
                    res.put(jsonClient);
                }

                answerRequest(t, res.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Handler for listing all orders
     */
    class OrdersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            try (Session session = setupDB()) {
                JSONArray res = new JSONArray();

                //TODO read all orders and add them to res
                //Join orders with clients, order lines, and articles
                //Get the order id, client name, number of lines, and total prize of each order and add them to res
                Query<Order> query = session.createQuery("FROM org.example.Order", Order.class);
                List<Order> orders = query.list();

                for (Order order : orders) {
                    JSONObject jsonOrder = new JSONObject();
                    jsonOrder.put("id", order.getId());
                    jsonOrder.put("client", order.getClient().getName());
                    jsonOrder.put("lines", order.getOrderLines().size());

                    double totalPrice = order.getOrderLines().stream()
                            .mapToDouble(ol -> ol.getAmount() * ol.getArticle().getPrice())
                            .sum();
                    jsonOrder.put("price", totalPrice);

                    res.put(jsonOrder);
                }

                answerRequest(t, res.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
   
    /**
     * Handler class to place an order
     */
    class PlaceOrderHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            Session ses = setupDB();
            Map<String, String> params = queryToMap(t.getRequestURI().getQuery());

            int client_id = Integer.parseInt(params.get("client_id"));

            String response = "";
            int order_id = 1;
            try {
                //TODO Get the next free order id
                //TODO Create a new order with this id for client client_id

                for (int i = 1; i <= (params.size() - 1) / 2; ++i) {
                    int article_id = Integer.parseInt(params.get("article_id_" + i));
                    int amount = Integer.parseInt(params.get("amount_" + i));

                    //TODO Get the available amount for article article_id
                    int available = 1000;

                    if (available < amount)
                        throw new IllegalArgumentException(String.format("Not enough items of article #%d available", article_id));

                    //TODO Decrease the available amount for article article_id by amount

                    //TODO Insert new order line
                }

                response = String.format("{\"order_id\": %d}", order_id);
            } catch (IllegalArgumentException iae) {
                response = String.format("{\"error\":\"%s\"}", iae.getMessage());
            }

            answerRequest(t, response);
        }
    }

    /**
     * Handler for listing static index page
     */
    class IndexHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "<!doctype html>\n" +
                    "<html><head><title>INSY Webshop</title><link rel=\"stylesheet\" href=\"https://cdn.jsdelivr.net/npm/water.css@2/out/water.css\"></head>" +
                    "<body><h1>INSY Pseudo-Webshop</h1>" +
                    "<h2>Verf&uuml;gbare Endpoints:</h2><dl>"+
                    "<dt>Alle Artikel anzeigen:</dt><dd><a href=\"http://127.0.0.1:"+port+"/articles\">http://127.0.0.1:"+port+"/articles</a></dd>"+
                    "<dt>Alle Bestellungen anzeigen:</dt><dd><a href=\"http://127.0.0.1:"+port+"/orders\">http://127.0.0.1:"+port+"/orders</a></dd>"+
                    "<dt>Alle Kunden anzeigen:</dt><dd><a href=\"http://127.0.0.1:"+port+"/clients\">http://127.0.0.1:"+port+"/clients</a></dd>"+
                    "<dt>Bestellung abschicken:</dt><dd><a href=\"http://127.0.0.1:"+port+"/placeOrder?client_id=<client_id>&article_id_1=<article_id_1>&amount_1=<amount_1&article_id_2=<article_id_2>&amount_2=<amount_2>\">http://127.0.0.1:"+port+"/placeOrder?client_id=&lt;client_id>&article_id_1=&lt;article_id_1>&amount_1=&lt;amount_1>&article_id_2=&lt;article_id_2>&amount_2=&lt;amount_2></a></dd>"+
                    "</dl></body></html>";

            answerRequest(t, response);
        }

    }


    /**
     * Helper function to send an answer given as a String back to the browser
     * @param t HttpExchange of the request
     * @param response Answer to send
     * @throws IOException
     */
    private void answerRequest(HttpExchange t, String response) throws IOException {
        byte[] payload = response.getBytes();
        t.sendResponseHeaders(200, payload.length);
        OutputStream os = t.getResponseBody();
        os.write(payload);
        os.close();
    }

    /**
     * Helper method to parse query paramaters
     * @param query
     * @return
     */
    public static Map<String, String> queryToMap(String query){
        Map<String, String> result = new HashMap<String, String>();
        for (String param : query.split("&")) {
            String pair[] = param.split("=");
            if (pair.length>1) {
                result.put(pair[0], pair[1]);
            }else{
                result.put(pair[0], "");
            }
        }
        return result;
    }

  
}
