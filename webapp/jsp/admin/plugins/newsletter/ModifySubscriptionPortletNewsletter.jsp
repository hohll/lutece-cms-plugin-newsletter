<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:include page="../../PortletAdminHeader.jsp" />

<%@page import="fr.paris.lutece.plugins.newsletter.web.portlet.NewsLetterSubscriptionPortletJspBean"%>

${ newsletterSubscriptionPortletJspBean.init( pageContext.request, NewsLetterSubscriptionPortletJspBean.RIGHT_MANAGE_ADMIN_SITE ) }
${ newsletterSubscriptionPortletJspBean.getModify( pageContext.request ) }

<%@ include file="../../AdminFooter.jsp" %>
