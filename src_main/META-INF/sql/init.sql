-- Hinweis: Die Variablen %host%, %port%, %dbname%, %username%, %password% werden vor der Ausfuehrung durch die
--          entsprechenden Werte der FrontendDb ersetzt


-- #######################################################################################
-- # Vorgang                                                                             #
-- #######################################################################################
  
-- Function: trf_vorgang()

-- DROP FUNCTION trf_vorgang();

CREATE OR REPLACE FUNCTION trf_vorgang()
  RETURNS trigger AS
$BODY$
DECLARE
  remote_id bigint;                     -- Backend = Frontend: id
  remote_datum varchar(50);             -- Backend = Frontend: datum
  remote_autor_email varchar(300);      -- Backend = Frontend: autor_email
  remote_details text;                  -- Backend = Frontend: details
  remote_status varchar(255);           -- Backend = Frontend: status
  remote_foto_normal_jpg text;          -- Backend = Frontend: foto_normal_jpg
  remote_foto_thumb_jpg text;           -- Backend = Frontend: foto_thumb_jpg
  remote_titel varchar(80);             -- Backend: bemerkung = Frontend: titel
  remote_betreff varchar(300);          -- Backend: status_kommentar = Frontend: bemerkung
  remote_bemerkung varchar(300);
  remote_the_geom geometry;             -- Backend: ovi = Frontend: the_geom
  remote_vorgangstyp varchar(255);      -- Backend: typ = Frontend: vorgangstyp
  remote_kategorieid bigint;            -- Backend: kategorie = Frontend: kategorieid
  remote_foto_vorhanden boolean;        -- Backend: nicht vorhanden 

  query text;

BEGIN
  PERFORM dblink_connect('hostaddr=%host%  port=%port% dbname=%dbname% user=%username% password=%password%');
  
  IF (TG_OP = 'DELETE') THEN
        remote_id = old.id;
        query := 'SELECT dblink_exec(''DELETE FROM klarschiff.klarschiff_vorgang WHERE id = ' || remote_id || ''');';
        EXECUTE query;
      RETURN old;

    ELSIF (TG_OP = 'UPDATE') THEN
      remote_id = new.id;
      remote_autor_email = new.autor_email;
      remote_bemerkung = COALESCE(new.status_kommentar,'');
      IF new.betreff_freigabe_status = 'extern' THEN
        remote_titel = new.betreff;
      ELSE
        remote_titel = '';
      END IF;
    
      remote_foto_vorhanden = FALSE;
      IF new.foto_normal_jpg IS NOT NULL THEN
        remote_foto_vorhanden = TRUE;
        IF new.foto_freigabe_status = 'extern' THEN
          IF ( select substring(version() from 'PostgreSQL #"_#"%' for '#')::int ) >= 9 THEN
            remote_foto_normal_jpg = encode(new.foto_normal_jpg, 'base64');
            remote_foto_thumb_jpg = encode(new.foto_thumb_jpg, 'base64');
           ELSE
            remote_foto_normal_jpg = encode(quote_literal(new.foto_normal_jpg, 'base64'));
            remote_foto_thumb_jpg = encode(quote_literal(new.foto_thumb_jpg, 'base64'));
          END IF;            
        END IF;
      END IF;
    
      remote_datum = new.datum::varchar(50);
      IF new.details_freigabe_status = 'extern' THEN
        remote_details = new.details;
      ELSE
        remote_details = '';
      END IF;
      remote_the_geom = new.ovi;
      remote_status = new.status;
      remote_vorgangstyp = new.typ;
      remote_kategorieid = new.kategorie;
      
      query := 'SELECT dblink_exec(';
      query := query || '''UPDATE klarschiff.klarschiff_vorgang SET ';
      query := query || 'autor_email = ';
      query := query || '''''' || remote_autor_email || '''''';
      query := query || ', titel = ';
      query := query || '''''' || remote_titel || '''''';
      query := query || ', datum = ';
      query := query || '''''' || remote_datum || '''''';
      query := query || ', details = ';
      query := query || '''''' || remote_details || '''''';
      query := query || ', bemerkung= ';
      query := query || '''''' || remote_bemerkung || '''''';
      query := query || ', the_geom = ';
      query := query || '''''' || remote_the_geom::text || '''''';
      query := query || ', status = ';
      query := query || '''''' || remote_status || '''''';
      query := query || ', vorgangstyp = ';
      query := query || '''''' || remote_vorgangstyp || '''''';
      query := query || ', kategorieid = ';
      query := query || '' || remote_kategorieid || '';
      query := query || ', foto_vorhanden = ';
      query := query || '' || remote_foto_vorhanden || '';
    
    IF ( new.foto_normal_jpg IS NOT NULL AND new.foto_freigabe_status = 'extern' ) THEN
      query := query || ', foto_normal_jpg = ';
      query := query || 'decode(''''' || remote_foto_normal_jpg || ''''', ''''base64'''')';
      query := query || ', foto_thumb_jpg = ';
      query := query || 'decode(''''' || remote_foto_thumb_jpg || ''''', ''''base64'''')';
     ELSE
      query := query || ', foto_normal_jpg = NULL';
      query := query || ', foto_thumb_jpg = NULL';
    END IF;
      
      query := query || ' where ';
      query := query || 'id = ' ||  remote_id;
      query := query || ';'');';
    
      RAISE NOTICE 'query: %', query;
    
      EXECUTE query;
   
    RETURN new;

    ELSIF (TG_OP = 'INSERT') THEN
      remote_id = new.id;
      remote_autor_email = new.autor_email;
      remote_betreff = COALESCE(new.status_kommentar,'');
      IF new.betreff_freigabe_status = 'extern' THEN
        remote_titel = new.betreff;
      ELSE
        remote_titel = '';
      END IF;
    
      remote_foto_vorhanden = FALSE;
      IF new.foto_normal_jpg IS NOT NULL THEN
        remote_foto_vorhanden = TRUE;
        IF new.foto_freigabe_status = 'extern' THEN
          remote_foto_normal_jpg = encode(quote_literal(new.foto_normal_jpg)::bytea, 'base64');
          remote_foto_thumb_jpg = encode(quote_literal(new.foto_thumb_jpg)::bytea, 'base64');
        END IF;
      END IF;
    
      remote_datum = new.datum::varchar(50);
      IF new.details_freigabe_status = 'extern' THEN
        remote_details = new.details;
      ELSE
        remote_details = '';
      END IF;
      remote_the_geom = new.ovi;
      -- remote_status_kommentar = new.status_kommentar;
      remote_status = new.status;
      remote_vorgangstyp = new.typ;
      remote_kategorieid = new.kategorie;
      
      query := 'SELECT dblink_exec(
       ''INSERT INTO klarschiff.klarschiff_vorgang (id,
        autor_email,
        titel,
        datum,
        details,
        bemerkung,
        the_geom,
        status,
        vorgangstyp,
        kategorieid,
        foto_vorhanden';

    RAISE NOTICE 'remote_foto_thumb_jpg 1: %', remote_foto_thumb_jpg;
    RAISE NOTICE 'new.foto_thumb_jpg 1: %', new.foto_thumb_jpg;
    RAISE NOTICE 'Insert-Query 1: %', query;
        
    IF remote_foto_normal_jpg IS NOT NULL THEN
      query := query || ', ';
      query := query || 'foto_normal_jpg,';
      query := query || 'foto_thumb_jpg';
    END IF;

      RAISE NOTICE 'remote_foto_thumb_jpg 2: %', remote_foto_thumb_jpg;
      RAISE NOTICE 'Insert-Query 2: %', query;
    
      query := query || ') VALUES(';
      query := query || remote_id || ',';
      query := query || '''''' || remote_autor_email || ''''',';
      query := query || '''''' || remote_titel || ''''',';
      query := query || '''''' || remote_datum || ''''',';
      query := query || '''''' || remote_details || ''''',';
      query := query || '''''' || remote_betreff || ''''',';  
      query := query || '''''' || remote_the_geom::text || ''''',';
      query := query || '''''' || remote_status || ''''',';
      query := query || '''''' || remote_vorgangstyp || ''''',';
      query := query || remote_kategorieid || ',';
      query := query || remote_foto_vorhanden ;

      RAISE NOTICE 'remote_foto_thumb_jpg 3: %', remote_foto_thumb_jpg;
      RAISE NOTICE 'Insert-Query 3: %', query;
     
      IF remote_foto_normal_jpg IS NOT NULL THEN
        query := query || ', ';
        query := query || 'decode(''''' || remote_foto_normal_jpg || ''''', ''''base64''''),';
        query := query || 'decode(''''' || remote_foto_thumb_jpg || ''''', ''''base64'''')';
      END IF;
    
      query := query || ')'');';
    
      RAISE NOTICE 'remote_foto_thumb_jpg 4: %', remote_foto_thumb_jpg;
      RAISE NOTICE 'Insert-Query 4: %', query;
      
      EXECUTE query;
    RETURN new;

   END IF;
  RETURN NULL;

-- Trigger erzeugen:
--------------------
CREATE TRIGGER tr_vorgang
  BEFORE INSERT OR UPDATE OR DELETE
  ON klarschiff_vorgang
  FOR EACH ROW
  EXECUTE PROCEDURE trf_vorgang();

-- Tests:
--------------------
-- update klarschiff_vorgang 
--   set betreff='das ist das haus vom nikolaus',
--   betreff_freigabe_status = 'extern' where id=22;
--------------------

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION trf_vorgang() OWNER TO klarschiff_backend;


-- #######################################################################################
-- # Unterstuetzer                                                                       #
-- #######################################################################################

-- Function: trf_unterstuetzer()

-- DROP FUNCTION trf_unterstuetzer();

CREATE OR REPLACE FUNCTION trf_unterstuetzer()
  RETURNS trigger AS
$BODY$
DECLARE
  remote_id bigint;
  remote_datum character varying(255);
  remote_email_verschluesselt character varying(255);
  remote_vorgang bigint;
  
  query text;

BEGIN
  PERFORM dblink_connect('hostaddr=%host% port=%port% dbname=%dbname% user=%username% password=%password%');

  IF (TG_OP = 'DELETE') THEN
        remote_id      = old.id;
        query := 'SELECT dblink_exec(''DELETE FROM klarschiff.klarschiff_unterstuetzer WHERE id = ' || remote_id || ''');';
        EXECUTE query;
      RETURN old;

    ELSIF (TG_OP = 'UPDATE') THEN
      IF new.datum_bestaetigung IS NOT NULL THEN
        remote_id      = new.id;
        remote_datum   = new.datum_bestaetigung;
        remote_email_verschluesselt = new.hash;
        remote_vorgang = new.vorgang;  
        query := 'SELECT dblink_exec(''UPDATE klarschiff.klarschiff_unterstuetzer SET ';
        query := query || 'datum = ''''' || remote_datum || ''''', ';
        query := query || 'vorgang = ' || remote_vorgang || ', ';
        query := query || 'email_verschluesselt = ''''' || remote_email_verschluesselt || '''''';
        query := query || ' WHERE id = ' || remote_id || ';'')';
        RAISE NOTICE 'Query: %', query;
        EXECUTE query;
       END IF; 
     RETURN new;

    ELSIF (TG_OP = 'INSERT') THEN
        remote_id = new.id;
        remote_datum = new.datum_bestaetigung;
        remote_email_verschluesselt = new.hash;
        remote_vorgang = new.vorgang;
        query := 'SELECT dblink_exec(''INSERT INTO klarschiff.klarschiff_unterstuetzer (id, vorgang) VALUES(';
        query := query || remote_id || ',';
        query := query || remote_vorgang;
        query := query || ')'');';
        IF new.datum_bestaetigung IS NOT NULL THEN
          query := 'SELECT dblink_exec(''INSERT INTO klarschiff.klarschiff_unterstuetzer (id, datum, email_verschluesselt, vorgang) VALUES(';
          query := query || remote_id || ',''''' || remote_datum || ''''',''''';
          query := query || remote_email_verschluesselt || ''''',';
          query := query || remote_vorgang;
          query := query || ')'');';
        END IF;
        EXECUTE query;
       RETURN new;
  END IF;
  RETURN NULL;

-- Trigger erzeugen:
--------------------
CREATE TRIGGER tr_unterstuetzer
  BEFORE INSERT OR UPDATE OR DELETE
  ON klarschiff_unterstuetzer
  FOR EACH ROW
  EXECUTE PROCEDURE trf_unterstuetzer();

-- Tests:
--------------------
-- insert into klarschiff_unterstuetzer
--   (id, datum, datum_bestaetigung, hash, vorgang)
--     values
--   (769, '2011-07-31 19:54:23.881',NULL,'5qgfaijqe74t1k0d1knlbbl3lh',9);
--
-- insert into klarschiff_unterstuetzer
--   (id, datum, datum_bestaetigung, hash, vorgang)
--     values
--   (770, '2011-07-31 20:44:06.462', '2011-07-31 20:50:10', '3o9lqmb9g3ria2lh33hiovjh2j', 9);
--
-- delete from klarschiff_unterstuetzer where id=770;
--
-- insert into klarschiff_unterstuetzer
--   (id, datum, datum_bestaetigung, hash, vorgang)
--     values
--   (769, '2011-07-31 19:54:23.881',NULL,'5qgfaijqe74t1k0d1knlbbl3lh',9);
--
-- update klarschiff_unterstuetzer 
--   set datum_bestaetigung = '2011-07-31 20:50:10' where id = 769;
--------------------

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION trf_unterstuetzer() OWNER TO klarschiff_backend;


-- #######################################################################################
-- # Missbrauchsmeldung                                                                  #
-- #######################################################################################

-- Function: trf_missbrauch()

-- DROP FUNCTION trf_missbrauch();

CREATE OR REPLACE FUNCTION trf_missbrauch()
  RETURNS trigger AS
$BODY$
DECLARE
  remote_id bigint;
--  remote_datum varchar(50); 
  remote_datum timestamp without time zone; 
  remote_datum_abarbeitung timestamp without time zone;
  remote_datum_bestaetigung timestamp without time zone; 
  remote_hash character varying(32);
  remote_text text;
  remote_vorgang bigint;

  query text;

BEGIN
  PERFORM dblink_connect('hostaddr=%host% port=%port% dbname=%dbname% user=%username% password=%password%');

  IF (TG_OP = 'DELETE') THEN
        remote_id      = old.id;
        query := 'SELECT dblink_exec(''DELETE FROM klarschiff.klarschiff_missbrauchsmeldung WHERE id = ' || remote_id || ''');';
        EXECUTE query;
      RETURN old;

    ELSIF (TG_OP = 'UPDATE') THEN
        remote_id                 = new.id;
        remote_datum              = new.datum;
        IF new.datum_abarbeitung IS NOT NULL THEN
          remote_datum_abarbeitung  = new.datum_abarbeitung;
        END IF;
        IF new.datum_bestaetigung IS NOT NULL THEN
          remote_datum_bestaetigung = new.datum_bestaetigung;
        END IF;

        remote_hash               = COALESCE(new.hash,'');
        remote_text               = COALESCE(new.text,'');
        remote_vorgang            = new.vorgang;

        query := 'SELECT dblink_exec(''UPDATE klarschiff.klarschiff_missbrauchsmeldung SET ';
        query := query || 'datum                = ''''' || remote_datum               || ''''', '; 
        IF new.datum_abarbeitung IS NOT NULL THEN
          query := query || 'datum_abarbeitung  = ''''' || remote_datum_abarbeitung   || ''''', ';
        END IF;
        IF new.datum_bestaetigung IS NOT NULL THEN
          query := query || 'datum_bestaetigung = ''''' || remote_datum_bestaetigung  || ''''', '; 
        END IF;
        query := query || 'hash                 = ''''' || remote_hash                || ''''', '; 
        query := query || '"text"               = ''''' || remote_text                || ''''''; 
        query := query || ' WHERE id = ' || remote_id || ';'')';

        -- RAISE NOTICE 'Query: %', query;

        EXECUTE query;
       RETURN new;

    ELSIF (TG_OP = 'INSERT') THEN
        remote_id                 = new.id;
        remote_datum              = new.datum;
        IF new.datum_abarbeitung IS NOT NULL THEN
          remote_datum_abarbeitung  = new.datum_abarbeitung;
        END IF;
        IF new.datum_bestaetigung IS NOT NULL THEN
          remote_datum_bestaetigung = new.datum_bestaetigung;
        END IF;
          
        remote_hash               = COALESCE(new.hash,'');
        remote_text               = COALESCE(new.text,'');
        remote_vorgang            = new.vorgang;

        query := 'SELECT dblink_exec(''INSERT INTO klarschiff.klarschiff_missbrauchsmeldung (id, datum, datum_abarbeitung, datum_bestaetigung, hash, "text", vorgang) VALUES(';
        query := query || remote_id || ','''''; 
        query := query || remote_datum || ''''',';

        IF new.datum_abarbeitung IS NOT NULL THEN
          query := query || '''''' || remote_datum_abarbeitung || ''''',';
         ELSE
          query := query || 'NULL' || ',';
        END IF;
        
        IF new.datum_bestaetigung IS NOT NULL THEN
          query := query || '''''' || remote_datum_bestaetigung || ''''',';
         ELSE
          query := query || 'NULL' || ',';
        END IF;

        query := query || '''''' || remote_hash || ''''',''''';
        query := query || remote_text || ''''',';
        query := query || remote_vorgang;
        query := query || ')'');';

        RAISE NOTICE 'Query: %', query;
        
        EXECUTE query;
       RETURN new;
  END IF;
  RETURN NULL;

-- Trigger erzeugen:
--------------------
CREATE TRIGGER tr_missbrauch
  BEFORE INSERT OR UPDATE OR DELETE ON klarschiff_missbrauchsmeldung
  FOR EACH ROW EXECUTE PROCEDURE trf_missbrauch();

-- Tests:
--------------------
-- insert into klarschiff_missbrauchsmeldung () values ();
-- update klarschiff_missbrauchsmeldung set  where id=;
-- delete from klarschiff_missbrauchsmeldung where id=;

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION trf_missbrauch() OWNER TO klarschiff_backend;



-- #######################################################################################
-- # GeoRss                                                                              #
-- #######################################################################################

-- Function: trf_georss()

-- DROP FUNCTION trf_georss();

CREATE OR REPLACE FUNCTION trf_georss()
  RETURNS trigger AS
$BODY$
DECLARE
DECLARE
  remote_id bigint;
  remote_ideen boolean;
  remote_probleme boolean;
  remote_ideen_kategorien character varying(255);
  remote_probleme_kategorien character varying(255);
  remote_the_geom text;
  
  query text;

BEGIN
  PERFORM dblink_connect('hostaddr=%host% port=%port% dbname=%dbname% user=%username% password=%password%');

  IF (TG_OP = 'DELETE') THEN
        remote_id = old.id;
        query := 'SELECT dblink_exec(''DELETE FROM klarschiff.klarschiff_geo_rss WHERE id = ' || remote_id || ''');';
        EXECUTE query;
      RETURN old;

    ELSIF (TG_OP = 'UPDATE') THEN
      remote_id       = new.id;
      remote_ideen    = new.ideen;
      remote_probleme = new.probleme;
      remote_ideen_kategorien = new.ideen_kategorien;
      remote_probleme_kategorien = new.probleme_kategorien;
      remote_the_geom = new.ovi;
      query := 'SELECT dblink_exec(''UPDATE klarschiff.klarschiff_geo_rss SET ';
      query := query || 'ideen = '    || remote_ideen    || ', ';
      query := query || 'probleme = ' || remote_probleme || ', ';
      query := query || 'ideen_kategorien = ''''' || remote_ideen_kategorien || ''''', ';
      query := query || 'probleme_kategorien = ''''' || remote_probleme_kategorien || ''''', ';
      query := query || 'the_geom = ''''' || remote_the_geom::text || '''''';
      query := query || ' WHERE id = ' || remote_id || '; '')';
      RAISE NOTICE 'Query: %', query;
      EXECUTE query;
    RETURN new;

    ELSIF (TG_OP = 'INSERT') THEN
      remote_id = new.id;
      remote_ideen = new.ideen;
      remote_probleme = new.probleme;
      remote_ideen_kategorien = COALESCE(new.ideen_kategorien,'');
      remote_probleme_kategorien = COALESCE(new.probleme_kategorien,'');
      remote_the_geom = new.ovi;
      
      RAISE NOTICE 'Query, 1. Teilschritt: %', query;

      query := 'select dblink_exec(''INSERT INTO klarschiff.klarschiff_geo_rss (id, ideen, probleme, ideen_kategorien, probleme_kategorien, the_geom) VALUES (';
      query := query || remote_id || ',' ;
      query := query || remote_ideen || ',' ;
      query := query || remote_probleme || ',' ;
      query := query || '''''' || remote_ideen_kategorien || ''''',' ;
      query := query || '''''' || remote_probleme_kategorien || ''''',' ;
      query := query || '''''' || remote_the_geom::text || '''''' ;
      query := query || ');'');' ;
 
      RAISE NOTICE 'Query, 2. Teilschritt: %', query;
      EXECUTE query;

      RETURN NEW;
   END IF;
  RETURN NULL;

-- Trigger erzeugen:
--------------------
CREATE TRIGGER tr_georss
  BEFORE INSERT OR UPDATE OR DELETE
  ON klarschiff_geo_rss
  FOR EACH ROW
  EXECUTE PROCEDURE trf_georss();

-- Tests:
--------------------
-- -- INSERT INTO klarschiff.klarschiff_geo_rss
--  (id, ideen, probleme, ideen_kategorien, probleme_kategorien, the_geom)
--    VALUES
--  (298,true,true,'68,73,80,86,90,97','1,15,26,29,34,41,54,63',
--   '0106000020E96400000100000001030000000100000006000000012CB8D0E6D31241F1D9156712E55641E89B97B331E7124173DD6F1C8EE456412B634BB459D31241BB5BFEB862E15641ABA315B506BD12411BF24F8ECCE15641F5EC5E5FC6C71241BAB1EA588EE35641012CB8D0E6D31241F1D9156712E55641';
--
-- delete from klarschiff_geo_rss where id = 298;
--
-- update klarschiff_geo_rss 
--  set ideen_kategorien = '68,73,80,86' where id = 298;
--------------------

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION trf_georss() OWNER TO klarschiff_backend;


-- #######################################################################################
-- # EnumVorgangStatus                                                                   #
-- #######################################################################################

-- Function: trf_status()

-- DROP FUNCTION trf_status();

CREATE OR REPLACE FUNCTION trf_status()
  RETURNS trigger AS
$BODY$
DECLARE
  remote_id character varying (255);
  remote_name character varying (300);
  remote_nid integer;

  query text;

BEGIN
  PERFORM dblink_connect('hostaddr=%host%  port=%port% dbname=%dbname% user=%username% password=%password%');

  IF (TG_OP = 'DELETE') THEN
        remote_id = old.id;
        query := 'SELECT dblink_exec(''DELETE FROM klarschiff.klarschiff_status WHERE id = ''''' || remote_id || ''''''');';
        EXECUTE query;
      RETURN old;

    ELSIF (TG_OP = 'UPDATE') THEN
      remote_id     = new.id;
      remote_name   = new."text";
      remote_nid    = new.ordinal;
      query := 'SELECT dblink_exec(''UPDATE klarschiff.klarschiff_status SET ';
      query := query || '"name" = ''''' || remote_name || ''''', ';
      query := query || 'nid = ' || remote_nid;
      query := query || ' WHERE id = ''''' || remote_id || ''''';'')';
      RAISE NOTICE 'Query: %', query;
      EXECUTE query;
    RETURN new;

    ELSIF (TG_OP = 'INSERT') THEN
        remote_id      = new.id;
        remote_name   = new."text";
        remote_nid = new.ordinal;
        RAISE NOTICE 'INSERT!';
        query := 'SELECT dblink_exec(''INSERT INTO klarschiff.klarschiff_status (id, "name", nid) VALUES (''''';
        query := query || remote_id || ''''',''''';
        query := query || remote_name || ''''',';
        query := query || remote_nid;
        query := query || ')'');';
        RAISE NOTICE 'Query: %', query;
        EXECUTE query;
      RETURN new;
  END IF;
  RETURN NULL;

-- Trigger erzeugen:
--------------------
CREATE TRIGGER tr_status
  BEFORE INSERT OR UPDATE OR DELETE
  ON klarschiff_enum_vorgang_status
  FOR EACH ROW
  EXECUTE PROCEDURE trf_status();

-- Tests:
--------------------
-- insert into klarschiff_enum_vorgang_status
--   (id, "text", ordinal) values ('keiner', 'keiner', 7);
--
-- delete from klarschiff_enum_vorgang_status where id = 'keiner';
--
-- update klarschiff_enum_vorgang_status
--  set text='offen', ordinal=1 where id='offen';
--------------------

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION trf_status() OWNER TO klarschiff_backend;


-- #######################################################################################
-- # Trashmail                                                                           #
-- #######################################################################################

-- Function: trf_trashmail()

-- DROP FUNCTION trf_trashmail();

CREATE OR REPLACE FUNCTION trf_trashmail()
  RETURNS trigger AS
$BODY$
DECLARE
  remote_id bigint;
  remote_pattern character varying(255);

  query text;

BEGIN
  PERFORM dblink_connect('hostaddr=%host% port=%port% dbname=%dbname% user=%username% password=%password%');

  IF (TG_OP = 'DELETE') THEN
        remote_id      = old.id;
        remote_pattern = old.pattern;
        query := 'SELECT dblink_exec(''DELETE FROM klarschiff.klarschiff_trashmail_blacklist WHERE id = ' || remote_id || ''');';
        EXECUTE query;
      RETURN old;

    ELSIF (TG_OP = 'UPDATE') THEN
        remote_id      = new.id;
        remote_pattern = new.pattern;
        query := 'SELECT dblink_exec(''UPDATE klarschiff.klarschiff_trashmail_blacklist SET ';
        query := query || 'pattern = ''''' || remote_pattern || ''''''; 
        query := query || ' WHERE id = ' || remote_id || ';'')';
        EXECUTE query;
       RETURN new;

    ELSIF (TG_OP = 'INSERT') THEN
        remote_id      = new.id;
        remote_pattern = new.pattern;
        query := 'SELECT dblink_exec(''INSERT INTO klarschiff.klarschiff_trashmail_blacklist (id, pattern) VALUES(';
        query := query || remote_id || ',''''' || remote_pattern || '''''';
        query := query || ')'');';
        EXECUTE query;
       RETURN new;
  END IF;
  RETURN NULL;

-- Trigger erzeugen:
--------------------
CREATE TRIGGER tr_trashmail
  BEFORE INSERT OR UPDATE OR DELETE ON klarschiff_trashmail
  FOR EACH ROW EXECUTE PROCEDURE trf_trashmail();

-- Tests:
--------------------
-- insert into klarschiff_trashmail (id, pattern) values (426, '0815.ru');
-- update klarschiff_trashmail set pattern='0815a.ru' where id=426;
-- delete from klarschiff_trashmail where id=426;

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION trf_trashmail() OWNER TO klarschiff_backend;
