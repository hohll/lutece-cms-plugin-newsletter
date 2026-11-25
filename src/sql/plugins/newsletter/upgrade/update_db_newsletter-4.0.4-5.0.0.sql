-- liquibase formatted sql
-- changeset newsletter:update_db_newsletter-4.0.4-5.0.0.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = database() AND table_name = 'newsletter_template' AND column_name = 'template_file_key'
--
-- The files of the newsletter templates are no longer stored in the database (core_file) nor uploaded from the back office.
-- The template of a newsletter is designated by its name (column template_name, formerly template_file_key): the name of a file of the folder
-- WEB-INF/templates/skin/plugins/newsletter/templates, chosen in the feature Newsletter template, which warns when that file is missing.
-- picture_file_key is the name of a picture of the folder themes/admin/shared/plugins/newsletter/images/newsletter_templates.
-- A template whose former template_file_key was the numeric key of a core_file row can no longer be rendered: copy the content of
-- that core_file row into a file of the templates folder, then choose that file in the modification screen of the template.
--
ALTER TABLE newsletter_template RENAME COLUMN template_file_key TO template_name;

--
-- At least one template must be available: the default template shipped with the plugin is declared when it is missing.
--
-- changeset newsletter:update_db_newsletter-4.0.4-5.0.0.sql-rev1.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM newsletter_template WHERE topic_type = 'NEWSLETTER_TEMPLATE' AND template_name = 'model_newsletter.html'
INSERT INTO newsletter_template ( id_template, description, template_name, picture_file_key, workgroup_key, topic_type, sections )
SELECT COALESCE( MAX( id_template ), 0 ) + 1, 'newsletter_model', 'model_newsletter.html', 'model-newsletter.svg', 'all', 'NEWSLETTER_TEMPLATE', 2 FROM newsletter_template;

--
-- A subscriber can be suspended: it keeps its subscriptions but receives no newsletter (status 1 active, 0 suspended)
--
-- changeset newsletter:update_db_newsletter-4.0.4-5.0.0.sql-rev2.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = database() AND table_name = 'newsletter_subscriber_details' AND column_name = 'status'
ALTER TABLE newsletter_subscriber_details ADD COLUMN status SMALLINT DEFAULT 1 NOT NULL;
ALTER TABLE newsletter_subscriber_details ADD COLUMN date_status_update TIMESTAMP NULL;
CREATE INDEX index_newsletter_subscriber_confirmed ON newsletter_subscriber (id_newsletter, confirmed);

--
-- The editor of the composition of the newsletters: grapesjs (the visual email builder shipped with the plugin, by default) or lutece (the rich
-- text editor of the back office, with the profile configured for the newsletter plugin in the Text editor tab of the technical settings)
--
-- changeset newsletter:update_db_newsletter-4.0.4-5.0.0.sql-rev3.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = database() AND table_name = 'newsletter_properties' AND column_name = 'editor'
ALTER TABLE newsletter_properties ADD COLUMN editor VARCHAR(50) DEFAULT 'grapesjs' NOT NULL;
