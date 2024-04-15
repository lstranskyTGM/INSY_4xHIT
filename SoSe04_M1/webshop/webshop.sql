
drop table if exists land cascade;

CREATE TABLE land ( --df: mult=0.05
	 id integer PRIMARY KEY,
    lbez varchar(255) NOT NULL
);



drop table if exists adresse cascade;

CREATE TABLE adresse ( --df: mult=1.0
    id integer PRIMARY KEY,
    strasse varchar(255) NOT NULL,
    hnummer integer,
    plz integer NOT NULL,
    ort varchar(255) NOT NULL,
	 land_id integer not null references land(id)
);



drop table if exists artikel cascade;

CREATE TABLE artikel ( --df: mult=1.0
    id integer PRIMARY KEY,    
    anr integer NOT NULL,
    abez varchar(255) NOT NULL,
    npreis numeric(7,2) NOT NULL,
    vstueckz smallint NOT NULL,
    ainfo character varying(255)
);


drop table if exists kunde cascade;

CREATE TABLE kunde ( --df: mult=1.0
	 id integer PRIMARY KEY,
    knr integer NOT NULL,
    email varchar(255) NOT NULL,
    pw varchar(255) NOT NULL
);


drop table if exists bestellung cascade;

CREATE TABLE bestellung ( --df: mult=1.0
	 id integer PRIMARY KEY,
    bnr integer NOT NULL,
    bdatum date NOT NULL,
    bstatus varchar(255) NOT NULL,
	 kunde_id integer not null references kunde(id),
    lieferadr_id int NOT NULL references adresse(id),
    rechnadr_id int references adresse(id)
);


drop table if exists bestellartikel cascade;

CREATE TABLE bestellartikel ( --df: mult=2
	 id integer PRIMARY KEY,
    artikel_id integer NOT NULL references artikel(id),
    bestellung_id integer NOT NULL references bestellung(id),
    anzahl integer NOT NULL
);









