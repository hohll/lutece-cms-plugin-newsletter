-- liquibase formatted sql
-- changeset newsletter:update_db_newsletter-4.0.2-4.0.3.sql
-- validCheckSum: 8:1decd0fc2f93f900d920af62a7820524
-- validCheckSum: 9:7c44d91df7069598572295496d6f6d24
-- preconditions onFail:MARK_RAN onError:WARN
-- comment The UPDATE of core_stylesheet (XSL of the subscription portlet) is removed from this released script: on a site migrated to Lutece 8 the core_style* tables are gone and the statement stopped the whole Liquibase update, the core's included; the edit cannot wait for a later script, which would run after this one
ALTER TABLE newsletter_description	CHANGE COLUMN `unsubscribe` `unsubscribe` VARCHAR(6) NULL DEFAULT 'TRUE';

ALTER TABLE newsletter_template RENAME COLUMN `file_name` TO `template_file_key`;
ALTER TABLE newsletter_template RENAME COLUMN `picture` TO `picture_file_key`;

-- The update of the XSL stylesheet of the subscription portlet (core_stylesheet, id 401) has been removed: the portlets are rendered with
-- FreeMarker templates and the core of Lutece 8 has no core_style* tables anymore (plugin-xmltransformer). Kept, the statement stopped the
-- whole Liquibase update of a site migrated from an older version, where the tables are gone.
