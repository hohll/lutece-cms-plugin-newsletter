<%@ page errorPage="../../ErrorPage.jsp" %>

<%@page import="fr.paris.lutece.plugins.newsletter.web.portlet.NewsLetterSubscriptionPortletJspBean"%>

${ newsletterSubscriptionPortletJspBean.init( pageContext.request, NewsLetterSubscriptionPortletJspBean.RIGHT_MANAGE_ADMIN_SITE ) }
${ pageContext.response.sendRedirect( newsletterSubscriptionPortletJspBean.doCreate( pageContext.request ) ) }
