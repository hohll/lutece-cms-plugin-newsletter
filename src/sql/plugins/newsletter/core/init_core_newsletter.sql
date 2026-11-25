-- liquibase formatted sql
-- changeset newsletter:init_core_newsletter.sql
-- validCheckSum: 8:270aa56dd49168a9e2339140bc62586c
-- validCheckSum: 9:0aecda90b4983d85ce34b92639cecdbe
-- preconditions onFail:MARK_RAN onError:WARN
--
-- Dumping data for table core_admin_right
--

INSERT INTO core_admin_right (id_right,name,level_right,admin_url,description,is_updatable,plugin_name,id_feature_group,icon_url,documentation_url) VALUES ('NEWSLETTER_MANAGEMENT','newsletter.adminFeature.newsletter_management.name',2,'jsp/admin/plugins/newsletter/ManageNewsLetter.jsp','newsletter.adminFeature.newsletter_management.description',0,'newsletter','CONTENT','ti ti-news', NULL);
INSERT INTO core_admin_right (id_right,name,level_right,admin_url,description,is_updatable,plugin_name,id_feature_group,icon_url,documentation_url) VALUES ('NEWSLETTER_TEMPLATE_MANAGEMENT','newsletter.adminFeature.newsletter_template_management.name',2,'jsp/admin/plugins/newsletter/ManageTemplates.jsp','newsletter.adminFeature.newsletter_template_management.description',0,'newsletter','STYLE','ti ti-template', NULL);

--
-- Dumping data for table core_admin_role
--

INSERT INTO core_admin_role (role_key,role_description) VALUES ('newsletter_manager','The role needed to access a newsletter template');


--
-- Dumping data for table core_admin_role_resource
--

INSERT INTO core_admin_role_resource (role_key,resource_type,resource_id,permission) VALUES ('newsletter_manager','NEWSLETTER','*','*');
INSERT INTO core_admin_role_resource (role_key,resource_type,resource_id,permission) VALUES ('newsletter_manager','NEWSLETTER_TEMPLATE','*','*');

 
--
-- Dumping data for table core_portlet_type
--

INSERT INTO core_portlet_type (id_portlet_type,name,url_creation,url_update,home_class,plugin_name,url_docreate,create_script,create_specific,create_specific_form,url_domodify,modify_script,modify_specific,modify_specific_form,icon_name) VALUES ('NEWSLETTER_ARCHIVE_PORTLET','newsletter.portlet.name','plugins/newsletter/CreatePortletNewsletter.jsp','plugins/newsletter/ModifyPortletNewsletter.jsp','fr.paris.lutece.plugins.newsletter.business.portlet.NewsLetterArchivePortletHome','newsletter','plugins/newsletter/DoCreatePortletNewsletter.jsp','/admin/portlet/script_create_portlet.html','','','plugins/newsletter/DoModifyPortletNewsletter.jsp','/admin/portlet/script_modify_portlet.html','/admin/plugins/newsletter/newsletter_sending_list.html','','archive');
INSERT INTO core_portlet_type (id_portlet_type,name,url_creation,url_update,home_class,plugin_name,url_docreate,create_script,create_specific,create_specific_form,url_domodify,modify_script,modify_specific,modify_specific_form,icon_name) VALUES ('NEWSLETTER_SUBSCRIPTION_PORTLET','newsletter.portlet.subscription.name','plugins/newsletter/CreateSubscriptionPortletNewsletter.jsp','plugins/newsletter/ModifySubscriptionPortletNewsletter.jsp','fr.paris.lutece.plugins.newsletter.business.portlet.NewsLetterSubscriptionPortletHome','newsletter','plugins/newsletter/DoCreateSubscriptionPortletNewsletter.jsp','/admin/portlet/script_create_portlet.html','','','plugins/newsletter/DoModifySubscriptionPortletNewsletter.jsp','/admin/portlet/script_modify_portlet.html','/admin/plugins/newsletter/newsletter_subscription_list.html','','news');

--
-- Dumping data for table core_user_right
--

INSERT INTO core_user_right (id_right,id_user) VALUES ('NEWSLETTER_TEMPLATE_MANAGEMENT',1);
INSERT INTO core_user_right (id_right,id_user) VALUES ('NEWSLETTER_MANAGEMENT',1);
INSERT INTO core_user_right (id_right,id_user) VALUES ('NEWSLETTER_MANAGEMENT',2);

--
-- Dumping data for table core_user_role
--

INSERT INTO core_user_role (role_key,id_user) VALUES ('newsletter_manager',1);
INSERT INTO core_user_role (role_key,id_user) VALUES ('newsletter_manager',2);

--
-- FreeMarker templates available for the newsletter portlets, registered in the core (Section Template Management feature).
-- They replace the XSL styles 1100 (Newsletter-Archives) and 1101 (Newsletter-Subscription) of the former versions.
--
-- changeset newsletter:init_core_newsletter.sql-rev1.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM core_portlet_template WHERE id_portlet_type IN ('NEWSLETTER_ARCHIVE_PORTLET', 'NEWSLETTER_SUBSCRIPTION_PORTLET')
INSERT INTO core_portlet_template (id_portlet_type, description, template_path) VALUES ('NEWSLETTER_ARCHIVE_PORTLET', 'Newsletter - Archives', 'skin/plugins/newsletter/portlet/newsletter_archive_portlet.html');
INSERT INTO core_portlet_template (id_portlet_type, description, template_path) VALUES ('NEWSLETTER_SUBSCRIPTION_PORTLET', 'Newsletter - Souscription', 'skin/plugins/newsletter/portlet/newsletter_subscription_portlet.html');

--
-- The sendings / newsletters selection is now the specific part of the core creation form (no more override of admin/portlet/create_portlet.html)
--
-- changeset newsletter:init_core_newsletter.sql-rev2.sql
-- preconditions onFail:MARK_RAN onError:WARN
UPDATE core_portlet_type SET create_specific = '/admin/plugins/newsletter/newsletter_sending_list.html' WHERE id_portlet_type = 'NEWSLETTER_ARCHIVE_PORTLET';
UPDATE core_portlet_type SET create_specific = '/admin/plugins/newsletter/newsletter_subscription_list.html' WHERE id_portlet_type = 'NEWSLETTER_SUBSCRIPTION_PORTLET';

-- changeset newsletter:init_core_newsletter.sql-rev3.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM core_admin_right WHERE id_right = 'NEWSLETTER_SUBSCRIBER_MANAGEMENT'
--
-- Feature of the management of the subscribers of all the newsletters
--
INSERT INTO core_admin_right (id_right,name,level_right,admin_url,description,is_updatable,plugin_name,id_feature_group,icon_url,documentation_url) VALUES ('NEWSLETTER_SUBSCRIBER_MANAGEMENT','newsletter.adminFeature.newsletter_subscriber_management.name',2,'jsp/admin/plugins/newsletter/ManageSubscribersGlobal.jsp','newsletter.adminFeature.newsletter_subscriber_management.description',0,'newsletter','USERS','ti ti-users', NULL);
INSERT INTO core_user_right (id_right,id_user) VALUES ('NEWSLETTER_SUBSCRIBER_MANAGEMENT',1);

--
-- The actions of the management of the subscribers of all the newsletters are granted by the RBAC resource type NEWSLETTER_SUBSCRIBER
--
-- changeset newsletter:init_core_newsletter.sql-rev4.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM core_admin_role_resource WHERE role_key = 'newsletter_manager' AND resource_type = 'NEWSLETTER_SUBSCRIBER'
INSERT INTO core_admin_role_resource (role_key,resource_type,resource_id,permission) VALUES ('newsletter_manager','NEWSLETTER_SUBSCRIBER','*','*');

--
-- Dashboard of the subscriptions awaiting confirmation, shown to the administrators managing the subscribers
--
-- changeset newsletter:init_core_newsletter.sql-rev5.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM core_dashboard WHERE dashboard_name = 'NEWSLETTER_PENDING_SUBSCRIPTIONS'
INSERT INTO core_dashboard(dashboard_name, dashboard_column, dashboard_order) VALUES('NEWSLETTER_PENDING_SUBSCRIPTIONS', 5, 2);
