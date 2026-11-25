-- liquibase formatted sql
-- changeset newsletter:update_db_newsletter-2.0.11-2.1.1.sql
-- validCheckSum: 8:3a13369d4e3c82aa88c1bc2343375b29
-- validCheckSum: 9:6ca36a25009689ca27f06fe764c6f9df
-- preconditions onFail:MARK_RAN onError:WARN
-- comment The UPDATE of core_stylesheet (XSL of the subscription portlet) is removed from this released script: on a site migrated to Lutece 8 the core_style* tables are gone and the statement stopped the whole Liquibase update, the core's included; the edit cannot wait for a later script, which would run after this one
--
-- Table structure for table newsletter_properties
--

DROP TABLE IF EXISTS newsletter_properties;
CREATE TABLE newsletter_properties (
  validation_activated INT DEFAULT '1' NOT NULL,
  captcha_activated INT DEFAULT '1' NOT NULL,
  tos long VARCHAR 
 );

 --
-- Alter table structure for table newsletter_description
--

ALTER TABLE newsletter_description ADD COLUMN test_subject VARCHAR(255) DEFAULT NULL AFTER sender_name;

--
-- Dumping data for table newsletter_properties
--
INSERT INTO newsletter_properties VALUES (1,1,null);

-- The update of the XSL stylesheet of the subscription portlet (core_stylesheet, id 401) has been removed: the portlets are rendered with
-- FreeMarker templates and the core of Lutece 8 has no core_style* tables anymore (plugin-xmltransformer). Kept, the statement stopped the
-- whole Liquibase update of a site migrated from an older version, where the tables are gone.