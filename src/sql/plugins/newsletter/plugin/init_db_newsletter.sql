-- liquibase formatted sql
-- changeset newsletter:init_db_newsletter.sql
-- preconditions onFail:MARK_RAN onError:WARN
--
-- Dumping data for table newsletter
--
INSERT INTO newsletter_description values (1,'2007-05-16 13:03:30','Lettre d''information','Lettre d''information pour l''ensemble des utilisateurs','Content',1,'all','TRUE','lutece@lutece.fr','lutece@lutece.fr','Lutece','Pour validation',2);

--
-- Dumping data for table newsletter_template
--

INSERT INTO newsletter_template VALUES (1,'newsletter_model','model_newsletter.html','model-newsletter.svg','all','NEWSLETTER_TEMPLATE',2);

--
-- Dumping data for table newsletter_properties
--
INSERT INTO newsletter_properties VALUES (1,1,null);
 

-- changeset newsletter:init_db_newsletter.sql-rev1.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = database() AND table_name = 'newsletter_properties' AND column_name = 'editor'
--
-- The editor of the composition of the newsletters: grapesjs (the visual email builder shipped with the plugin, by default), lutece (the rich
-- text editor of the back office of the site) or the name of a rich text editor of the back office. Added after the row of the properties,
-- whose INSERT above names no column.
--
ALTER TABLE newsletter_properties ADD COLUMN editor VARCHAR(50) DEFAULT 'grapesjs' NOT NULL;
