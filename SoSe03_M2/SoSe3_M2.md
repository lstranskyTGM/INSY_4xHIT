# UE (GK/EK) Content Management Systeme

Verfasser: **Leonhard Stransky, 4AHIT**

Datum: **07.03.2024**

## Aufgaben (GK)

### Allgemein:

Ziel der Übung soll das Kennenlernen und der Betrieb von CMS Systemen sein.

Hierzu soll an einem praktischen Beispiel (TGM Website) ein passendes Template installiert und ein aussagekräftiges Backend konfiguriert werden.

Die Website soll generell als "One Pager" umgesetzt werden, wobei für die einzelnen Abteilungsseiten schon Unterseiten existieren dürfen. Ihr könnt die vorliegende Übung in Gruppen a maximal 2 Personen durchführen. Die Abgabe erfolgt in Form einer Abnahme eines Protokolls sowie eine Vorführung des Life Systems bei den betreuenden Lehrkräften.

### Schritt 1) Konzeption

Konzipiert in Form einer Benutzerhierachie/Akteurhierachie (UML) ein entsprechendes Berechtigungskonzept für den Schulbetrieb. Die unbedingt notwenigen Rollen hierbei sind:

Schüler, Klassenadministrator (Redakteur einer Klasse), Lehrer, Redakteure, Abteilungsadministratoren, Administratoren.

### Schritt 2) Aufsetzten der Arbeitsumgebung

Schafft in einer eurer virtuellen Umgebungen die geeignete Infrastruktur für eine Installation des Content Management Systems Wordpress (PHP 7.3 oder höher, MySQL oder MariaDB, Apache o.ä.).

Installiert Wordpress von der folgenden Adresse https://wordpress.org/download/

### Schritt 3) Einrichtung und Konfiguration
Richtet wie zuvor geplant die Benutzerhierachie und die entsprechenden Unterseiten im System ein und pflegt einige Dummyinhalte in das System ein.

### Schritt 4) Themeauswahl
Wählt ein geeignetes Theme für euer System aus und passt dies entsprechend den TGM Farben an. Das TGM Logo sowie die TGM Schriftart findet Ihr im Anschluss an die Aufgabenstellung angehängt.

## Aufgaben (EK)

Entwickelt ein eigenes kleines Modul das ihr im Weiteren per Shortcodes in Wordpress einbindet.

Shortcodes bieten unter Wordpress eine sehr einfache Möglichkeit externen Code in euer aktuelles Worpress Projekt einzubinden. Im Anschluss findet Ihr einige Links zu Seiten wo die Thematik shortcodes schnell und einfach erklärt ist.

Shortcodelinks:

https://designers-inn.de/eigene-wordpress-shortcodes-erstellen/

https://kinsta.com/de/blog/wordpress-shortcodes/

### EKü 

In eurem selbst erstellten externen Modul greift ihr auf eine Datenbank (z.b. eine eigene Tabelle der Wordpress Datenbank) zu und verarbeitet formularbasiert Daten aus oder für diese Datenbanktabelle. 

### EKv

Das Modul kann zusätzlich zur Datenbankanbindung noch Informationen aus einem Webservice holen, verarbeiten und anzeigen. Die Daten hierfür erhaltet ihr beispielsweise über JSON Dokumente. Hierzu findet Ihr gleich unterhalb ein exemplarisches Webservice openweathermap, auf das über eine API sehr einfach zugegriffen werden kann.

https://openweathermap.org/ .... 

Hier findet ihr unter guide Infos und unter API und dann die einzelnen DOCs fertige Beispiele. Einzig die Anforderung - ihr müsst euch zunächst einen API Key besorgen (natürlich will der Anbieter, dass ihr euch authentifiziert um einen Missbrauch ihres Services zu verhindern)

Gutes Gelingen!

## Quellen:

[1] https://wordpress.org/download/

[2] https://designers-inn.de/eigene-wordpress-shortcodes-erstellen/

[3] https://kinsta.com/de/blog/wordpress-shortcodes/

[4] https://openweathermap.org/

[5] https://elearning.tgm.ac.at/pluginfile.php/106303/mod_assign/intro/Akteurhierachie_Beispiel.png

[6] [TGM-BOLD[494].TTF](https://elearning.tgm.ac.at/pluginfile.php/106303/mod_assign/introattachment/0/TGM-BOLD%5B494%5D.TTF?forcedownload=1)

[7] [TGM-ITALIC[495].TTF](https://elearning.tgm.ac.at/pluginfile.php/106303/mod_assign/introattachment/0/TGM-ITALIC%5B495%5D.TTF?forcedownload=1)

[8] [TGM.TTF](https://elearning.tgm.ac.at/pluginfile.php/106303/mod_assign/introattachment/0/TGM.TTF?forcedownload=1)

[9] [TGMLogo.svg](https://elearning.tgm.ac.at/pluginfile.php/106303/mod_assign/introattachment/0/TGMLogo.svg?forcedownload=1)











