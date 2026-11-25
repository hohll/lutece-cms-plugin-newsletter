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
package fr.paris.lutece.plugins.newsletter.service;

import fr.paris.lutece.plugins.newsletter.business.NewsLetter;
import fr.paris.lutece.plugins.newsletter.business.NewsLetterHome;
import fr.paris.lutece.plugins.newsletter.business.NewsLetterTemplate;
import fr.paris.lutece.plugins.newsletter.business.Subscriber;
import fr.paris.lutece.plugins.newsletter.business.SubscriberHome;
import fr.paris.lutece.plugins.newsletter.business.topic.NewsletterTopic;
import fr.paris.lutece.plugins.newsletter.business.topic.NewsletterTopicHome;
import fr.paris.lutece.plugins.newsletter.service.topic.INewsletterTopicService;
import fr.paris.lutece.plugins.newsletter.service.topic.NewsletterTopicService;
import fr.paris.lutece.plugins.newsletter.util.NewsLetterConstants;
import fr.paris.lutece.plugins.newsletter.util.NewsletterUtils;
import fr.paris.lutece.plugins.resource.loader.ResourceNotFoundException;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.mail.MailService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.template.HtmlMarkup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.CryptoService;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.file.FileUtil;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.mail.UrlAttachment;
import fr.paris.lutece.util.string.StringUtil;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.text.StringEscapeUtils;

import com.opencsv.CSVWriter;

/**
 * The newsletter service
 *
 */
@ApplicationScoped
public class NewsletterService implements Serializable
{
    /**
     * Name of the bean of this service
     */
    public static final String BEAN_NAME = "newsletter.newsletterService";

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 1644159439192572037L;

    private static final String MESSAGE_PREVIEW_EMPTY_TOPIC = "newsletter.manage_topics.previewEmptyTopic";
    private static final String TEMPLATE_PREVIEW_SECTION = "<div data-newsletter-section=\"{0}\">{1}</div>";
    private static final String TEMPLATE_PREVIEW_TOPIC = "<div data-newsletter-topic=\"{0}\">{1}</div>";
    private static final String TEMPLATE_PREVIEW_EMPTY_TOPIC = "<div style=\"margin:6px 0;padding:8px 10px;border-radius:4px;background:#fdf3e3;color:#7a5410;font:13px sans-serif\">{0}</div>";

    // PROPERTIES

    @Inject
    @ConfigProperty( name = "newsletter.absolute.mail.url", defaultValue = "true" )
    private String _strAbsoluteUrlMail;
    @Inject
    @ConfigProperty( name = NewsLetterConstants.PROPERTY_MAIL_MULTIPART, defaultValue = "true" )
    private String _strMailMultipart;
    @Inject
    @ConfigProperty( name = NewsLetterConstants.PROPERTY_PATH_FILE_NEWSLETTER_TEMPLATE, defaultValue = "/skin/plugins/newsletter/templates" )
    private String _strPathFileNewsletterTemplate;
    @Inject
    @ConfigProperty( name = "newsletter.path.image.newsletter.template", defaultValue = "/themes/admin/shared/plugins/newsletter/images/newsletter_templates" )
    private String _strPathImageNewsletterTemplate;
    @Inject
    @ConfigProperty( name = "newsletter.nosecured.img.option", defaultValue = "false" )
    private boolean _bNoSecuredImgOption;
    @Inject
    @ConfigProperty( name = "newsletter.nosecured.img.folder.name", defaultValue = "unsecuredimg" )
    private String _strNoSecuredImgFolder;
    @Inject
    @ConfigProperty( name = "newsletter.unsubscribe.key.encryptionAlgorithm", defaultValue = "SHA-256" )
    private String _strUnsubscribeKeyEncryptionAlgorithm;
    @Inject
    @ConfigProperty( name = "path.templates", defaultValue = "/WEB-INF/templates" )
    private String _strPathTemplates;
    @Inject
    @ConfigProperty( name = "newsletter.nosecured.webapp.path" )
    private Optional<String> _strNoSecuredWebappPath;
    @Inject
    @ConfigProperty( name = "lutece.prod.url" )
    private Optional<String> _strProdUrl;
    @Inject
    @ConfigProperty( name = "newsletter.css.files" )
    private Optional<String> _strCssFiles;
    @Inject
    private transient NewsletterTopicService _newsletterTopicService;

    @Inject
    private transient Instance<INewsletterTopicService> _topicServices;

    /**
     * Send the newsletter to a list of subscribers
     * 
     * @param newsletter
     *            The newsletter to send
     * @param strObject
     *            The email object
     * @param strBaseUrl
     *            The baseUrl (can be prod url)
     * @param templateNewsletter
     *            The generated template
     * @param listSubscribers
     *            The list of subscribers (date and id can be null, only email is used)
     */
    public void sendMail( NewsLetter newsletter, String strObject, String strBaseUrl, HtmlTemplate templateNewsletter, Collection<Subscriber> listSubscribers )
    {
        List<UrlAttachment> urlAttachments = null;
        HtmlTemplate templateNewsletterToUse = templateNewsletter;
        if ( isMhtmlActivated( ) )
        {
            // we use absolute urls if there is no preproduction process
            boolean useAbsoluteUrl = isAbsoluteUrl( );
            String strTemplate = templateNewsletterToUse.getHtml( );
            strTemplate = StringUtil.substitute( strTemplate, strBaseUrl, NewsLetterConstants.WEBAPP_PATH_FOR_LINKSERVICE );
            urlAttachments = MailService.getUrlAttachmentList( strTemplate, strBaseUrl, useAbsoluteUrl );

            // all images, css urls are relative
            if ( !useAbsoluteUrl )
            {
                templateNewsletterToUse.substitute( strBaseUrl, strBaseUrl.replaceFirst( "https?://[^/]+/", "/" ) );
            }
            else
            {
                String strContent = NewsletterUtils.rewriteUrls( templateNewsletterToUse.getHtml( ), strBaseUrl );
                templateNewsletterToUse = new HtmlTemplate( strContent );
            }
        }

        for ( Subscriber subscriber : listSubscribers )
        {
            HtmlTemplate t = new HtmlTemplate( templateNewsletterToUse );

            t.substitute( NewsLetterConstants.MARK_SUBSCRIBER_EMAIL_EACH, subscriber.getEmail( ) );
            if ( Boolean.parseBoolean( newsletter.getUnsubscribe( ) ) )
            {
                String strUnsubscribeKey = getUnsubscriptionKey( subscriber.getEmail( ) );
                t.substitute( NewsLetterConstants.MARK_UNSUBSCRIBE_KEY_EACH, strUnsubscribeKey );
            }

            String strNewsLetterCode = t.getHtml( );

            if ( ( urlAttachments == null ) || ( urlAttachments.size( ) == 0 ) )
            {
                MailService.sendMailHtml( subscriber.getEmail( ), newsletter.getNewsletterSenderName( ), newsletter.getNewsletterSenderMail( ), strObject,
                        strNewsLetterCode );
            }
            else
            {
                MailService.sendMailMultipartHtml( subscriber.getEmail( ), newsletter.getNewsletterSenderName( ), newsletter.getNewsletterSenderMail( ),
                        strObject, strNewsLetterCode, urlAttachments );
            }
        }
    }

    /**
     * Check the property in property file to know if url must be absolutes or relatives
     * 
     * @return true if absolute or false else
     */
    public boolean isAbsoluteUrl( )
    {
        boolean useAbsoluteUrl = false;
        String strUseAbsoluteUrl = _strAbsoluteUrlMail;

        if ( ( strUseAbsoluteUrl != null ) && strUseAbsoluteUrl.equalsIgnoreCase( Boolean.TRUE.toString( ) ) )
        {
            useAbsoluteUrl = true;
        }

        return useAbsoluteUrl;
    }

    /**
     * Determine if mails must be sent in MHTML
     * 
     * @return true whether MHTML is needed
     */
    public boolean isMhtmlActivated( )
    {
        String strProperty = _strMailMultipart;
        return ( strProperty != null ) && Boolean.valueOf( strProperty ).booleanValue( );
    }

    /**
     * Fetches the list of subscribers on a specific newsletter
     * 
     * @param nNewsletterId
     *            The id of the newsletter
     * @return The byte representation of the list of subscribers
     */
    public byte [ ] getSubscribersCsvExport( int nNewsletterId )
    {
        byte [ ] byteSubscribersList = null;

        try
        {
            ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream( );
            CSVWriter writer = new CSVWriter( new BufferedWriter( new OutputStreamWriter( byteArrayStream, "UTF-8" ) ) );
            Collection<Subscriber> listSubscriber = SubscriberHome.findSubscribers( nNewsletterId, getPlugin( ) );

            for ( Subscriber subscriber : listSubscriber )
            {
                String [ ] arraySubscriber = new String [ 3];
                arraySubscriber [0] = Integer.toString( subscriber.getId( ) );
                arraySubscriber [1] = subscriber.getEmail( );
                arraySubscriber [2] = subscriber.getDateSubscription( ).toString( );
                writer.writeNext( arraySubscriber );
            }

            writer.close( );
            byteSubscribersList = byteArrayStream.toByteArray( );
        }
        catch( UnsupportedEncodingException e )
        {
            AppLogService.error( e );
        }
        catch( IOException e )
        {
            AppLogService.error( e );
        }

        return byteSubscribersList;
    }

    /**
     * Remove a known suscriber from a newsletter
     * 
     * @param subscriber
     *            the subscriber to remove
     * @param nNewsletterId
     *            the newsletter id from which to remove the subscriber
     * @param plugin
     *            The plugin object
     */
    public void removeSubscriberFromNewsletter( Subscriber subscriber, int nNewsletterId, Plugin plugin )
    {
        /* checks newsletter exist in database */
        NewsLetter newsletter = NewsLetterHome.findByPrimaryKey( nNewsletterId, plugin );

        if ( ( subscriber != null ) && ( newsletter != null ) )
        {
            int nSubscriberId = subscriber.getId( );

            /* checks if the subscriber identified is registered */
            if ( NewsLetterHome.findRegistration( nNewsletterId, nSubscriberId, plugin ) )
            {
                /* unregistration */
                NewsLetterHome.removeSubscriber( nNewsletterId, nSubscriberId, plugin );
            }

            /*
             * if the subscriber is not registered to an other newsletter, his account is deleted
             */
            if ( SubscriberHome.findNewsLetters( nSubscriberId, plugin ) == 0 )
            {
                SubscriberHome.remove( nSubscriberId, plugin );
            }
        }
    }

    /**
     * Generate the html code of the newsletter according to the document and newsletter templates
     * 
     * @param newsletter
     *            the newsletter to generate the HTML of
     * @param nTemplateNewsLetterId
     *            the newsletter template id
     * @param strBaseUrl
     *            The base url of the portal
     * @param user
     *            the current user
     * @param locale
     *            The locale
     * @return the html code for the newsletter content of null if no template available
     */
    public String generateNewsletterHtmlCode( NewsLetter newsletter, int nTemplateNewsLetterId, String strBaseUrl, AdminUser user, Locale locale )
    {
        return buildNewsletterHtmlCode( newsletter, nTemplateNewsLetterId, strBaseUrl, locale,
                topic -> getNewsletterTopicService( ).getTopicContent( topic, user, locale ), false );
    }

    /**
     * Generate the html code of the preview of a newsletter, shown in the back office while its content is configured. Each section and each topic
     * is wrapped in an element carrying its number or its id (data-newsletter-section, data-newsletter-topic), so that the page can outline and
     * highlight them, and a topic without content is replaced by a notice. This html must never be sent.
     *
     * @param newsletter
     *            The newsletter
     * @param mapTopicContents
     *            The content already generated for each topic of the newsletter, by topic id
     * @param strBaseUrl
     *            The base url of the portal
     * @param locale
     *            The locale
     * @return The html code of the preview, or null if the template of the newsletter is not available
     */
    public String generateNewsletterPreviewHtmlCode( NewsLetter newsletter, Map<Integer, String> mapTopicContents, String strBaseUrl, Locale locale )
    {
        return buildNewsletterHtmlCode( newsletter, newsletter.getNewsLetterTemplateId( ), strBaseUrl, locale, topic -> {
            String strContent = mapTopicContents.get( topic.getId( ) );
            if ( StringUtils.isBlank( strContent ) )
            {
                strContent = MessageFormat.format( TEMPLATE_PREVIEW_EMPTY_TOPIC, I18nService.getLocalizedString( MESSAGE_PREVIEW_EMPTY_TOPIC,
                        new Object [ ] { StringEscapeUtils.escapeHtml4( StringUtils.defaultString( topic.getTitle( ) ) ) }, locale ) );
            }
            return MessageFormat.format( TEMPLATE_PREVIEW_TOPIC, Integer.toString( topic.getId( ) ), strContent );
        }, true );
    }

    /**
     * Generate the html code of a newsletter from its template, the content of each section being the content of its topics in their order
     *
     * @param newsletter
     *            The newsletter
     * @param nTemplateNewsLetterId
     *            The id of the template of the newsletter
     * @param strBaseUrl
     *            The base url of the portal
     * @param locale
     *            The locale
     * @param topicContent
     *            Gives the html content of a topic
     * @param bPreview
     *            True to wrap each section in an element carrying its number
     * @return The html code, or null if the template is not available
     */
    private String buildNewsletterHtmlCode( NewsLetter newsletter, int nTemplateNewsLetterId, String strBaseUrl, Locale locale,
            Function<NewsletterTopic, String> topicContent, boolean bPreview )
    {
        String strTemplatePath = NewsletterUtils.getHtmlTemplatePath( nTemplateNewsLetterId, getPlugin( ) );

        if ( strTemplatePath == null )
        {
            return null;
        }

        List<NewsletterTopic> listTopics = NewsletterTopicHome.findAllByIdNewsletter( newsletter.getId( ), getPlugin( ) );

        // We sort the elements so that they are ordered by section and order.
        Collections.sort( listTopics );

        // A topic whose section is beyond the sections of the template is not displayed
        String [ ] strContentBySection = new String [ newsletter.getNbSections( )];
        for ( int nSection = 1; nSection <= strContentBySection.length; nSection++ )
        {
            StringBuilder sbSectionContent = new StringBuilder( );
            for ( NewsletterTopic topic : listTopics )
            {
                if ( topic.getSection( ) == nSection )
                {
                    sbSectionContent.append( StringUtils.defaultString( topicContent.apply( topic ) ) );
                }
            }
            strContentBySection [nSection - 1] = bPreview ? MessageFormat.format( TEMPLATE_PREVIEW_SECTION, Integer.toString( nSection ), sbSectionContent )
                    : sbSectionContent.toString( );
        }

        Map<String, Object> model = new HashMap<>( );
        if ( strContentBySection.length > 0 )
        {
            model.put( NewsLetterConstants.MARK_CONTENT, toMarkup( strContentBySection [0] ) );
        }
        for ( int i = 0; i < strContentBySection.length; i++ )
        {
            model.put( NewsLetterConstants.MARK_CONTENT_SECTION + Integer.toString( i + 1 ), toMarkup( strContentBySection [i] ) );
        }
        model.put( NewsLetterConstants.MARK_BASE_URL, strBaseUrl );

        HtmlTemplate templateNewsLetter = AppTemplateService.getTemplate( strTemplatePath, locale, model );

        return templateNewsLetter.getHtml( );
    }

    /**
     * Returns the files of the folder of the newsletter templates (property newsletter.path.file.newsletter.template under the templates folder of
     * the webapp) that a newsletter template can use. The folder is read through the servlet context, so every source folder of the webapp is
     * listed (plugin, site, theme).
     * 
     * @return the list of the file names, sorted by name
     */
    public ReferenceList getTemplateFileList( )
    {
        return getFileList( _strPathTemplates + _strPathFileNewsletterTemplate, FileUtil::hasHtmlExtension );
    }

    /**
     * Checks if the file of a newsletter template is present in the folder of the newsletter templates of the webapp
     * 
     * @param newsletterTemplate
     *            the template
     * @return true if the template has a name and the file of that name is in the folder, false otherwise
     */
    public boolean isTemplateAvailable( NewsLetterTemplate newsletterTemplate )
    {
        return newsletterTemplate != null && StringUtils.isNotEmpty( newsletterTemplate.getTemplateName( ) )
                && getTemplateFileList( ).toMap( ).containsKey( newsletterTemplate.getTemplateName( ) );
    }

    /**
     * Returns the pictures of the folder of the pictures of the newsletter templates (property newsletter.path.image.newsletter.template), read
     * through the servlet context
     * 
     * @return the list of the picture file names, sorted by name
     */
    public ReferenceList getTemplatePictureList( )
    {
        return getFileList( _strPathImageNewsletterTemplate, FileUtil::hasImageExtension );
    }

    /**
     * Returns the names of the files of a folder of the webapp accepted by a filter. The folder is listed through the servlet context, which merges
     * every source folder of the webapp: a plain file system listing would only read the first one.
     * 
     * @param strFolder
     *            the folder, relative to the root of the webapp
     * @param filter
     *            the filter on the file names
     * @return the list of the file names, sorted by name, empty when the folder cannot be read
     */
    private static ReferenceList getFileList( String strFolder, Predicate<String> filter )
    {
        ReferenceList listFiles = new ReferenceList( );
        String strPath = Strings.CS.appendIfMissing( strFolder, NewsLetterConstants.CONSTANT_SLASH );
        Set<URL> setResources;

        try
        {
            setResources = AppPathService.getResourceURLFromRelativePath( strPath );
        }
        catch( ResourceNotFoundException e )
        {
            AppLogService.error( "The folder {} of the newsletter templates cannot be read", strPath, e );

            return listFiles;
        }

        setResources.stream( ).map( URL::getPath ).filter( strResource -> !strResource.endsWith( NewsLetterConstants.CONSTANT_SLASH ) )
                .map( strResource -> URLDecoder.decode( StringUtils.substringAfterLast( strResource, NewsLetterConstants.CONSTANT_SLASH ), StandardCharsets.UTF_8 ) )
                .filter( filter ).distinct( ).sorted( ).forEach( strName -> listFiles.addItem( strName, strName ) );

        return listFiles;
    }

    /**
     * Get the url of the image folder used by templates
     * 
     * @param strBaseUrl
     *            The base url
     * @return The absolute url of the folder containing images of templates.
     */
    public String getImageFolderPath( String strBaseUrl )
    {
        return strBaseUrl + _strPathImageNewsletterTemplate;
    }

    /**
     * Check if images of the newsletter should be transfered on an unsecured webapp or not
     * 
     * @return True if images of the newsletter should be transfered on an unsecured webapp, false otherwise
     */
    public boolean useUnsecuredImages( )
    {
        return _bNoSecuredImgOption;
    }

    /**
     * Get the unsecured image folder inside the unsecured folder
     * 
     * @return The unsecured image folder inside the unsecured folder
     */
    public String getUnsecuredImagefolder( )
    {
        return _strNoSecuredImgFolder + NewsLetterConstants.CONSTANT_SLASH;
    }

    /**
     * Get the absolute path to the unsecured folder where files should be saved
     * 
     * @return The absolute path to the unsecured folder where files should be saved, or the webapp path if none is defined
     */
    public String getUnsecuredFolderPath( )
    {
        return _strNoSecuredWebappPath.orElseGet( ( ) -> AppPathService.getWebAppPath( ) + NewsLetterConstants.CONSTANT_SLASH );
    }

    /**
     * Returns the production url of the site (property lutece.prod.url)
     *
     * @return the production url, empty when none is configured
     */
    public String getProdUrl( )
    {
        return _strProdUrl.orElse( StringUtils.EMPTY );
    }

    /**
     * Returns the stylesheets included in the newsletters (property newsletter.css.files)
     *
     * @return the names of the stylesheets separated by semicolons, empty when none is configured
     */
    public String getCssFiles( )
    {
        return _strCssFiles.orElse( StringUtils.EMPTY );
    }

    /**
     * Get the unsubscription key associated with the given email address.
     * 
     * @param strEmail
     *            The email to get the unsubscription key of.
     * @return The unsubscription key of the email
     */
    public String getUnsubscriptionKey( String strEmail )
    {
        return CryptoService.encrypt( CryptoService.getCryptoKey( ) + strEmail,
                _strUnsubscribeKeyEncryptionAlgorithm );
    }

    /**
     * Modify the number of sections of a newsletter template
     * 
     * @param nOldSectionNumber
     *            The old number of sections
     * @param nNewSectionNumber
     *            The new number of sections
     * @param nTemplateId
     *            The id of the template
     */
    public void modifySectionNumber( int nOldSectionNumber, int nNewSectionNumber, int nTemplateId )
    {
        // If the number of sections changed and is valid
        if ( nOldSectionNumber != nNewSectionNumber && nNewSectionNumber > 0 )
        {
            Collection<NewsLetter> listNewsletters = NewsLetterHome.findAllByTemplateId( nTemplateId, getPlugin( ) );
            for ( NewsLetter newsletter : listNewsletters )
            {
                // If we removed sections we reorganize newsletter's topics
                if ( nOldSectionNumber > nNewSectionNumber )
                {
                    List<NewsletterTopic> listTopics = NewsletterTopicHome.findAllByIdNewsletter( newsletter.getId( ), getPlugin( ) );
                    int nNewOrder = NewsletterTopicHome.getNewOrder( newsletter.getId( ), nNewSectionNumber, getPlugin( ) );
                    for ( NewsletterTopic topic : listTopics )
                    {
                        if ( topic.getSection( ) > nNewSectionNumber )
                        {
                            topic.setSection( nNewSectionNumber );
                            topic.setOrder( nNewOrder );
                            nNewOrder++;
                            NewsletterTopicHome.updateNewsletterTopic( topic, getPlugin( ) );
                        }
                    }
                }
                newsletter.setNbSections( nNewSectionNumber );
                NewsLetterHome.update( newsletter, getPlugin( ) );
            }
        }
    }

    /**
     * Get the instance of the newsletter plugin
     * 
     * @return the instance of the newsletter plugin
     */
    private Plugin getPlugin( )
    {
        return PluginService.getPlugin( NewsletterPlugin.PLUGIN_NAME );
    }

    /**
     * Get the NewsletterTopicService instance of this service
     *
     * @return The NewsletterTopicService instance of this service
     */
    private NewsletterTopicService getNewsletterTopicService( )
    {
        return _newsletterTopicService;
    }

    /**
     * Copy existing newsletter without its subscribers.
     * 
     * @param newsletter
     *            newsletter to copy
     * @param user
     *            the current user
     * @param locale
     *            The locale
     */
    public void copyExistingNewsletter( NewsLetter newsletter, AdminUser user, Locale locale )
    {
        int oldNewsLetterId = newsletter.getId( );
        NewsLetterHome.create( newsletter, getPlugin( ) );

        // Copy of topics
        List<NewsletterTopic> topicList = NewsletterTopicHome.findAllByIdNewsletter( oldNewsLetterId, getPlugin( ) );
        topicList.stream( ).forEach( ( NewsletterTopic nt ) -> {
            int oldTopicId = nt.getId( );
            nt.setIdNewsletter( newsletter.getId( ) );
            for ( INewsletterTopicService service : _topicServices )
            {
                if ( Objects.equals( service.getNewsletterTopicTypeCode( ), nt.getTopicTypeCode( ) ) )
                {
                    NewsletterTopicHome.insertNewsletterTopic( nt, getPlugin( ) );
                    service.copyNewsletterTopic( oldTopicId, nt, user, locale );
                }
            }
        } );
    }

    /**
     * Marks the HTML built by the application as markup for the template (auto-escaping)
     *
     * @param strHtml
     *            The HTML, may be null
     * @return The markup, or null if there is no HTML
     */
    private static Object toMarkup( String strHtml )
    {
        return ( strHtml == null ) ? null : HtmlMarkup.of( strHtml );
    }
}
