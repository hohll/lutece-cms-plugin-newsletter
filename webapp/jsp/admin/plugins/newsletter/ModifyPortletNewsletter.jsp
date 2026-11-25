<%@ page errorPage="../../ErrorPage.jsp" %>
<jsp:include page="../../PortletAdminHeader.jsp" />

<%@page import="fr.paris.lutece.plugins.newsletter.web.portlet.NewsLetterArchivePortletJspBean"%>

${ newsletterArchivePortletJspBean.init( pageContext.request, NewsLetterArchivePortletJspBean.RIGHT_MANAGE_ADMIN_SITE ) }
${ newsletterArchivePortletJspBean.getModify( pageContext.request ) }

<%@ include file="../../AdminFooter.jsp" %>
