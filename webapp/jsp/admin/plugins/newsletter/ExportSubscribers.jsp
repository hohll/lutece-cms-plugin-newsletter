<%@ page errorPage="../../ErrorPage.jsp" trimDirectiveWhitespaces="true" %>
<%-- The CSV export of the subscribers (view exportCsv): the file is the whole response, so no admin header nor footer, and no character after the expression --%>
${ newsletterExportJspBean.processController( pageContext.request , pageContext.response ) }