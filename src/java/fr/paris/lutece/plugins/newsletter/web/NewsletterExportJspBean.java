/*
 * Copyright (c) 2002-2021, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.newsletter.web;

import java.nio.charset.StandardCharsets;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import fr.paris.lutece.api.user.User;
import fr.paris.lutece.plugins.newsletter.business.NewsLetter;
import fr.paris.lutece.plugins.newsletter.business.NewsLetterHome;
import fr.paris.lutece.plugins.newsletter.service.NewsletterPlugin;
import fr.paris.lutece.plugins.newsletter.service.NewsletterResourceIdService;
import fr.paris.lutece.plugins.newsletter.service.NewsletterService;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.rbac.RBACService;
import fr.paris.lutece.portal.service.workgroup.AdminWorkgroupService;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.portal.web.constants.Messages;

/**
 * The CSV export of the subscribers of a newsletter. The file is the whole response: its JSP includes neither the admin header nor the footer, so
 * this controller holds the export only, a read, and no action that would reach the application without the security token of its pages.
 */
@RequestScoped
@Named( "newsletterExportJspBean" )
@Controller( controllerJsp = NewsletterExportJspBean.CONTROLLER_JSP, controllerPath = NewsletterJspBean.CONTROLLER_PATH, right = NewsletterJspBean.RIGHT_NEWSLETTER_MANAGEMENT, securityTokenEnabled = true )
public class NewsletterExportJspBean extends MVCAdminJspBean
{
    /**
     * The controller JSP
     */
    public static final String CONTROLLER_JSP = "ExportSubscribers.jsp";

    private static final long serialVersionUID = 2961546273710587124L;

    private static final String VIEW_EXPORT_CSV = "exportCsv";
    private static final String PARAMETER_NEWSLETTER_ID = "newsletter_id";
    private static final String CONSTANT_CSV_FILE_EXTENSION = ".csv";
    private static final String CONSTANT_CSV_CONTENT_TYPE = "application/csv; charset=UTF-8";
    private static final String MESSAGE_NO_SUBSCRIBER_EXPORT = "newsletter.message.noSubscriberExport";

    @Inject
    private NewsletterService _newsletterService;

    /**
     * Exports the list of the subscribers of a newsletter in a csv file format. The file is written through the response writer, the one the JSP
     * releases at its end: the output stream would make it fail.
     *
     * @param request
     *            The Http Request
     * @return null once the file is sent, or the redirection to the error message
     */
    @View( value = VIEW_EXPORT_CSV, defaultView = true )
    public String getExportCsv( HttpServletRequest request )
    {
        String strNewsletterId = request.getParameter( PARAMETER_NEWSLETTER_ID );
        NewsLetter newsletter = StringUtils.isNumeric( strNewsletterId ) ? NewsLetterHome.findByPrimaryKey( Integer.parseInt( strNewsletterId ), getPlugin( ) )
                : null;

        if ( newsletter == null || !AdminWorkgroupService.isAuthorized( newsletter, (User) getUser( ) ) || !RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE,
                Integer.toString( newsletter.getId( ) ), NewsletterResourceIdService.PERMISSION_EXPORT_SUBSCRIBERS, (User) getUser( ) ) )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_ERROR ) );
        }

        if ( NewsLetterHome.findNbrActiveSubscribers( newsletter.getId( ), getPlugin( ) ) == 0 )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, MESSAGE_NO_SUBSCRIBER_EXPORT, AdminMessage.TYPE_STOP ) );
        }

        download( new String( _newsletterService.getSubscribersCsvExport( newsletter.getId( ) ), StandardCharsets.UTF_8 ),
                newsletter.getName( ) + CONSTANT_CSV_FILE_EXTENSION, CONSTANT_CSV_CONTENT_TYPE );

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Plugin getPlugin( )
    {
        return PluginService.getPlugin( NewsletterPlugin.PLUGIN_NAME );
    }
}
