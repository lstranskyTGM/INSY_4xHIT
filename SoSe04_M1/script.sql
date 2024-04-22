---- Beispiel 1:

-- Query

CREATE INDEX idx_bdatum ON bestellung (bdatum);

EXPLAIN ANALYSE SELECT bnr, bstatus, bdatum
    FROM bestellung
    WHERE TO_CHAR(bdatum, 'YYYY') = '2017';

-- Optimized Query

-- Das genaue Jahr extrahieren
CREATE INDEX idx_bdatum_year ON bestellung (EXTRACT(YEAR FROM bdatum));

EXPLAIN ANALYSE SELECT bnr, bstatus, bdatum
    FROM bestellung
    WHERE EXTRACT(YEAR FROM bdatum) = 2017;

-- Alternative
EXPLAIN ANALYSE SELECT bnr, bstatus, bdatum
    FROM bestellung
    WHERE bdatum >= to_date(2017::varchar, 'YYYY') AND bdatum < to_date(2018::varchar, 'YYYY');

-- Alternative
EXPLAIN ANALYSE SELECT bnr, bstatus, bdatum
    FROM bestellung
    WHERE bdatum = to_date(2017::varchar, 'YYYY');

---- Beispiel 2:

-- Query

CREATE INDEX idx_zusammen ON bestellung (bstatus, bdatum);

EXPLAIN ANALYSE SELECT id, bstatus, bdatum
    FROM bestellung
    WHERE bstatus = 'bstatus_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_73_7'
    ORDER BY bdatum DESC
    LIMIT 1;

-- Optimized Query

-- Create Temporary Column
ALTER TABLE bestellung ADD COLUMN bstatus_short TEXT;

-- Extract Numbers from bstatus
UPDATE bestellung
SET bstatus_short = SUBSTRING(bstatus FROM 'bstatus_([0-9]+)');

-- Original Column löschen und neue Column umbenennen
ALTER TABLE bestellung DROP COLUMN bstatus;
ALTER TABLE bestellung RENAME COLUMN bstatus_short TO bstatus;

-- Or Rename Columns
ALTER TABLE bestellung RENAME COLUMN bstatus TO bstatus_old;
ALTER TABLE bestellung RENAME COLUMN bstatus_short TO bstatus;

-- DESC hinzufügen um ein re-sorting der Daten zu umgehen
CREATE INDEX idx_zusammen_desc ON bestellung (bstatus, bdatum DESC);

-- Index mit allen Spalten erstellen
CREATE INDEX idx_zusammen_covering ON bestellung (bstatus, bdatum DESC) INCLUDE (id);

EXPLAIN ANALYSE SELECT id, bstatus, bdatum
    FROM bestellung
    WHERE bstatus = '100'
    ORDER BY bdatum DESC
    LIMIT 1;

---- Beipsiel 3:

-- Query

CREATE INDEX idx_artikel ON artikel (anr, vstueckz);

EXPLAIN ANALYSE SELECT id, anr, vstueckz
    FROM artikel
    WHERE anr = 7940
    AND vstueckz = 9065;

EXPLAIN ANALYSE SELECT id, anr, vstueckz
    FROM artikel
    WHERE vstueckz = 9065;

-- Optimized Query

-- Index fuer die zweite Query
CREATE INDEX idx_vstueckz ON artikel (vstueckz);

---- Beispiel 4:

-- Query

CREATE INDEX idx_emails ON kunde (email varchar_pattern_ops);

EXPLAIN ANALYSE SELECT id from kunde where email like 'rlugner%';
EXPLAIN ANALYSE SELECT id from kunde where email like '%moertel.at';

-- Optimized Query

-- Umgekehrter Index fuer Suffix-Suche
CREATE INDEX idx_emails_reverse ON kunde (reverse(email));

-- Umgekehrter Index fuer Suffix-Suche mit varchar_pattern_ops
CREATE INDEX idx_emails_reverse_suffix ON kunde (reverse(email) varchar_pattern_ops);

-- Can not be optimized

---- Beispiel 5:

-- Query

CREATE INDEX idx_bestellung_2 ON bestellung (bdatum, kunde_id);

EXPLAIN ANALYSE SELECT id, bdatum, kunde_id
    FROM bestellung
    WHERE bdatum > CURRENT_DATE - INTERVAL '30' YEAR
    AND kunde_id = 42;

-- Optimized Query

-- Id als führenden Index-Schlüssel setzen
CREATE INDEX idx_bestellung_id ON bestellung (kunde_id, bdatum);

-- Index mit allen Spalten erstellen
CREATE INDEX idx_bestellung_covering ON bestellung (kunde_id, bdatum) INCLUDE (id);

-- Can not be optimized