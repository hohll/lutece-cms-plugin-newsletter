<%@ page errorPage="../../ErrorPage.jsp" %>

<%@page import="fr.paris.lutece.plugins.newsletter.web.portlet.NewsLetterArchivePortletJspBean"%>

${ newsletterArchivePortletJspBean.init( pageContext.request, NewsLetterArchivePortletJspBean.RIGHT_MANAGE_ADMIN_SITE ) }
${ pageContext.response.sendRedirect( newsletterArchivePortletJspBean.doModify( pageContext.request ) ) }
