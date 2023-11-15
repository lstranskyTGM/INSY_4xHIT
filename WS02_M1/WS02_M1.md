# WS02

Verfasser: **Leonhard Stransky, 4AHIT**

Datum: **10.11.2023**

## Projektbeschreibung

Im ersten Teil der Übung lest euch in die Thematik JDBC ein.

Studiert hierfür die beiden angebotenen Unterlagen JDBC und JDBC-Einführung.

Versucht dann in einem kurzen (1 Seite A4) Dokument zusammenzufassen, was die technischen Voraussetzungen für JDBC sind.

Ladet euch gegebenfalls Dinge herunter und nehmt diese in Betrieb. Protokolliert eure Ausführungen.

## Aufgaben:


### Was ist eine JDBC?

Eine JDBC (Java Database Connection) ist eine Möglichkeit, mit Java eine Verbindung zu einer
Datenbank zu erstellen, und mithilfe eins Cursers die Ergebnisse zu parsen.

### Wie kann ich eine JDBC verwenden?

Links zu den Drivern:

MySQL: https://dev.mysql.com/downloads/connector/j/
Oracle: https://www.oracle.com/database/technologies/appdev/jdbc-downloads.html
PostgreSQL: https://jdbc.postgresql.org/

### Code:

```java
package org.example;

import java.sql.*;
import java.util.Properties;

public class Test {
    public static <DataSourceClass> void main(String[] args) {
        // Connection String
        String url2 = "jdbc:postgresql://localhost:5432/postgres";
        try {
            Properties props = new Properties();
            // Setzen des users und des passwords
            props.setProperty("user", "postgres");
            props.setProperty("password", "root");
            // Erstellen der Connection
            Connection conn = DriverManager.getConnection(url2, props);
            // Erstellen eines Queries
            Statement st = conn.createStatement();
            // Ausführen des Queries
            ResultSet rs = st.executeQuery("SELECT email FROM customer LIMIT 10;");
            // Solange es noch einen eintrag gibt, gebe diesen aus
            while (rs.next()) {
                System.out.println(rs.getString(1));
            }
            // Schließen aller Verbindungen
            rs.close();
            st.close();
            conn.close();
        // Fehlerbehandlung
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}
```

Ich habe den JDBC Driver für PostgreSQL verwendet und als maven dependency eingebunden.

## Quellen:

[1] elearning.tgm.ac.at 2023. *Test1* [online] 
Available at: https://elearning.tgm.ac.at/mod/resource/view.php?id=75990 [Accessed 18 March 2023].

[2] elearning.tgm.ac.at 2023. *Test2* [online] 
Available at: https://elearning.tgm.ac.at/pluginfile.php/106253/mod_resource/content/1/JDBC.pdf [Accessed 18 March 2023].













