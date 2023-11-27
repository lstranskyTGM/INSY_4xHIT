package org.example;

import java.sql.*;
import java.util.Properties;

public class Main {
    public static <DataSourceClass> void main(String[] args) {
        String url2 = "jdbc:postgresql://localhost:5432/postgres";
        try {
            // set User
            Properties props = new Properties();
            props.setProperty("user", "postgres");
            props.setProperty("password", "root");
            Connection conn = DriverManager.getConnection(url2, props);
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT email FROM customer LIMIT 10;");
            while (rs.next()) {
                System.out.println(rs.getString(1));
            }
            rs.close();
            st.close();
            conn.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}