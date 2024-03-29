package org.example;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;
import org.json.*;
import java.sql.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import javax.swing.plaf.nimbus.State;

/**
 * INSY Webshop Server
 */
public class Server {

    /**
     * Port to bind to for HTTP service
     */
    private int port = 8000;

    /**
     * Connect to the database
     * @throws IOException
     */
    Connection setupDB()  {
        String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        Properties dbProps = new Properties();
        try {
            dbProps.load(new FileInputStream("db.properties"));
            //TODO Connect to DB at the url dbProps.getProperty("url")
            return DriverManager.getConnection(dbProps.getProperty("url"), dbProps);
            // return /* Database connection */ null;
        } catch (Exception throwables) {
            throwables.printStackTrace();
        }
        return null;
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
        Server webshop = new Server();
        webshop.start();
        System.out.println("Webshop running at http://127.0.0.1:" + webshop.port);
    }


    /**
     * Handler for listing all articles
     */
    class ArticlesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            Connection conn = setupDB();

            JSONArray res = new JSONArray();

            //TODO read all articles and add them to res

            Statement st = null;
            try {
                st = conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM articles;");
                while (rs.next()) {
                    res.put(new JSONObject()
                            .put("id", rs.getInt("id"))
                            .put("description", rs.getString("description"))
                            .put("price", rs.getInt("price"))
                            .put("amount", rs.getInt("amount"))
                    );
                }
                rs.close();
                st.close();
                conn.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            /*
            JSONObject art1 = new JSONObject();
            art1.put("id", 1);
            art1.put("description", "Bleistift");
            art1.put("price", 0.70);
            art1.put("amount", 2);
            res.put(art1);
            JSONObject art2 = new JSONObject();
            art2.put("id", 2);
            art2.put("description", "Papier");
            art2.put("price", 2);
            art2.put("amount", 100);
            res.put(art2);
             */

            // Set header to application/json
            t.getResponseHeaders().set("Content-Type", "application/json");
            answerRequest(t,res.toString());
        }

    }

    /**
     * Handler for listing all clients
     */
    class ClientsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            Connection conn = setupDB();

            JSONArray res = new JSONArray();

            //TODO read all clients and add them to res

            Statement st = null;
            try {
                st = conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM clients;");
                while (rs.next()) {
                    res.put(new JSONObject()
                            .put("id", rs.getInt("id"))
                            .put("name", rs.getString("name"))
                            .put("address", rs.getString("address"))
                            .put("city", rs.getString("city"))
                            .put("country", rs.getString("country"))
                    );
                }
                rs.close();
                st.close();
                conn.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            /*
	        JSONObject cli = new JSONObject();
            cli.put("id", 1);
            cli.put("name", "Brein");
            cli.put("address", "TGM, 1220 Wien");
            res.put(cli);
            Ü/
             */

            // Set header to application/json
            t.getResponseHeaders().set("Content-Type", "application/json");
            answerRequest(t,res.toString());
        }

    }


    /**
     * Handler for listing all orders
     */
    class OrdersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            Connection conn = setupDB();

            JSONArray res = new JSONArray();

            //TODO read all orders and add them to res
            // Join orders with clients, order lines, and articles
            // Get the order id, client name, number of lines, and total prize of each order and add them to res

            Statement st = null;
            String query = "SELECT o.id AS order_id, c.name AS client_name, COUNT(ol.id) AS lines, SUM(a.price * ol.amount) AS total_price " +
                    "FROM orders o " +
                    "JOIN clients c ON o.client_id = c.id " +
                    "JOIN order_lines ol ON o.id = ol.order_id " +
                    "JOIN articles a ON ol.article_id = a.id " +
                    "GROUP BY o.id, c.name;";
            try {
                st = conn.createStatement();
                ResultSet rs = st.executeQuery(query);
                while (rs.next()) {
                    res.put(new JSONObject()
                            .put("id", rs.getInt("order_id"))
                            .put("client", rs.getString("client_name"))
                            .put("lines", rs.getInt("lines"))
                            .put("price", rs.getDouble("total_price"))
                    );
                }
                rs.close();
                st.close();
                conn.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            /*
            JSONObject ord = new JSONObject();
	        ord.put("id", 1);
            ord.put("client", "Brein");
            ord.put("lines", 2);
            ord.put("price", 3.5);
            res.put(ord);
            */

            // Set header to application/json
            t.getResponseHeaders().set("Content-Type", "application/json");
            answerRequest(t,res.toString());
        }

    }

   
    /**
     * Handler class to place an order
     */
    class PlaceOrderHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {

            Connection conn = setupDB();
            Map <String,String> params  = queryToMap(t.getRequestURI().getQuery());

            int client_id = Integer.parseInt(params.get("client_id"));

            String response = "";
            int order_id = 1;
            try {


                //TODO Get the next free order id
                Statement st = conn.createStatement();;
                ResultSet rs = st.executeQuery("SELECT id FROM order_lines ORDER BY id DESC LIMIT 1;");
                while (rs.next()) {
                    order_id = rs.getInt("id");
                }
                rs.close();
                st.close();

                //TODO Create a new order with this id for client client_id


                for (int i = 1; i <= (params.size()-1) / 2; ++i ){
                    int article_id = Integer.parseInt(params.get("article_id_"+i));
                    int amount = Integer.parseInt(params.get("amount_"+i));


		            //TODO Get the available amount for article article_id
                    int available = 0;
                    st = conn.createStatement();
                    rs = st.executeQuery("SELECT amount FROM articles WHERE id = " + article_id + ";");
                    while (rs.next()) {
                        available = rs.getInt("amount");
                    }
                    rs.close();
                    st.close();


                    if (available < amount)
                        throw new IllegalArgumentException(String.format("Not enough items of article #%d available", article_id));

		            //TODO Decrease the available amount for article article_id by amount
                    st = conn.createStatement();
                    st.executeUpdate("UPDATE articles SET amount = " + (available - amount) + " WHERE id = " + article_id + ";" );
                    st.close();

		            //TODO Insert new order line
                    String query = "INSERT INTO orders (id, client_id) VALUES (?, ?)";
                    PreparedStatement pst = conn.prepareStatement(query);
                    pst.setInt(1, order_id);
                    pst.setInt(2, client_id);
                    pst.executeUpdate();
                    pst.close();

                    String query2 = "INSERT INTO order_lines (order_id, article_id, amount) VALUES (?, ?, ?)";
                    pst = conn.prepareStatement(query2);
                    pst.setInt(1, order_id);
                    pst.setInt(2, article_id);
                    pst.setInt(3, amount);
                    pst.executeUpdate();
                    pst.close();
                }
                conn.close();
                response = String.format("{\"order_id\": %d}", order_id);
            } catch (IllegalArgumentException | SQLException iae) {
                response = String.format("{\"error\":\"%s\"}", iae.getMessage());
            }

            // Set header to application/json
            t.getResponseHeaders().set("Content-Type", "application/json");
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

            // http://127.0.0.1:8000/placeOrder?client_id=2&article_id_1=2&amount_1=1

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
