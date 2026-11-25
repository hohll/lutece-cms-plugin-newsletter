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

import fr.paris.lutece.api.user.User;
import fr.paris.lutece.plugins.newsletter.business.NewsLetterTemplate;
import fr.paris.lutece.plugins.newsletter.business.NewsLetterTemplateHome;
import fr.paris.lutece.plugins.newsletter.service.NewsletterPlugin;
import fr.paris.lutece.plugins.newsletter.service.NewsletterService;
import fr.paris.lutece.plugins.newsletter.service.NewsletterTemplateResourceIdService;
import fr.paris.lutece.plugins.newsletter.service.topic.NewsletterTopicService;
import fr.paris.lutece.plugins.newsletter.util.NewsLetterConstants;
import fr.paris.lutece.portal.business.rbac.RBAC;
import fr.paris.lutece.portal.business.workgroup.AdminWorkgroupHome;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.rbac.RBACService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.workgroup.AdminWorkgroupService;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.portal.web.cdi.mvc.Models;
import fr.paris.lutece.portal.web.constants.Messages;
import fr.paris.lutece.util.ReferenceItem;
import fr.paris.lutece.util.ReferenceList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

/**
 * JspBean to manage newsletter templates. The files of the templates are not uploaded: they are chosen among the files of the folder of the
 * newsletter templates of the webapp, and their pictures among the files of the folder of the template pictures. A template cannot be
 * removed from the back office.
 */
@RequestScoped
@Named
@Controller( controllerJsp = NewsletterTemplateJspBean.CONTROLLER_JSP, controllerPath = NewsletterTemplateJspBean.CONTROLLER_PATH, right = NewsletterTemplateJspBean.RIGHT_NEWSLETTER_TEMPLATE_MANAGEMENT, securityTokenEnabled = true )
public class NewsletterTemplateJspBean extends MVCAdminJspBean
{
    /**
     * The right used for managing newsletter templates
     */
    public static final String RIGHT_NEWSLETTER_TEMPLATE_MANAGEMENT = "NEWSLETTER_TEMPLATE_MANAGEMENT";

    /**
     * The controller JSP
     */
    public static final String CONTROLLER_JSP = "ManageTemplates.jsp";

    /**
     * The controller path
     */
    public static final String CONTROLLER_PATH = "jsp/admin/plugins/newsletter/";

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = -2513112227429482685L;

    // TEMPLATES
    private static final String TEMPLATE_MANAGE_NEWSLETTER_TEMPLATE = "admin/plugins/newsletter/manage_templates.html";
    private static final String TEMPLATE_MODIFY_NEWSLETTER_TEMPLATE = "admin/plugins/newsletter/modify_newsletter_template.html";

    // VIEWS
    private static final String VIEW_MANAGE_TEMPLATES = "manageTemplates";
    private static final String VIEW_CREATE_TEMPLATE = "createTemplate";
    private static final String VIEW_MODIFY_TEMPLATE = "modifyTemplate";

    // ACTIONS
    private static final String ACTION_CREATE_TEMPLATE = VIEW_CREATE_TEMPLATE;
    private static final String ACTION_MODIFY_TEMPLATE = VIEW_MODIFY_TEMPLATE;

    // MARKS
    private static final String MARK_TEMPLATE = "template";
    private static final String MARK_TEMPLATE_TYPE = "template_type";
    private static final String MARK_NEWSLETTER_TEMPLATE_ALLOW_MODIFICATION = "newsletter_template_allow_modification";
    private static final String MARK_NEWSLETTER_TEMPLATE_WORKGROUP_DESCRIPTION = "newsletter_template_workgroup_description";
    private static final String MARK_NEWSLETTER_TEMPLATE_TYPE_LABEL = "newsletter_template_type_label";
    private static final String MARK_TEMPLATE_TAB = "template_tab";
    private static final String TEMPLATE_TAB_CREATE = "create";
    private static final String TEMPLATE_TAB_INFORMATION = "information";
    private static final String MARK_IMG_PATH = "img_path";
    private static final String MARK_TEMPLATES_LIST = "template_list";
    private static final String MARK_ALLOW_CREATION = "creation_allowed";
    private static final String MARK_WORKGROUP_LIST = "workgroup_list";
    private static final String MARK_TEMPLATE_FILE_LIST = "template_file_list";
    private static final String MARK_TEMPLATE_PICTURE_LIST = "template_picture_list";
    private static final String MARK_TEMPLATE_MISSING = "template_missing";
    private static final String MARK_AVAILABLE_PICTURES = "available_pictures";

    // MESSAGES
    private static final String MESSAGE_PAGE_TITLE_MANAGE_TEMPLATES = "newsletter.manage_templates.pageTitle";
    private static final String MESSAGE_PAGE_TITLE_ADD_TEMPLATE = "newsletter.add_newsletter_template.pageTitle";
    private static final String MESSAGE_PAGE_TITLE_MODIFY_TEMPLATE = "newsletter.modify_newsletter_template.pageTitle";
    private static final String MESSAGE_NEWSLETTER_TEMPLATE = "newsletter.template.type.newsletter.label";
    private static final String MESSAGE_TEMPLATE_NOT_FOUND = "newsletter.message.templateNotFound";
    private static final String MESSAGE_NO_PICTURE = "newsletter.add_newsletter_template.noPicture";
    private static final String INFO_TEMPLATE_CREATED = "newsletter.info.templateCreated";
    private static final String INFO_TEMPLATE_UPDATED = "newsletter.info.templateUpdated";

    // PARAMETERS
    private static final String PARAMETER_NEWSLETTER_TEMPLATE_ID = "newsletter_template_id";
    private static final String PARAMETER_NEWSLETTER_TEMPLATE_NAME = "newsletter_template_name";
    private static final String PARAMETER_NEWSLETTER_TEMPLATE_TYPE = "newsletter_template_type";
    private static final String PARAMETER_NEWSLETTER_TEMPLATE_WORKGROUP = "newsletter_template_workgroup";
    private static final String PARAMETER_TEMPLATE_FILE = "newsletter_template_file";
    private static final String PARAMETER_TEMPLATE_PICTURE = "newsletter_template_picture";
    private static final String PARAMETER_TEMPLATE_SECTION = "newsletter_template_section";

    private static final int DEFAULT_SECTION_NUMBER = 1;

    @Inject
    private transient NewsletterTopicService _newsletterTopicService;
    @Inject
    private transient NewsletterService _newsletterService;

    /**
     * Builds the newsletter's templates management page
     * 
     * @param request
     *            The HTTP request
     * @param model
     *            the model of the page
     * @return the html code for newsletter's templates management page (list of templates + available actions)
     */
    @View( value = VIEW_MANAGE_TEMPLATES, defaultView = true )
    public String getManageTemplates( HttpServletRequest request, Models model )
    {
        Collection<NewsLetterTemplate> listTemplates = AdminWorkgroupService.getAuthorizedCollection( NewsLetterTemplateHome.getTemplatesList( getPlugin( ) ),
                (User) getUser( ) );
        Map<String, String> mapTemplateTypeLabels = buildTemplateTypeList( ).toMap( );
        List<Map<String, Object>> listNewsletterTemplateDisplay = new ArrayList<>( );

        for ( NewsLetterTemplate newsletterTemplate : listTemplates )
        {
            Map<String, Object> newsletterTemplateDisplay = new HashMap<>( );
            newsletterTemplateDisplay.put( MARK_TEMPLATE, newsletterTemplate );
            newsletterTemplateDisplay.put( MARK_NEWSLETTER_TEMPLATE_TYPE_LABEL,
                    mapTemplateTypeLabels.getOrDefault( newsletterTemplate.getTopicType( ), newsletterTemplate.getTopicType( ) ) );
            newsletterTemplateDisplay.put( MARK_NEWSLETTER_TEMPLATE_ALLOW_MODIFICATION,
                    RBACService.isAuthorized( newsletterTemplate, NewsletterTemplateResourceIdService.PERMISSION_MODIFY, (User) getUser( ) ) );
            newsletterTemplateDisplay.put( MARK_NEWSLETTER_TEMPLATE_WORKGROUP_DESCRIPTION, getWorkgroupDescription( newsletterTemplate ) );
            newsletterTemplateDisplay.put( MARK_TEMPLATE_MISSING, !_newsletterService.isTemplateAvailable( newsletterTemplate ) );
            listNewsletterTemplateDisplay.add( newsletterTemplateDisplay );
        }

        model.put( MARK_TEMPLATES_LIST, listNewsletterTemplateDisplay );
        model.put( MARK_ALLOW_CREATION, isNewsletterTemplateCreationAllowed( ) );
        model.put( MARK_IMG_PATH, _newsletterService.getImageFolderPath( AppPathService.getBaseUrl( request ) ) );
        model.put( MARK_AVAILABLE_PICTURES, new ArrayList<>( _newsletterService.getTemplatePictureList( ).toMap( ).keySet( ) ) );

        return getPage( MESSAGE_PAGE_TITLE_MANAGE_TEMPLATES, TEMPLATE_MANAGE_NEWSLETTER_TEMPLATE );
    }

    /**
     * Builds the newsletter's templates creation page
     * 
     * @param request
     *            The HTTP request
     * @param model
     *            the model of the page
     * @return the html code for newsletter's templates creation page
     */
    @View( VIEW_CREATE_TEMPLATE )
    public String getCreateNewsLetterTemplate( HttpServletRequest request, Models model )
    {
        if ( !isNewsletterTemplateCreationAllowed( ) )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_STOP ) );
        }

        model.put( MARK_TEMPLATE_TAB, TEMPLATE_TAB_CREATE );
        fillFormModel( model, request );

        return getPage( MESSAGE_PAGE_TITLE_ADD_TEMPLATE, TEMPLATE_MODIFY_NEWSLETTER_TEMPLATE );
    }

    /**
     * Processes the creation form of a new newsletter template
     * 
     * @param request
     *            the http request
     * @param model
     *            the model of the page
     * @return the creation page when a field is invalid, the redirection to the templates management page otherwise
     */
    @Action( ACTION_CREATE_TEMPLATE )
    public String doCreateNewsletterTemplate( HttpServletRequest request, Models model )
    {
        if ( !isNewsletterTemplateCreationAllowed( ) )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_STOP ) );
        }

        NewsLetterTemplate newsletterTemplate = new NewsLetterTemplate( );
        populate( newsletterTemplate, request );

        if ( !validate( newsletterTemplate ) )
        {
            addError( Messages.MANDATORY_FIELDS, getLocale( ) );
            model.put( MARK_TEMPLATE, newsletterTemplate );

            return getCreateNewsLetterTemplate( request, model );
        }

        NewsLetterTemplateHome.create( newsletterTemplate, getPlugin( ) );
        addInfo( INFO_TEMPLATE_CREATED, getLocale( ) );

        return redirectView( request, VIEW_MANAGE_TEMPLATES );
    }

    /**
     * Builds the newsletter's templates modification page
     * 
     * @param request
     *            The HTTP request
     * @param model
     *            the model of the page
     * @return the html code for newsletter's templates modification page
     */
    @View( VIEW_MODIFY_TEMPLATE )
    public String getModifyNewsLetterTemplate( HttpServletRequest request, Models model )
    {
        NewsLetterTemplate newsletterTemplate = findTemplate( request );
        String strErrorUrl = checkTemplate( request, newsletterTemplate, NewsletterTemplateResourceIdService.PERMISSION_MODIFY );

        if ( strErrorUrl != null )
        {
            return redirect( request, strErrorUrl );
        }

        if ( model.get( MARK_TEMPLATE ) == null )
        {
            model.put( MARK_TEMPLATE, newsletterTemplate );
        }

        NewsLetterTemplate templateDisplayed = (NewsLetterTemplate) model.get( MARK_TEMPLATE );
        model.put( MARK_TEMPLATE_TAB, TEMPLATE_TAB_INFORMATION );
        fillFormModel( model, request );
        model.put( MARK_TEMPLATE_MISSING, StringUtils.isNotEmpty( templateDisplayed.getTemplateName( ) ) && !_newsletterService.isTemplateAvailable( templateDisplayed ) );

        return getPage( MESSAGE_PAGE_TITLE_MODIFY_TEMPLATE, TEMPLATE_MODIFY_NEWSLETTER_TEMPLATE );
    }

    /**
     * Processes the modification form of a newsletter template
     * 
     * @param request
     *            the http request
     * @param model
     *            the model of the page
     * @return the modification page when a field is invalid, the redirection to the templates management page otherwise
     */
    @Action( ACTION_MODIFY_TEMPLATE )
    public String doModifyNewsletterTemplate( HttpServletRequest request, Models model )
    {
        NewsLetterTemplate newsletterTemplate = findTemplate( request );
        String strErrorUrl = checkTemplate( request, newsletterTemplate, NewsletterTemplateResourceIdService.PERMISSION_MODIFY );

        if ( strErrorUrl != null )
        {
            return redirect( request, strErrorUrl );
        }

        int nOldSectionNumber = newsletterTemplate.getSectionNumber( );
        populate( newsletterTemplate, request );

        if ( !validate( newsletterTemplate ) )
        {
            addError( Messages.MANDATORY_FIELDS, getLocale( ) );
            model.put( MARK_TEMPLATE, newsletterTemplate );

            return getModifyNewsLetterTemplate( request, model );
        }

        _newsletterService.modifySectionNumber( nOldSectionNumber, newsletterTemplate.getSectionNumber( ), newsletterTemplate.getId( ) );
        NewsLetterTemplateHome.update( newsletterTemplate, getPlugin( ) );
        addInfo( INFO_TEMPLATE_UPDATED, getLocale( ) );

        return redirectView( request, VIEW_MANAGE_TEMPLATES );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Plugin getPlugin( )
    {
        return PluginService.getPlugin( NewsletterPlugin.PLUGIN_NAME );
    }

    /**
     * Builds the list of the template types: the newsletter type followed by the topic types
     * 
     * @return the list of the template types
     */
    private ReferenceList buildTemplateTypeList( )
    {
        ReferenceList refTemplateTypeList = _newsletterTopicService.getNewsletterTopicTypeRefList( getLocale( ) );
        ReferenceItem refItemTemplate = new ReferenceItem( );
        refItemTemplate.setCode( NewsLetterTemplate.RESOURCE_TYPE );
        refItemTemplate.setName( I18nService.getLocalizedString( MESSAGE_NEWSLETTER_TEMPLATE, getLocale( ) ) );
        refTemplateTypeList.add( 0, refItemTemplate );

        return refTemplateTypeList;
    }

    /**
     * Returns the description of the workgroup of a template
     * 
     * @param newsletterTemplate
     *            the template
     * @return the description of its workgroup, or the label of all the groups
     */
    private String getWorkgroupDescription( NewsLetterTemplate newsletterTemplate )
    {
        if ( NewsLetterConstants.ALL_GROUPS.equals( newsletterTemplate.getWorkgroup( ) ) )
        {
            return I18nService.getLocalizedString( NewsLetterConstants.PROPERTY_LABEL_ALL_GROUPS, getLocale( ) );
        }

        return AdminWorkgroupHome.findByPrimaryKey( newsletterTemplate.getWorkgroup( ) ).getDescription( );
    }

    /**
     * Puts in the model the lists of the creation and modification form: template types, workgroups, files of the templates folder and pictures
     * of the pictures folder
     * 
     * @param model
     *            the model of the page
     * @param request
     *            the HTTP request
     */
    private void fillFormModel( Models model, HttpServletRequest request )
    {
        ReferenceList listPictures = new ReferenceList( );
        listPictures.addItem( StringUtils.EMPTY, I18nService.getLocalizedString( MESSAGE_NO_PICTURE, getLocale( ) ) );
        listPictures.addAll( _newsletterService.getTemplatePictureList( ) );

        model.put( MARK_TEMPLATE_TYPE, buildTemplateTypeList( ) );
        model.put( MARK_WORKGROUP_LIST, AdminWorkgroupService.getUserWorkgroups( getUser( ), getLocale( ) ) );
        model.put( MARK_TEMPLATE_FILE_LIST, _newsletterService.getTemplateFileList( ) );
        model.put( MARK_TEMPLATE_PICTURE_LIST, listPictures );
        model.put( MARK_IMG_PATH, _newsletterService.getImageFolderPath( AppPathService.getBaseUrl( request ) ) );
        model.put( MARK_AVAILABLE_PICTURES, new ArrayList<>( _newsletterService.getTemplatePictureList( ).toMap( ).keySet( ) ) );
    }

    /**
     * Fills a newsletter template with the parameters of the creation or modification form
     * 
     * @param newsletterTemplate
     *            the template to fill
     * @param request
     *            the HTTP request
     */
    private void populate( NewsLetterTemplate newsletterTemplate, HttpServletRequest request )
    {
        String strSectionNumber = request.getParameter( PARAMETER_TEMPLATE_SECTION );

        newsletterTemplate.setTopicType( request.getParameter( PARAMETER_NEWSLETTER_TEMPLATE_TYPE ) );
        newsletterTemplate.setDescription( StringUtils.trim( request.getParameter( PARAMETER_NEWSLETTER_TEMPLATE_NAME ) ) );
        newsletterTemplate.setWorkgroup( request.getParameter( PARAMETER_NEWSLETTER_TEMPLATE_WORKGROUP ) );
        newsletterTemplate.setTemplateName( request.getParameter( PARAMETER_TEMPLATE_FILE ) );
        newsletterTemplate.setPictureKey( request.getParameter( PARAMETER_TEMPLATE_PICTURE ) );
        newsletterTemplate.setSectionNumber(
                StringUtils.isNumeric( strSectionNumber ) ? Math.max( DEFAULT_SECTION_NUMBER, Integer.parseInt( strSectionNumber ) ) : DEFAULT_SECTION_NUMBER );
    }

    /**
     * Checks a newsletter template filled from the form: the mandatory fields are given, the file is one of the files of the templates folder and
     * the picture, when given, one of the pictures of the pictures folder
     * 
     * @param newsletterTemplate
     *            the template to check
     * @return true if the template is valid, false otherwise
     */
    private boolean validate( NewsLetterTemplate newsletterTemplate )
    {
        return StringUtils.isNoneEmpty( newsletterTemplate.getTopicType( ), newsletterTemplate.getDescription( ), newsletterTemplate.getWorkgroup( ) )
                && isTemplateFileAvailable( newsletterTemplate.getTemplateName( ) )
                && ( StringUtils.isEmpty( newsletterTemplate.getPictureKey( ) )
                        || _newsletterService.getTemplatePictureList( ).toMap( ).containsKey( newsletterTemplate.getPictureKey( ) ) );
    }

    /**
     * Checks if a file is one of the files of the templates folder
     * 
     * @param strFileName
     *            the name of the file
     * @return true if the file is in the templates folder, false otherwise
     */
    private boolean isTemplateFileAvailable( String strFileName )
    {
        return StringUtils.isNotEmpty( strFileName ) && _newsletterService.getTemplateFileList( ).toMap( ).containsKey( strFileName );
    }

    /**
     * Returns the newsletter template whose identifier is in the request
     * 
     * @param request
     *            the HTTP request
     * @return the template, or null when the identifier is missing, matches no template or a template outside the workgroups of the user
     */
    private NewsLetterTemplate findTemplate( HttpServletRequest request )
    {
        String strTemplateId = request.getParameter( PARAMETER_NEWSLETTER_TEMPLATE_ID );

        if ( !StringUtils.isNumeric( strTemplateId ) )
        {
            return null;
        }

        NewsLetterTemplate newsletterTemplate = NewsLetterTemplateHome.findByPrimaryKey( Integer.parseInt( strTemplateId ), getPlugin( ) );

        return ( newsletterTemplate == null || newsletterTemplate.getId( ) == 0
                || !AdminWorkgroupService.isAuthorized( newsletterTemplate, (User) getUser( ) ) ) ? null : newsletterTemplate;
    }

    /**
     * Checks that a template exists and that the user holds its workgroup and a permission on it
     * 
     * @param request
     *            the HTTP request
     * @param newsletterTemplate
     *            the template, may be null
     * @param strPermission
     *            the RBAC permission
     * @return the URL of the error message, null when everything is fine
     */
    private String checkTemplate( HttpServletRequest request, NewsLetterTemplate newsletterTemplate, String strPermission )
    {
        if ( newsletterTemplate == null )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_TEMPLATE_NOT_FOUND, AdminMessage.TYPE_STOP );
        }

        if ( !AdminWorkgroupService.isAuthorized( newsletterTemplate, (User) getUser( ) ) || !RBACService.isAuthorized( NewsLetterTemplate.RESOURCE_TYPE,
                Integer.toString( newsletterTemplate.getId( ) ), strPermission, (User) getUser( ) ) )
        {
            return AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_STOP );
        }

        return null;
    }

    /**
     * Check if user is authorized to create a newsletter template
     * 
     * @return true if creation is authorized, false otherwise
     */
    private boolean isNewsletterTemplateCreationAllowed( )
    {
        User user = (User) getUser( );

        if ( RBACService.isAuthorized( NewsLetterTemplate.RESOURCE_TYPE, RBAC.WILDCARD_RESOURCES_ID, NewsletterTemplateResourceIdService.PERMISSION_CREATE,
                user ) )
        {
            return true;
        }

        for ( NewsLetterTemplate newsletterTemplate : AdminWorkgroupService.getAuthorizedCollection( NewsLetterTemplateHome.getTemplatesList( getPlugin( ) ),
                user ) )
        {
            if ( RBACService.isAuthorized( newsletterTemplate, NewsletterTemplateResourceIdService.PERMISSION_CREATE, user ) )
            {
                return true;
            }
        }

        return false;
    }
}
