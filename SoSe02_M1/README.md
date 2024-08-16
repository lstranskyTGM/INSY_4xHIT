# UE (GK+EK) Datenbankseitige Programmierung

Verfasser: **Leonhard Stransky, 4AHIT**

Datum: **24.02.2024**

## Projektbeschreibung

Moderne Datenbankmanagementsysteme wie PostgreSQL erlauben es, Programmcode direkt in der Datenbank laufen zu lassen. Dabei werden die Daten nicht wie sonst ueblich mittels SELECT-Queries zum Client kopiert, dort bearbeitet und ausgewertet (und ggf wieder mit INSERT/UPDATE/DELETE Statements zurueck zum Server geschickt) -- stattdessen bleiben die Daten am Server und die Verarbeitung geschieht im DBMS selbst. Dies kann in der Praxis enorme Geschwindigkeitsvorteile bringen, schon alleine dadurch, dass weniger Daten zwischen Client und Server hin- und hergeschickt werden muessen. In SQL ist der generelle Ueberbegriff hierfuer Stored Procedures.

Wir werden die Datenbank aus der letzten Laborangabe zur Verwaltung von Kontodaten verwenden bzw. erweitern:

```sql
CREATE TABLE accounts (
    client_id integer primary key,
    amount decimal,
    deactivated boolean
);
```

```sql
CREATE TABLE transfers (
    transfer_id integer primary key,
    from_client_id integer,
    to_client_id integer,
    date date, 
    amount decimal
);
```

Programmcode kann auf verschiedene Art und Weise in die Datenbank eingebunden und damit auch aufgerufen werden, die gebraeuchlichsten Methoden sind Functions, Procedures und Trigger:

## Functions 

Eine einfache eigene Funktion kann in Postgres zum Beispiel mit

```sql
CREATE OR REPLACE FUNCTION meineFunktion(f1 INTEGER, f2 INTEGER) 
        RETURNS INTEGER  
        AS $$ 
        BEGIN  
           RETURN f1 * f2; 
        END; 
        $$ LANGUAGE plpgsql;
```

definiert werden. Diese Funktion koennte man dann in einer SQL-Query mit SELECT meineFunktion(2,3); aufgerufen werden. Die verwendete Sprache ist hier plpgsql, eine prozedurale Erweiterung von SQL, die Variablen, Schleifen, If-Statementents, usw. umfasst. Zum Beispiel liefert die folgende Funktion eine Bilanz (Einzahlungen - Auszahlungen) fuer einen gegebenen Benutzer:

```sql
CREATE OR REPLACE FUNCTION bilanz(client_id INTEGER) 
        RETURNS INTEGER  
        AS $$ 
        DECLARE
           einzahlungen DECIMAL;
           auszahlungen DECIMAL;
        BEGIN  
           SELECT SUM(amount) INTO einzahlungen FROM transfers WHERE to_client_id = client_id;
           SELECT SUM(amount) INTO auszahlungen FROM transfers WHERE from_client_id = client_id;           
           RETURN einzahlungen - auszahlungen; 
        END; 
        $$ LANGUAGE plpgsql;
```

Beachte, dass Variablen extra deklariert werden muessen und das Ergebnis einer SQL Query hier mit INTO in einer Variablen abgelegt wird. Neben reinen SELECT-Statements sind natuerlich auch INSERT, UPDATE und DELETE-Queries moeglich.

### Aufgabe 1

Erweitere die Funktion 'bilanz' wie folgt: Die Bank hat bis 2019 bei jeder Transaktion eine Steuer von 2% eingehoben - d.h. ein Transfer von 100.- soll in der Bilanz des Empfaengers nur mit 98.- gewertet werden. 2020 wurde diese Steuer auf 1% gesenkt. Erstelle eine Funktion bilanz_mit_steuer, die diese beiden Steuersaetze beruecksichtigt. (Hint: mit date_part('year',date) laesst sich das Jahr zu einem Datum ermitteln.) Rufe deine Funktion mit select bilanz(<id>) auf und teste anhand von passenden Daten, ob sie auch funktioniert.

```sql
CREATE OR REPLACE FUNCTION bilanz_mit_steuer(client_id INTEGER) 
RETURNS DECIMAL
AS $$ 
DECLARE
    einzahlungen DECIMAL;
    auszahlungen DECIMAL;
BEGIN  
    -- Einzahlungen mit Steuer
    SELECT SUM(CASE 
                WHEN date_part('year', date) <= 2019 THEN amount * 0.98 
                ELSE amount * 0.99 
            END) INTO einzahlungen 
    FROM transfers 
    WHERE to_client_id = client_id;

    -- Auszahlungen ohne Steuer
    SELECT SUM(amount) INTO auszahlungen 
    FROM transfers 
    WHERE from_client_id = client_id;     

    -- Berechnung der Bilanz
    RETURN einzahlungen - auszahlungen; 
END; 
$$ LANGUAGE plpgsql;
```

## Procedures

Procedures sind Functions sehr aehnlich, mit einem wichtigen Unterschied: Wahrend Functions immer komplett in einer Transaktion ablaufen, hat man in einer Procedure die Kontrolle ueber die aktuelle Funktion. (D.h. man kann Kommandos wie COMMIT und ROLLBACK ausfuehren.) Sie werden daher haeufiger fuer Datenmanipulationen eingesetzt.

```sql
CREATE  OR  REPLACE  PROCEDURE transfer_direct(sender INTEGER, recipient INTEGER, howmuch DECIMAL) 
    AS $$ 
    BEGIN  
        UPDATE accounts SET amount = amount - howmuch WHERE client_id = sender; 
        UPDATE accounts SET amount = amount + howmuch WHERE client_id = recipient; 
        COMMIT; 
    END; 
    $$ LANGUAGE plpgsql;
```

Eine Procedure wird im Gegensatz zu einer Function ausserhalb von einer Query mit dem Kommando CALL aufgerufen: CALL transfer_direct(1,2,100); ueberweist 100,- von Konto 1 an Konto 2

### Aufgabe 2

Passe die Procedure transfer_direct() so an, dass die Ueberweisung nicht durchgefuehrt wird, falls das Konto nicht gedeckt ist, d.h., wenn am Konto des Senders weniger Geld vorhanden ist, als ueberwiesen werden soll. (Hint 1: mit zum Beispiel IF a < b THEN ... END IF; lassen sich Fallunterscheidungen durchfuehren.) (Hint 2: mit RAISE EXCEPTION 'meine Fehlermeldung' laesst sich die Procedure abbrechen.)

```sql
CREATE OR REPLACE PROCEDURE transfer_direct(sender INTEGER, recipient INTEGER, howmuch DECIMAL) 
AS $$
DECLARE
    sender_balance DECIMAL;
BEGIN  
    -- Ermittlung des aktuellen Guthabens des Senders
    SELECT amount INTO sender_balance FROM accounts WHERE client_id = sender;
    
    -- Überprüfung, ob das Guthaben für die Überweisung ausreicht
    IF sender_balance < howmuch THEN
        -- Auslösen einer Exception, wenn das Guthaben nicht ausreicht
        RAISE EXCEPTION 'Nicht genügend Guthaben für die Überweisung. Verfügbar: %, erforderlich: %', sender_balance, howmuch;
    ELSE
        -- Durchführung der Überweisung, wenn das Guthaben ausreicht
        UPDATE accounts SET amount = amount - howmuch WHERE client_id = sender; 
        UPDATE accounts SET amount = amount + howmuch WHERE client_id = recipient; 
    END IF;
    COMMIT;
END; 
$$ LANGUAGE plpgsql;
```

## Triggers

Als Trigger bezeichnet man eine spezielle Art von Funktionen, welche vom Datenbankmanagementsystem automatisch aufgerufen werden, wenn bestimmte Ereignisse eintreten. Solche Ereignisse sind zum Beispiel das Einfuegen, Aendern oder Loeschen von Datensaetzen. Trigger koennen dieses Ereigniss verhindern, die zu einzufuegenden Daten modifizieren, oder auch in andere Tabellen schreiben. Zuerst definiert man hierfuer eine Function, die den Returntyp trigger besitzt und richtet diese dann mit dem Kommando CREATER TRIGGER als Trigger ein. Dabei kann man angeben, ob der Trigger vor oder nach einer Operation aufgerufen werden soll. Mit dem Kommando DROP TRIGGER name ON tabelle; kann ein Trigger wieder entfernt werden.

Moechte man zum Beispiel verhindern, dass Konten geloescht werden koennen (zB aus rechltichen Gruenden), koennte man wie folgt vorgehen: Man definiert einen BEFORE DELETE Trigger auf die Accounts Tabelle. In diesem Trigger wird der zu loeschende Datesatz lediglich deaktiviert, anstatt geloescht. Dadurch, dass der Trigger NULL zurueckgibt, wird die Loesch-Aktion nicht durchgefuert:

```sql
CREATE OR REPLACE FUNCTION deactivate_user() 
RETURNS trigger 
AS $$ 
BEGIN  
    update accounts set deactivated = TRUE where client_id = OLD.client_id;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql; 
```

```sql
-- Implement Trigger
CREATE TRIGGER no_del_accounts BEFORE DELETE ON accounts FOR EACH ROW EXECUTE PROCEDURE deactivate_user();
```

Moechte man nun ein Konto mittels delete from accounts where client_id = 1 loeschen, so wird dieses stattdessen nur deaktiviert. Beachte dabei, wie unter OLD.client_id die id des zu loeschenden Datensatzes zugreifbar ist. Ebenso existiert bei INSERT und UPDATE Triggern der Datensatz NEW, welcher den neuen zu speichernden Datensatz beinhaltet.

### Aufgabe 3

Erstelle einen Trigger, welcher bewirkt, dass bei dem Anlegen eines neuen Transfers der aktuelle Kontostand in der accounts-Tabelle automatisch angepasst wird. Erstelle dafuer eine Function update_accounts(), die die noetigen Anpassungen durchfuehrt und mache diese Funktion mittels CREATE TRIGGER new_transfer BEFORE INSERT ON transfers FOR EACH ROW EXECUTE PROCEDURE update_accounts(); zum Trigger. (Hint: Damit der Transfer auch gespeichert wird, muss dein Trigger RETURN NEW zurueckgeben);

```sql
CREATE OR REPLACE FUNCTION update_accounts() 
RETURNS TRIGGER AS $$
BEGIN
    -- Erhöhung des Kontostands des Empfängers
    UPDATE accounts SET amount = amount + NEW.amount WHERE client_id = NEW.to_client_id;
    
    -- Verringerung des Kontostands des Absenders
    UPDATE accounts SET amount = amount - NEW.amount WHERE client_id = NEW.from_client_id;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

```sql
-- Implementing Trigger
CREATE TRIGGER new_transfer BEFORE INSERT ON transfers
FOR EACH ROW EXECUTE FUNCTION update_accounts();
```

### Aufgabe 4

Passe den Trigger aus Aufgabe 3 so an, dass nur Transfers angenommen werden, die das Senderkonto nicht ueberziehen. D.h. ueberpruefe, ob das Konto nach der Ueberweisung unter 0.- fallen wuerde und verhindere in diesem Fall mit RETURN NULL das Einfuegen der Transaktion

```sql
CREATE OR REPLACE FUNCTION update_accounts_safe() 
RETURNS TRIGGER AS $$
DECLARE
    sender_balance DECIMAL;
BEGIN
    -- Ermittlung des aktuellen Guthabens des Senders
    SELECT amount INTO sender_balance FROM accounts WHERE client_id = NEW.from_client_id;
    
    -- Überprüfung, ob das Guthaben ausreicht
    IF sender_balance < NEW.amount THEN
        -- Wenn das Guthaben nicht ausreicht, wird die Transaktion nicht durchgeführt
        RAISE EXCEPTION 'Nicht genügend Guthaben für die Überweisung. Verfügbar: %, erforderlich: %', sender_balance, NEW.amount;
    ELSE
        -- Ansonsten Durchführung der Kontostandanpassung
        UPDATE accounts SET amount = amount - NEW.amount WHERE client_id = NEW.from_client_id;
        UPDATE accounts SET amount = amount + NEW.amount WHERE client_id = NEW.to_client_id;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

```sql
-- Implementing Trigger
DROP TRIGGER IF EXISTS new_transfer ON transfers;

CREATE TRIGGER new_transfer BEFORE INSERT ON transfers
FOR EACH ROW EXECUTE FUNCTION update_accounts_safe();
```

### Showcase

```sql
-- Test Function
SELECT meinefunktion(1, 2);

-- Bilanz ausgeben
SELECT bilanz_mit_steuer(2);
SELECT bilanz(2);

-- Transfer between users
CALL transfer_direct(2, 1, 2000000000);
CALL transfer_direct(1, 2, 100);

-- Add Transfers to table
INSERT INTO transfers (transfer_id, from_client_id, to_client_id, date, amount)
    VALUES (80, 3, 4, '2024-03-18', 5.00);
INSERT INTO transfers (transfer_id, from_client_id, to_client_id, date, amount)
    VALUES (80, 3, 4, '2024-03-18', 50000000000.00)
```

## Zusammenfassung

### Functions:

Funktionen sind Routinen, die bestimmte Aufgaben in der Datenbank ausführen und ein Ergebnis zurückgeben. Sie können in der prozeduralen Sprache PL/pgSQL definiert werden und ermöglichen Operationen wie Berechnungen und Datenmanipulationen innerhalb der Datenbank.

```sql
-- Example Function (That adds two Numbers)
CREATE OR REPLACE FUNCTION add_numbers(a INTEGER, b INTEGER) 
RETURNS INTEGER AS $$
BEGIN
    RETURN a + b;
END;
$$ LANGUAGE plpgsql;
```

```sql
-- Executing Function
SELECT add_numbers(5, 10) AS result;
```

### Procedures:

Prozeduren ähneln Funktionen, führen jedoch Aufgaben aus, ohne zwingend einen Wert zurückzugeben. Sie bieten mehr Kontrolle über Transaktionen, da sie Befehle wie COMMIT und ROLLBACK innerhalb ihres Körpers enthalten können.

```sql
-- Example Procedure (That outputs a Message)
CREATE OR REPLACE PROCEDURE show_message()
LANGUAGE plpgsql
AS $$
BEGIN
    RAISE NOTICE 'This is an example Function';
END;
$$;
```

```sql
-- Executing Procedure:
CALL show_message();
```

### Trigger:

Trigger sind spezielle Routinen, die automatisch in Reaktion auf bestimmte Datenbankereignisse ausgelöst werden, wie z.B. das Einfügen, Ändern oder Löschen von Datensätzen. Sie können verwendet werden, um Datenintegrität sicherzustellen, automatische Aktionen durchzuführen oder Änderungen in anderen Tabellen zu protokollieren.

```sql
-- Example Tables:
CREATE TABLE IF NOT EXISTS accounts (
    account_id SERIAL PRIMARY KEY,
    account_name TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS logs (
    log_id SERIAL PRIMARY KEY,
    message TEXT NOT NULL,
    logged_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

```sql
-- Example Trigger (That creates an Log-Entry)
CREATE OR REPLACE FUNCTION log_account_creation()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO logs (message) VALUES ('Account created');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

```sql
-- Example Implementation:
CREATE TRIGGER account_creation_trigger
AFTER INSERT ON accounts
FOR EACH ROW
EXECUTE FUNCTION log_account_creation();
```

## EK

### Aufgabenstellung

Erstelle eine Tabelle `statistics`, welche fuer jeden Tag (an dem Ueberweisungen durchgefuehrt wurden) statistische Daten sammelt:

```sql
CREATE TABLE statistics (
    date date,
    sum_amount decimal,
    avg_amount decimal,
    max_amount decimal,
    taxes decimal 
);
```

wobei `sum_amount` die Gesamtsumme der an diesem Tag getaetigten Ueberweisungen bezeichnet, `avg_amount` den durchschnittlichen Betrag der Ueberweisungen, `max_amount` die groesste Ueberweisung und `taxes` die insgesamt an diesem Tag angefallenen Steuern (2% bis 2019 und 1% ab 2020). Erstelle eine Procedure `calc_statistics()`, die die `statistics`-Tabelle anhand der in der `transfers`-Tabelle gespeicherten Ueberweisungen komplett befuellt.

### Umsetzung

Die zur Umsetzung verwendete Programmiersprache ist freigestellt. Neben PL/pgSQL unterstuetzt Postgres von sich aus auch noch andere prozedurale Programmiersprachen wie zB Python [1] oder Perl. Zusaetzlich gibt es open source Projekte fuer die Unterstuetzung von JavaScript [2], PHP [3], oder Java [4].

Falls du dich entschliesst, weiterhin PL/pgSQL [6] zu verwenden, wirst du zum Auslesen der Datensaetze das Konzept eines **Cursors** benoetigen: Ein Cursor bezeichnet im Prinzip das Ergebnis einer Query und erlaubt es dir, auf die einzelnen Ergebnisse der Query in einer Schleife Schritt fuer Schritt zuzugreifen, wie zum Beispiel unter [5] beschrieben. Grundsaetzlich wird ein Cursor zunaechst unter DECLARE deklariert; in der Funktion kann er dann mit `OPEN cursorname` geoeffnet werden - Hier wird die eigentliche Query ausgefuehrt. In der LOOP --- END LOOP Schleife wird bei jedem Durchlauf mittels `FETCH cursorname INTO variablenname` das naechste Ergebnis in einer Variablen abgelegt und kann weiter bearbeitet werden. Abgebrochen wird die Schleife mit `EXIT WHEN NOT FOUND` wenn alle Datensaetze abgearbeitet wurden. Der folgende Sourcecode iteriert zum Beispiel ueber alle Jahre, in denen Ueberweisungen getaetigt wurden. - Fuer die Loesung der Uebungsaufgabe wirst du analog ueber alle Tage iterieren muessen.

```sql
DECLARE 
  y INTEGER;
  years CURSOR FOR  SELECT DISTINCT DATE_PART('year', date)  FROM transfers; 
BEGIN  
   OPEN years;
   LOOP 
     FETCH years INTO y; 
     EXIT WHEN NOT FOUND;
     -- In "y" steht jetzt das jeweilige Jahr
   END  LOOP;
   CLOSE years;
 END;
```

### Solution

1. Create the `statistics` table:

```sql
CREATE TABLE statistics (
    date date,
    sum_amount decimal,
    avg_amount decimal,
    max_amount decimal,
    taxes decimal
);
```

2. Create the `calc_statistics()` procedure:

```sql
CREATE OR REPLACE PROCEDURE calc_statistics() 
AS $$
DECLARE
    -- Declare variables for cursor and daily statistics
    transfer_date RECORD;
    daily_stats RECORD;
    year_tax_rate NUMERIC;
    daily_cursor CURSOR FOR SELECT DISTINCT date FROM transfers ORDER BY date;
BEGIN
    -- Open cursor to iterate over distinct transfer dates
    OPEN daily_cursor;
    LOOP
        -- Fetch the next transfer date
        FETCH daily_cursor INTO transfer_date;
        -- Exit loop when no more dates are found
        EXIT WHEN NOT FOUND;
        
        -- Determine the tax rate based on the year
        IF DATE_PART('year', transfer_date.date) <= 2019 THEN
            year_tax_rate := 0.02;  -- Tax rate of 2% up to 2019
        ELSE
            year_tax_rate := 0.01;  -- Tax rate of 1% from 2020
        END IF;

        -- Calculate daily statistics for the specific date
        SELECT
            SUM(amount) AS total_amount,
            AVG(amount) AS average_amount,
            MAX(amount) AS max_amount,
            SUM(amount * year_tax_rate) AS total_taxes
        INTO daily_stats
        FROM transfers
        WHERE date = transfer_date.date;  -- Filter transfers for the specific date

        -- Insert the calculated statistics into the statistics table
        INSERT INTO statistics (date, sum_amount, avg_amount, max_amount, taxes)
        VALUES (
            transfer_date.date, 
            daily_stats.total_amount, 
            daily_stats.average_amount, 
            daily_stats.max_amount, 
            daily_stats.total_taxes
        );
    END LOOP;
    -- Close the cursor
    CLOSE daily_cursor;
END;
$$ LANGUAGE plpgsql;
```

1. Execute the `calc_statistics()` procedure:

```sql
CALL calc_statistics();
```

1. Function to generate random transactions:

```sql
CREATE OR REPLACE FUNCTION generate_random_transactions(days INTEGER, max_trans_per_day INTEGER)
RETURNS void AS $$
DECLARE
    -- Declare variables for generating random transactions
    v_current_date DATE;
    num_transactions INTEGER;
    i INTEGER;
    j INTEGER;
    from_client INTEGER;
    to_client INTEGER;
    trans_amount NUMERIC;
    available_balance NUMERIC;
    next_transfer_id INTEGER;
BEGIN
    -- Initialize the next_transfer_id
    SELECT COALESCE(MAX(transfer_id), 0) + 1 INTO next_transfer_id FROM transfers;

    -- Set the starting date for transactions
    v_current_date := CURRENT_DATE - days;

    FOR i IN 1..days LOOP
        -- Randomly determine the number of transactions for the current day
        num_transactions := FLOOR(RANDOM() * max_trans_per_day) + 1;

        -- Generate random transactions for the current day
        FOR j IN 1..num_transactions LOOP
            -- Select a random 'from_client_id' that exists and has a positive balance
            LOOP
                from_client := (SELECT client_id FROM accounts WHERE amount > 10 ORDER BY RANDOM() LIMIT 1); -- Accounts with at least $10
                SELECT amount INTO available_balance FROM accounts WHERE client_id = from_client;

                -- Break the loop if a valid client with a positive balance is found
                EXIT WHEN available_balance IS NOT NULL AND available_balance > 10; -- Extra margin to avoid rounding issues
            END LOOP;

            -- Select a random 'to_client_id' ensuring it is not the same as 'from_client_id'
            LOOP
                to_client := (SELECT client_id FROM accounts WHERE client_id <> from_client ORDER BY RANDOM() LIMIT 1);

                -- Break the loop if a valid client is found
                EXIT WHEN to_client IS NOT NULL;
            END LOOP;

            -- Determine a safe transaction amount
            -- This amount should not exceed the 'available_balance' of 'from_client'
            trans_amount := ROUND((RANDOM() * (LEAST(500, available_balance - 10) - 10) + 10)::NUMERIC, 2); -- Ensuring there's always a $10 buffer

            -- Insert the generated transaction into 'transfers'
            INSERT INTO transfers (transfer_id, from_client_id, to_client_id, date, amount)
            VALUES (next_transfer_id, from_client, to_client, v_current_date, trans_amount);
            next_transfer_id := next_transfer_id + 1; -- Increment the transfer_id
        END LOOP;

        -- Increment the date for the next day of transactions
        v_current_date := v_current_date + 1;
    END LOOP;
END;
$$ LANGUAGE plpgsql;
```

5. Execute function to generate random transactions:

```sql
SELECT generate_random_transactions(10, 500);
```

## Quellen:

[1] https://www.postgresqltutorial.com/postgresql-create-function/ 

[2] https://www.postgresql.org/docs/current/plpgsql.html

[3] https://www.postgresql.org/docs/12/trigger-definition.html