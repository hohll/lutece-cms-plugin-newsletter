<%@ page errorPage="../../ErrorPage.jsp" trimDirectiveWhitespaces="true" %>
<%@ page import="fr.paris.lutece.plugins.newsletter.web.NewsletterJspBean" %>
<jsp:useBean id="newsletterJspBean" scope="session" class="fr.paris.lutece.plugins.newsletter.web.NewsletterJspBean" />

${ newsletterJspBean.init( pageContext.request, NewsletterJspBean.RIGHT_NEWSLETTER_MANAGEMENT ) }
<%
    String strResult = newsletterJspBean.doExportCsv( request, response );
    if ( !response.isCommitted( ) )
    {
        response.sendRedirect( strResult );
    }
    else
    {
        // the CSV went through the response output stream: the JSP writer must not touch the response any more
        out.clear( );
        out = pageContext.pushBody( );
    }
%>
