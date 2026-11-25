/*
 * Copyright (c) 2002-2026, City of Paris
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
import fr.paris.lutece.plugins.newsletter.business.NewsLetter;
import fr.paris.lutece.plugins.newsletter.business.Pair;
import fr.paris.lutece.plugins.newsletter.business.Subscriber;
import fr.paris.lutece.plugins.newsletter.business.SubscriberFilter;
import fr.paris.lutece.plugins.newsletter.business.SubscriberHome;
import fr.paris.lutece.plugins.newsletter.business.Subscription;
import fr.paris.lutece.plugins.newsletter.service.NewsletterPlugin;
import fr.paris.lutece.plugins.newsletter.service.NewsletterResourceIdService;
import fr.paris.lutece.plugins.newsletter.service.SubscriberResourceIdService;
import fr.paris.lutece.plugins.newsletter.service.SubscriberService;
import fr.paris.lutece.portal.business.rbac.RBAC;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.rbac.RBACService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.portal.util.mvc.utils.MVCUtils;
import fr.paris.lutece.portal.web.cdi.mvc.Models;
import fr.paris.lutece.portal.web.constants.Messages;
import fr.paris.lutece.portal.web.util.IPager;
import fr.paris.lutece.portal.web.util.Pager;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.string.StringUtil;
import fr.paris.lutece.util.url.UrlItem;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

/**
 * JspBean of the management of the subscribers across the newsletters: search, suspension and activation of a subscriber, subscription and
 * unsubscription, confirmation of the pending subscriptions. An administrator sees the subscribers of the newsletters of his workgroups on which he
 * holds the permission of managing the subscribers, and only their subscriptions to these newsletters.
 */
@RequestScoped
@Named( "newsletterSubscriberJspBean" )
@Controller( controllerJsp = SubscriberJspBean.CONTROLLER_JSP, controllerPath = SubscriberJspBean.CONTROLLER_PATH, right = SubscriberJspBean.RIGHT_NEWSLETTER_SUBSCRIBER_MANAGEMENT, securityTokenEnabled = true )
public class SubscriberJspBean extends MVCAdminJspBean
{
    /**
     * The right used for managing the subscribers
     */
    public static final String RIGHT_NEWSLETTER_SUBSCRIBER_MANAGEMENT = "NEWSLETTER_SUBSCRIBER_MANAGEMENT";

    /**
     * The controller JSP
     */
    public static final String CONTROLLER_JSP = "ManageSubscribersGlobal.jsp";

    /**
     * The controller path
     */
    public static final String CONTROLLER_PATH = "jsp/admin/plugins/newsletter/";

    private static final long serialVersionUID = 4283105925468703911L;

    // TEMPLATES
    private static final String TEMPLATE_MANAGE_SUBSCRIBERS = "admin/plugins/newsletter/subscriber/manage_subscribers_global.html";
    private static final String TEMPLATE_VIEW_SUBSCRIBER = "admin/plugins/newsletter/subscriber/view_subscriber.html";

    // VIEWS
    private static final String VIEW_MANAGE_SUBSCRIBERS = "manageSubscribers";
    private static final String VIEW_SUBSCRIBER = "viewSubscriber";
    private static final String VIEW_CONFIRM_UNSUBSCRIBE = "confirmUnsubscribe";

    // ACTIONS
    private static final String ACTION_PROCESS_SUBSCRIBERS = "processSubscribers";
    private static final String ACTION_UNSUBSCRIBE = "unsubscribe";

    // OPERATIONS
    private static final String OPERATION_SUSPEND = "suspend";
    private static final String OPERATION_ACTIVATE = "activate";
    private static final String OPERATION_VALIDATE = "validate";
    private static final String OPERATION_SUBSCRIBE = "subscribe";
    private static final String OPERATION_RESEND_CONFIRMATION = "resendConfirmation";
    private static final String OPERATION_CREATE = "create";

    // PARAMETERS
    private static final String PARAMETER_ID_SUBSCRIBER = "id_subscriber";
    private static final String PARAMETER_NEWSLETTER_ID = "newsletter_id";
    private static final String PARAMETER_SEARCH_EMAIL = "search_email";
    private static final String PARAMETER_SUBSCRIBER_STATUS = "subscriber_status";
    private static final String PARAMETER_SUBSCRIPTION_STATE = "subscription_state";
    private static final String PARAMETER_SUBSCRIBER_SELECTION = "subscriber_selection";
    private static final String PARAMETER_OPERATION = "operation";
    private static final String PARAMETER_SEND_CONFIRMATION = "send_confirmation";
    private static final String PARAMETER_RETURN_VIEW = "return_view";
    private static final String PARAMETER_EMAIL = "email";
    private static final String PARAMETER_NEWSLETTER_IDS = "newsletter_ids";

    // VALUES OF THE FILTER PARAMETERS
    private static final String VALUE_STATUS_ACTIVE = "active";
    private static final String VALUE_STATUS_SUSPENDED = "suspended";
    private static final String VALUE_SUBSCRIPTION_CONFIRMED = "confirmed";
    private static final String VALUE_SUBSCRIPTION_PENDING = "pending";

    // MARKS
    private static final String MARK_SUBSCRIBERS_LIST = "subscribers_list";
    private static final String MARK_SUBSCRIPTION_COUNTS = "subscription_counts";
    private static final String MARK_NEWSLETTERS_LIST = "newsletters_list";
    private static final String MARK_STATUS_LIST = "status_list";
    private static final String MARK_SUBSCRIPTION_STATE_LIST = "subscription_state_list";
    private static final String MARK_SEARCH_EMAIL = "search_email";
    private static final String MARK_SUBSCRIBER_STATUS = "subscriber_status";
    private static final String MARK_SUBSCRIPTION_STATE = "subscription_state";
    private static final String MARK_NEWSLETTER_ID = "newsletter_id";
    private static final String MARK_FILTER_ACTIVE = "filter_active";
    private static final String MARK_SUBSCRIBER = "subscriber";
    private static final String MARK_SUBSCRIPTIONS_LIST = "subscriptions_list";
    private static final String MARK_AVAILABLE_NEWSLETTERS = "available_newsletters";
    private static final String MARK_HAS_PENDING = "has_pending";
    private static final String MARK_STATUS_CHANGE_ALLOWED = "status_change_allowed";
    private static final String MARK_CLEAN_SUBSCRIBERS_ALLOWED = "clean_subscribers_allowed";
    private static final String MARK_SUBSCRIBER_STATISTICS = "subscriber_statistics";
    private static final String MARK_PERMISSION_CREATE = "permission_create";
    private static final String MARK_PERMISSION_MODIFY_STATUS = "permission_modify_status";
    private static final String MARK_PERMISSION_VALIDATE = "permission_validate";
    private static final String MARK_PERMISSION_SUBSCRIBE = "permission_subscribe";
    private static final String MARK_PERMISSION_UNSUBSCRIBE = "permission_unsubscribe";

    // PROPERTIES
    private static final String PROPERTY_PAGE_TITLE_MANAGE_SUBSCRIBERS = "newsletter.manage_subscribers_global.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_VIEW_SUBSCRIBER = "newsletter.view_subscriber.pageTitle";
    private static final String PROPERTY_ITEMS_PER_PAGE = "newsletter.subscribers.itemsPerPage";
    private static final String PROPERTY_OPTION_ALL = "newsletter.manage_subscribers_global.optionAll";
    private static final String PROPERTY_OPTION_ALL_NEWSLETTERS = "newsletter.manage_subscribers_global.optionAllNewsletters";
    private static final String PROPERTY_STATUS_ACTIVE = "newsletter.model.entity.subscriber.attribute.status.active";
    private static final String PROPERTY_STATUS_SUSPENDED = "newsletter.model.entity.subscriber.attribute.status.suspended";
    private static final String PROPERTY_SUBSCRIPTION_CONFIRMED = "newsletter.manage_subscribers.countConfirmed";
    private static final String PROPERTY_SUBSCRIPTION_PENDING = "newsletter.manage_subscribers.countPending";

    // MESSAGES
    private static final String MESSAGE_CONFIRM_UNSUBSCRIBE = "newsletter.message.confirmUnsubscribe";
    private static final String MESSAGE_CONFIRM_UNSUBSCRIBE_SELECTED = "newsletter.message.confirmUnsubscribeSelected";
    private static final String MESSAGE_SUBSCRIBER_NOT_FOUND = "newsletter.message.error.subscriberNotFound";
    private static final String MESSAGE_NEWSLETTER_NOT_AUTHORIZED = "newsletter.message.error.newsletterNotAuthorized";
    private static final String MESSAGE_ALREADY_SUBSCRIBED = "newsletter.message.error.alreadySubscribed";
    private static final String MESSAGE_ALREADY_SUBSCRIBED_TO_ALL = "newsletter.message.error.alreadySubscribedToAll";
    private static final String MESSAGE_FIELD_EMAIL_VALID = "newsletter.message.fieldEmailValid";
    private static final String MESSAGE_NO_NEWSLETTER_SELECTED = "newsletter.message.error.noNewsletterSelected";
    private static final String MESSAGE_NOTHING_PENDING = "newsletter.message.error.nothingPending";
    private static final String MESSAGE_NO_SELECTION = "newsletter.message.error.noSubscriberSelected";
    private static final String MESSAGE_STATUS_NOT_CHANGED = "newsletter.message.error.statusNotChanged";
    private static final String INFO_SUBSCRIBERS_SUSPENDED = "newsletter.message.success.subscribersSuspended";
    private static final String INFO_SUBSCRIBERS_ACTIVATED = "newsletter.message.success.subscribersActivated";
    private static final String INFO_SUBSCRIPTIONS_VALIDATED = "newsletter.message.success.subscriptionsValidated";
    private static final String INFO_SUBSCRIPTION_ADDED = "newsletter.message.success.subscriptionAdded";
    private static final String INFO_SUBSCRIPTIONS_ADDED = "newsletter.message.success.subscriptionsAdded";
    private static final String INFO_SUBSCRIPTION_REMOVED = "newsletter.message.success.subscriptionRemoved";
    private static final String INFO_SUBSCRIPTIONS_REMOVED = "newsletter.message.success.subscriptionsRemoved";
    private static final String INFO_SUBSCRIBER_REMOVED = "newsletter.message.success.subscriberRemoved";
    private static final String INFO_CONFIRMATION_SENT = "newsletter.message.success.confirmationSent";

    @Inject
    private transient SubscriberService _subscriberService;

    @Inject
    @Pager( listBookmark = MARK_SUBSCRIBERS_LIST, defaultItemsPerPage = PROPERTY_ITEMS_PER_PAGE, baseUrl = CONTROLLER_PATH + CONTROLLER_JSP )
    private IPager<Integer, Subscriber> _pager;

    /**
     * Displays the subscribers matching the filter of the request: part of the email, newsletter, status of the subscriber and state of the
     * subscription. The view is opened from the subscribers tab of a newsletter with the newsletter, and possibly the pending state, already
     * chosen.
     *
     * @param request
     *            the HTTP request
     * @param model
     *            the model of the page
     * @return the page
     */
    @View( value = VIEW_MANAGE_SUBSCRIBERS, defaultView = true, securityTokenAction = ACTION_PROCESS_SUBSCRIBERS )
    public String getManageSubscribers( HttpServletRequest request, Models model )
    {
        if ( !isAllowed( SubscriberResourceIdService.PERMISSION_VIEW ) )
        {
            return redirectAccessDenied( request );
        }

        List<NewsLetter> listNewsletters = _subscriberService.getAuthorizedNewsletters( (User) getUser( ) );
        List<Integer> listIdNewsletters = _subscriberService.getNewsletterIds( listNewsletters );
        SubscriberFilter filter = buildFilter( request, listIdNewsletters );

        if ( filter == null )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, MESSAGE_NEWSLETTER_NOT_AUTHORIZED, AdminMessage.TYPE_STOP ) );
        }

        UrlItem urlPager = new UrlItem( CONTROLLER_PATH + CONTROLLER_JSP );
        urlPager.addParameter( MVCUtils.PARAMETER_VIEW, VIEW_MANAGE_SUBSCRIBERS );
        getFilterParameters( request ).forEach( urlPager::addParameter );

        _pager.withBaseUrl( urlPager.getUrl( ) ).withIdList( _subscriberService.findSubscriberIds( filter ) ).populateModels( request, model,
                listIds -> SubscriberHome.findByIds( listIds, PluginService.getPlugin( NewsletterPlugin.PLUGIN_NAME ) ), getLocale( ) );

        @SuppressWarnings( "unchecked" )
        List<Subscriber> listPageSubscribers = (List<Subscriber>) model.get( MARK_SUBSCRIBERS_LIST );
        Map<String, Pair<Integer, Integer>> mapCounts = new HashMap<>( );
        _subscriberService.getSubscriptionCounts( listPageSubscribers, listIdNewsletters )
                .forEach( ( nIdSubscriber, counts ) -> mapCounts.put( Integer.toString( nIdSubscriber ), counts ) );

        model.put( MARK_SUBSCRIPTION_COUNTS, mapCounts );
        ReferenceList refNewsletters = new ReferenceList( );
        refNewsletters.addItem( StringUtils.EMPTY, I18nService.getLocalizedString( PROPERTY_OPTION_ALL_NEWSLETTERS, getLocale( ) ) );
        refNewsletters.addAll( buildNewsletterList( listNewsletters ) );

        model.put( MARK_NEWSLETTERS_LIST, refNewsletters );
        model.put( MARK_STATUS_LIST, buildChoiceList( VALUE_STATUS_ACTIVE, PROPERTY_STATUS_ACTIVE, VALUE_STATUS_SUSPENDED, PROPERTY_STATUS_SUSPENDED ) );
        model.put( MARK_SUBSCRIPTION_STATE_LIST,
                buildChoiceList( VALUE_SUBSCRIPTION_CONFIRMED, PROPERTY_SUBSCRIPTION_CONFIRMED, VALUE_SUBSCRIPTION_PENDING, PROPERTY_SUBSCRIPTION_PENDING ) );
        model.put( MARK_SEARCH_EMAIL, StringUtils.trimToEmpty( request.getParameter( PARAMETER_SEARCH_EMAIL ) ) );
        model.put( MARK_SUBSCRIBER_STATUS, StringUtils.trimToEmpty( request.getParameter( PARAMETER_SUBSCRIBER_STATUS ) ) );
        model.put( MARK_SUBSCRIPTION_STATE, StringUtils.trimToEmpty( request.getParameter( PARAMETER_SUBSCRIPTION_STATE ) ) );
        model.put( MARK_NEWSLETTER_ID, filter.getIdNewsletter( ) == SubscriberFilter.ALL ? StringUtils.EMPTY : Integer.toString( filter.getIdNewsletter( ) ) );
        model.put( MARK_FILTER_ACTIVE, !getFilterParameters( request ).isEmpty( ) );
        model.put( MARK_SUBSCRIBER_STATISTICS, _subscriberService.getStatistics( listIdNewsletters ) );
        fillPermissions( model );
        model.put( MARK_CLEAN_SUBSCRIBERS_ALLOWED, getUser( ).checkRight( NewsletterJspBean.RIGHT_NEWSLETTER_MANAGEMENT )
                && RBACService.isAuthorized( NewsLetter.RESOURCE_TYPE, null, NewsletterResourceIdService.PERMISSION_CLEAN_SUBSCRIBERS, (User) getUser( ) ) );

        return getPage( PROPERTY_PAGE_TITLE_MANAGE_SUBSCRIBERS, TEMPLATE_MANAGE_SUBSCRIBERS );
    }

    /**
     * Displays a subscriber: his status, his subscriptions to the newsletters the administrator may manage, and the newsletters he may be subscribed
     * to
     *
     * @param request
     *            the HTTP request
     * @param model
     *            the model of the page
     * @return the page
     */
    @View( value = VIEW_SUBSCRIBER, securityTokenAction = ACTION_PROCESS_SUBSCRIBERS )
    public String getViewSubscriber( HttpServletRequest request, Models model )
    {
        if ( !isAllowed( SubscriberResourceIdService.PERMISSION_VIEW ) )
        {
            return redirectAccessDenied( request );
        }

        List<NewsLetter> listNewsletters = _subscriberService.getAuthorizedNewsletters( (User) getUser( ) );
        List<Integer> listIdNewsletters = _subscriberService.getNewsletterIds( listNewsletters );
        Subscriber subscriber = findSubscriber( request.getParameter( PARAMETER_ID_SUBSCRIBER ), listIdNewsletters );

        if ( subscriber == null )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, MESSAGE_SUBSCRIBER_NOT_FOUND, AdminMessage.TYPE_STOP ) );
        }

        List<Subscription> listSubscriptions = _subscriberService.getSubscriptions( subscriber.getId( ), listIdNewsletters );
        Set<Integer> setSubscribed = new LinkedHashSet<>( );
        boolean bHasPending = false;

        for ( Subscription subscription : listSubscriptions )
        {
            setSubscribed.add( subscription.getIdNewsletter( ) );
            bHasPending = bHasPending || !subscription.isConfirmed( );
        }

        List<NewsLetter> listAvailable = new ArrayList<>( );

        for ( NewsLetter newsletter : listNewsletters )
        {
            if ( !setSubscribed.contains( newsletter.getId( ) ) )
            {
                listAvailable.add( newsletter );
            }
        }

        model.put( MARK_SUBSCRIBER, subscriber );
        model.put( MARK_SUBSCRIPTIONS_LIST, listSubscriptions );
        model.put( MARK_AVAILABLE_NEWSLETTERS, buildNewsletterList( listAvailable ) );
        model.put( MARK_HAS_PENDING, bHasPending );
        model.put( MARK_STATUS_CHANGE_ALLOWED, _subscriberService.isSubscribedOnlyTo( subscriber.getId( ), listIdNewsletters ) );
        fillPermissions( model );

        return getPage( PROPERTY_PAGE_TITLE_VIEW_SUBSCRIBER, TEMPLATE_VIEW_SUBSCRIBER );
    }

    /**
     * Asks the confirmation of the unsubscription of a subscriber from one or several newsletters
     *
     * @param request
     *            the HTTP request
     * @return the url of the confirmation message
     */
    @View( value = VIEW_CONFIRM_UNSUBSCRIBE, securityTokenAction = ACTION_UNSUBSCRIBE )
    public String getConfirmUnsubscribe( HttpServletRequest request )
    {
        if ( !isAllowed( SubscriberResourceIdService.PERMISSION_UNSUBSCRIBE ) )
        {
            return redirectAccessDenied( request );
        }

        List<Integer> listIdNewsletters = _subscriberService.getNewsletterIds( _subscriberService.getAuthorizedNewsletters( (User) getUser( ) ) );
        Subscriber subscriber = findSubscriber( request.getParameter( PARAMETER_ID_SUBSCRIBER ), listIdNewsletters );
        List<Integer> listUnsubscribed = getChosenNewsletters( request, PARAMETER_NEWSLETTER_ID, listIdNewsletters );

        if ( subscriber == null )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, MESSAGE_SUBSCRIBER_NOT_FOUND, AdminMessage.TYPE_STOP ) );
        }

        if ( listUnsubscribed == null || listUnsubscribed.isEmpty( ) )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, MESSAGE_NEWSLETTER_NOT_AUTHORIZED, AdminMessage.TYPE_STOP ) );
        }

        UrlItem url = new UrlItem( getActionUrl( ACTION_UNSUBSCRIBE ) );
        url.addParameter( PARAMETER_ID_SUBSCRIBER, subscriber.getId( ) );
        listUnsubscribed.forEach( nIdNewsletter -> url.addParameter( PARAMETER_NEWSLETTER_ID, nIdNewsletter ) );

        if ( listUnsubscribed.size( ) == 1 )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_UNSUBSCRIBE, new Object [ ] {
                    subscriber.getEmail( )
            }, url.getUrl( ), AdminMessage.TYPE_CONFIRMATION ) );
        }

        return redirect( request, AdminMessageService.getMessageUrl( request, MESSAGE_CONFIRM_UNSUBSCRIBE_SELECTED, new Object [ ] {
                subscriber.getEmail( ), listUnsubscribed.size( )
        }, url.getUrl( ), AdminMessage.TYPE_CONFIRMATION ) );
    }

    /**
     * Unsubscribes a subscriber from one or several newsletters. A subscriber without subscription left is removed.
     *
     * @param request
     *            the HTTP request
     * @return the redirection to the subscriber, or to the list when the subscriber has been removed
     */
    @Action( ACTION_UNSUBSCRIBE )
    public String doUnsubscribe( HttpServletRequest request )
    {
        if ( !isAllowed( SubscriberResourceIdService.PERMISSION_UNSUBSCRIBE ) )
        {
            return redirectAccessDenied( request );
        }

        List<Integer> listIdNewsletters = _subscriberService.getNewsletterIds( _subscriberService.getAuthorizedNewsletters( (User) getUser( ) ) );
        Subscriber subscriber = findSubscriber( request.getParameter( PARAMETER_ID_SUBSCRIBER ), listIdNewsletters );
        List<Integer> listUnsubscribed = getChosenNewsletters( request, PARAMETER_NEWSLETTER_ID, listIdNewsletters );

        if ( subscriber == null )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, MESSAGE_SUBSCRIBER_NOT_FOUND, AdminMessage.TYPE_STOP ) );
        }

        if ( listUnsubscribed == null || listUnsubscribed.isEmpty( ) )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, MESSAGE_NEWSLETTER_NOT_AUTHORIZED, AdminMessage.TYPE_STOP ) );
        }

        for ( Integer nIdNewsletter : listUnsubscribed )
        {
            if ( _subscriberService.unsubscribe( subscriber.getId( ), nIdNewsletter ) )
            {
                addInfo( INFO_SUBSCRIBER_REMOVED, getLocale( ) );

                return redirectView( request, VIEW_MANAGE_SUBSCRIBERS );
            }
        }

        addInfo( listUnsubscribed.size( ) == 1 ? INFO_SUBSCRIPTION_REMOVED : INFO_SUBSCRIPTIONS_REMOVED, getLocale( ) );

        return redirect( request, VIEW_SUBSCRIBER, PARAMETER_ID_SUBSCRIBER, subscriber.getId( ) );
    }

    /**
     * Processes an operation on the subscribers selected in the list, or on the subscriber displayed: suspension, activation, confirmation of the
     * pending subscriptions, subscription to a newsletter, sending again of the confirmation email. Processes also the creation of a subscriber
     * from the list, which needs no selection.
     *
     * @param request
     *            the HTTP request
     * @return the redirection to the subscriber, or to the list with its filter
     */
    @Action( ACTION_PROCESS_SUBSCRIBERS )
    public String doProcessSubscribers( HttpServletRequest request )
    {
        List<Integer> listIdNewsletters = _subscriberService.getNewsletterIds( _subscriberService.getAuthorizedNewsletters( (User) getUser( ) ) );
        String strOperation = StringUtils.defaultString( request.getParameter( PARAMETER_OPERATION ) );
        String strIdNewsletter = request.getParameter( PARAMETER_NEWSLETTER_ID );

        if ( StringUtils.isNotEmpty( strIdNewsletter ) && !listIdNewsletters.contains( parseId( strIdNewsletter ) ) )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, MESSAGE_NEWSLETTER_NOT_AUTHORIZED, AdminMessage.TYPE_STOP ) );
        }

        if ( !isAllowed( getOperationPermission( strOperation ) ) )
        {
            return redirectAccessDenied( request );
        }

        if ( OPERATION_CREATE.equals( strOperation ) )
        {
            return doCreateSubscriber( request, listIdNewsletters );
        }

        List<Subscriber> listSubscribers = getSelectedSubscribers( request, listIdNewsletters );

        if ( listSubscribers.isEmpty( ) )
        {
            addError( MESSAGE_NO_SELECTION, getLocale( ) );

            return redirectBack( request );
        }

        switch( strOperation )
        {
            case OPERATION_SUSPEND:
                changeStatus( listSubscribers, listIdNewsletters, true );
                break;
            case OPERATION_ACTIVATE:
                changeStatus( listSubscribers, listIdNewsletters, false );
                break;
            case OPERATION_VALIDATE:
                List<Integer> listValidated = StringUtils.isNotEmpty( strIdNewsletter ) ? Collections.singletonList( parseId( strIdNewsletter ) )
                        : listIdNewsletters;
                listSubscribers.forEach( subscriber -> _subscriberService.validate( subscriber.getId( ), listValidated ) );
                addInfo( INFO_SUBSCRIPTIONS_VALIDATED, getLocale( ) );
                break;
            case OPERATION_SUBSCRIBE:
                return doSubscribe( request, listSubscribers.get( 0 ), listIdNewsletters );
            case OPERATION_RESEND_CONFIRMATION:
                if ( _subscriberService.resendConfirmation( listSubscribers.get( 0 ), listIdNewsletters, AppPathService.getProdUrl( request ), getLocale( ) ) )
                {
                    addInfo( INFO_CONFIRMATION_SENT, getLocale( ) );
                }
                else
                {
                    addError( MESSAGE_NOTHING_PENDING, getLocale( ) );
                }
                break;
            default:
                return redirect( request, AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_STOP ) );
        }

        return redirectBack( request );
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
     * Creates a subscriber, or takes the subscriber already registered with the email, and subscribes him to the newsletters chosen among those the
     * administrator may manage, either confirmed or awaiting his confirmation with the confirmation email sent
     *
     * @param request
     *            the HTTP request
     * @param listIdNewsletters
     *            the identifiers of the newsletters the administrator may manage
     * @return the redirection to the subscriber, to the list when the email is invalid or no newsletter is chosen, or the url of the error message
     *         when a newsletter chosen is not authorized
     */
    private String doCreateSubscriber( HttpServletRequest request, List<Integer> listIdNewsletters )
    {
        String strEmail = StringUtils.trimToEmpty( StringEscapeUtils.unescapeHtml4( request.getParameter( PARAMETER_EMAIL ) ) );

        if ( strEmail.isEmpty( ) || !StringUtil.checkEmail( strEmail ) )
        {
            addError( MESSAGE_FIELD_EMAIL_VALID, getLocale( ) );

            return redirectView( request, VIEW_MANAGE_SUBSCRIBERS );
        }

        List<Integer> listChosen = getChosenNewsletters( request, PARAMETER_NEWSLETTER_IDS, listIdNewsletters );

        if ( listChosen == null )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, MESSAGE_NEWSLETTER_NOT_AUTHORIZED, AdminMessage.TYPE_STOP ) );
        }

        if ( listChosen.isEmpty( ) )
        {
            addError( MESSAGE_NO_NEWSLETTER_SELECTED, getLocale( ) );

            return redirectView( request, VIEW_MANAGE_SUBSCRIBERS );
        }

        Subscriber subscriber = _subscriberService.findOrCreateSubscriber( strEmail );
        subscribeToNewsletters( request, subscriber, listChosen );

        return redirect( request, VIEW_SUBSCRIBER, PARAMETER_ID_SUBSCRIBER, subscriber.getId( ) );
    }

    /**
     * Subscribes the subscriber displayed to the newsletters chosen among those the administrator may manage
     *
     * @param request
     *            the HTTP request
     * @param subscriber
     *            the subscriber
     * @param listIdNewsletters
     *            the identifiers of the newsletters the administrator may manage
     * @return the redirection to the subscriber, or the url of the error message when a newsletter chosen is not authorized
     */
    private String doSubscribe( HttpServletRequest request, Subscriber subscriber, List<Integer> listIdNewsletters )
    {
        List<Integer> listChosen = getChosenNewsletters( request, PARAMETER_NEWSLETTER_IDS, listIdNewsletters );

        if ( listChosen == null )
        {
            return redirect( request, AdminMessageService.getMessageUrl( request, MESSAGE_NEWSLETTER_NOT_AUTHORIZED, AdminMessage.TYPE_STOP ) );
        }

        if ( listChosen.isEmpty( ) )
        {
            addError( MESSAGE_NO_NEWSLETTER_SELECTED, getLocale( ) );
        }
        else
        {
            subscribeToNewsletters( request, subscriber, listChosen );
        }

        return redirectBack( request );
    }

    /**
     * Returns the newsletters chosen in the request
     *
     * @param request
     *            the HTTP request
     * @param strParameter
     *            the name of the parameter holding the identifiers of the newsletters
     * @param listIdNewsletters
     *            the identifiers of the newsletters the administrator may manage
     * @return the identifiers of the newsletters chosen, without duplicate, null when one of them is not authorized
     */
    private List<Integer> getChosenNewsletters( HttpServletRequest request, String strParameter, List<Integer> listIdNewsletters )
    {
        List<Integer> listChosen = new ArrayList<>( );
        String [ ] arrayIdNewsletters = request.getParameterValues( strParameter );

        for ( String strIdNewsletter : arrayIdNewsletters == null ? new String [ 0] : arrayIdNewsletters )
        {
            int nIdNewsletter = parseId( strIdNewsletter );

            if ( !listIdNewsletters.contains( nIdNewsletter ) )
            {
                return null;
            }

            if ( !listChosen.contains( nIdNewsletter ) )
            {
                listChosen.add( nIdNewsletter );
            }
        }

        return listChosen;
    }

    /**
     * Subscribes a subscriber to newsletters, either confirmed or awaiting his confirmation with the confirmation email sent for the subscriptions
     * added
     *
     * @param request
     *            the HTTP request
     * @param subscriber
     *            the subscriber
     * @param listChosen
     *            the identifiers of the newsletters, already checked as authorized
     */
    private void subscribeToNewsletters( HttpServletRequest request, Subscriber subscriber, List<Integer> listChosen )
    {
        boolean bSendConfirmation = request.getParameter( PARAMETER_SEND_CONFIRMATION ) != null;
        List<Integer> listAdded = new ArrayList<>( );

        for ( Integer nIdNewsletter : listChosen )
        {
            if ( _subscriberService.subscribe( subscriber.getId( ), nIdNewsletter, !bSendConfirmation ) )
            {
                listAdded.add( nIdNewsletter );
            }
        }

        if ( listAdded.isEmpty( ) )
        {
            addError( listChosen.size( ) == 1 ? MESSAGE_ALREADY_SUBSCRIBED : MESSAGE_ALREADY_SUBSCRIBED_TO_ALL, getLocale( ) );
        }
        else
        {
            addInfo( listAdded.size( ) == 1 ? INFO_SUBSCRIPTION_ADDED : INFO_SUBSCRIPTIONS_ADDED, getLocale( ) );
        }

        if ( bSendConfirmation && !listAdded.isEmpty( )
                && _subscriberService.resendConfirmation( subscriber, listAdded, AppPathService.getProdUrl( request ), getLocale( ) ) )
        {
            addInfo( INFO_CONFIRMATION_SENT, getLocale( ) );
        }
    }

    /**
     * Suspends or activates subscribers. A subscriber subscribed to a newsletter the administrator does not manage is left unchanged: his status
     * applies to all his subscriptions.
     *
     * @param listSubscribers
     *            the subscribers
     * @param listIdNewsletters
     *            the identifiers of the newsletters the administrator may manage
     * @param bSuspend
     *            true to suspend the subscribers, false to activate them
     */
    private void changeStatus( List<Subscriber> listSubscribers, List<Integer> listIdNewsletters, boolean bSuspend )
    {
        int nSkipped = 0;

        for ( Subscriber subscriber : listSubscribers )
        {
            if ( !_subscriberService.isSubscribedOnlyTo( subscriber.getId( ), listIdNewsletters ) )
            {
                nSkipped++;
            }
            else
                if ( bSuspend )
                {
                    _subscriberService.suspend( subscriber.getId( ) );
                }
                else
                {
                    _subscriberService.activate( subscriber.getId( ) );
                }
        }

        if ( nSkipped < listSubscribers.size( ) )
        {
            addInfo( bSuspend ? INFO_SUBSCRIBERS_SUSPENDED : INFO_SUBSCRIBERS_ACTIVATED, getLocale( ) );
        }

        if ( nSkipped > 0 )
        {
            addWarning( MESSAGE_STATUS_NOT_CHANGED, getLocale( ) );
        }
    }

    /**
     * Returns the permission an operation of the list or of the subscriber needs
     *
     * @param strOperation
     *            the operation
     * @return the permission, null for an unknown operation
     */
    private static String getOperationPermission( String strOperation )
    {
        switch( strOperation )
        {
            case OPERATION_SUSPEND:
            case OPERATION_ACTIVATE:
                return SubscriberResourceIdService.PERMISSION_MODIFY_STATUS;
            case OPERATION_VALIDATE:
            case OPERATION_RESEND_CONFIRMATION:
                return SubscriberResourceIdService.PERMISSION_VALIDATE;
            case OPERATION_SUBSCRIBE:
                return SubscriberResourceIdService.PERMISSION_SUBSCRIBE;
            case OPERATION_CREATE:
                return SubscriberResourceIdService.PERMISSION_CREATE;
            default:
                return null;
        }
    }

    /**
     * Tells whether the administrator holds a permission of the management of the subscribers
     *
     * @param strPermission
     *            the permission, null for none
     * @return true if the administrator holds the permission on all the subscribers
     */
    private boolean isAllowed( String strPermission )
    {
        return strPermission != null && RBACService.isAuthorized( Subscriber.RESOURCE_TYPE, RBAC.WILDCARD_RESOURCES_ID, strPermission, (User) getUser( ) );
    }

    /**
     * Puts in the model the permissions of the administrator on the actions of the management of the subscribers
     *
     * @param model
     *            the model of the page
     */
    private void fillPermissions( Models model )
    {
        model.put( MARK_PERMISSION_CREATE, isAllowed( SubscriberResourceIdService.PERMISSION_CREATE ) );
        model.put( MARK_PERMISSION_MODIFY_STATUS, isAllowed( SubscriberResourceIdService.PERMISSION_MODIFY_STATUS ) );
        model.put( MARK_PERMISSION_VALIDATE, isAllowed( SubscriberResourceIdService.PERMISSION_VALIDATE ) );
        model.put( MARK_PERMISSION_SUBSCRIBE, isAllowed( SubscriberResourceIdService.PERMISSION_SUBSCRIBE ) );
        model.put( MARK_PERMISSION_UNSUBSCRIBE, isAllowed( SubscriberResourceIdService.PERMISSION_UNSUBSCRIBE ) );
    }

    /**
     * Redirects to the message telling the administrator he is not allowed to do the action
     *
     * @param request
     *            the HTTP request
     * @return the url of the message
     */
    private String redirectAccessDenied( HttpServletRequest request )
    {
        return redirect( request, AdminMessageService.getMessageUrl( request, Messages.USER_ACCESS_DENIED, AdminMessage.TYPE_STOP ) );
    }

    /**
     * Builds the filter of the list from the request
     *
     * @param request
     *            the HTTP request
     * @param listIdNewsletters
     *            the identifiers of the newsletters the administrator may manage
     * @return the filter, null when the newsletter of the request is not one of the authorized newsletters
     */
    private SubscriberFilter buildFilter( HttpServletRequest request, List<Integer> listIdNewsletters )
    {
        SubscriberFilter filter = new SubscriberFilter( );
        filter.setAuthorizedNewsletterIds( listIdNewsletters );
        filter.setEmail( StringUtils.trimToNull( StringEscapeUtils.unescapeHtml4( request.getParameter( PARAMETER_SEARCH_EMAIL ) ) ) );

        String strIdNewsletter = request.getParameter( PARAMETER_NEWSLETTER_ID );

        if ( StringUtils.isNotEmpty( strIdNewsletter ) )
        {
            int nIdNewsletter = parseId( strIdNewsletter );

            if ( !listIdNewsletters.contains( nIdNewsletter ) )
            {
                return null;
            }

            filter.setIdNewsletter( nIdNewsletter );
        }

        String strStatus = StringUtils.defaultString( request.getParameter( PARAMETER_SUBSCRIBER_STATUS ) );

        if ( VALUE_STATUS_ACTIVE.equals( strStatus ) )
        {
            filter.setStatus( Subscriber.STATUS_ACTIVE );
        }
        else
            if ( VALUE_STATUS_SUSPENDED.equals( strStatus ) )
            {
                filter.setStatus( Subscriber.STATUS_SUSPENDED );
            }

        String strState = StringUtils.defaultString( request.getParameter( PARAMETER_SUBSCRIPTION_STATE ) );

        if ( VALUE_SUBSCRIPTION_CONFIRMED.equals( strState ) )
        {
            filter.setSubscriptionState( SubscriberFilter.SUBSCRIPTION_CONFIRMED );
        }
        else
            if ( VALUE_SUBSCRIPTION_PENDING.equals( strState ) )
            {
                filter.setSubscriptionState( SubscriberFilter.SUBSCRIPTION_PENDING );
            }

        return filter;
    }

    /**
     * Returns the parameters of the filter given in the request, ready to be put in an url
     *
     * @param request
     *            the HTTP request
     * @return the non empty filter parameters, by name
     */
    private Map<String, String> getFilterParameters( HttpServletRequest request )
    {
        Map<String, String> mapParameters = new HashMap<>( );

        for ( String strParameter : new String [ ] {
                PARAMETER_SEARCH_EMAIL, PARAMETER_NEWSLETTER_ID, PARAMETER_SUBSCRIBER_STATUS, PARAMETER_SUBSCRIPTION_STATE
        } )
        {
            String strValue = StringUtils.trimToEmpty( StringEscapeUtils.unescapeHtml4( request.getParameter( strParameter ) ) );

            if ( !strValue.isEmpty( ) )
            {
                mapParameters.put( strParameter, URLEncoder.encode( strValue, StandardCharsets.UTF_8 ) );
            }
        }

        return mapParameters;
    }

    /**
     * Redirects, after an operation, to the subscriber displayed or to the list with its filter
     *
     * @param request
     *            the HTTP request
     * @return the redirection
     */
    private String redirectBack( HttpServletRequest request )
    {
        int nIdSubscriber = parseId( request.getParameter( PARAMETER_ID_SUBSCRIBER ) );

        if ( VIEW_SUBSCRIBER.equals( request.getParameter( PARAMETER_RETURN_VIEW ) ) && SubscriberHome.findNewsLetters( nIdSubscriber, getPlugin( ) ) > 0 )
        {
            return redirect( request, VIEW_SUBSCRIBER, PARAMETER_ID_SUBSCRIBER, nIdSubscriber );
        }

        return redirect( request, VIEW_MANAGE_SUBSCRIBERS, getFilterParameters( request ) );
    }

    /**
     * Returns the subscribers an operation applies to: the subscriber displayed, or the subscribers selected in the list. The subscribers the
     * administrator may not see are left out.
     *
     * @param request
     *            the HTTP request
     * @param listIdNewsletters
     *            the identifiers of the newsletters the administrator may manage
     * @return the subscribers
     */
    private List<Subscriber> getSelectedSubscribers( HttpServletRequest request, List<Integer> listIdNewsletters )
    {
        List<Subscriber> listSubscribers = new ArrayList<>( );
        String [ ] arrayIds = request.getParameterValues( PARAMETER_SUBSCRIBER_SELECTION );

        if ( arrayIds == null && request.getParameter( PARAMETER_ID_SUBSCRIBER ) != null )
        {
            arrayIds = new String [ ] {
                    request.getParameter( PARAMETER_ID_SUBSCRIBER )
            };
        }

        if ( arrayIds == null )
        {
            return listSubscribers;
        }

        for ( String strId : arrayIds )
        {
            Subscriber subscriber = findSubscriber( strId, listIdNewsletters );

            if ( subscriber != null )
            {
                listSubscribers.add( subscriber );
            }
        }

        return listSubscribers;
    }

    /**
     * Returns a subscriber the administrator may see: a subscriber having a subscription to one of the newsletters he may manage
     *
     * @param strIdSubscriber
     *            the identifier of the subscriber
     * @param listIdNewsletters
     *            the identifiers of the newsletters the administrator may manage
     * @return the subscriber, null when the identifier is invalid, matches no subscriber or a subscriber the administrator may not see
     */
    private Subscriber findSubscriber( String strIdSubscriber, List<Integer> listIdNewsletters )
    {
        int nIdSubscriber = parseId( strIdSubscriber );

        if ( nIdSubscriber < 0 || !_subscriberService.isSubscribedToOneOf( nIdSubscriber, listIdNewsletters ) )
        {
            return null;
        }

        return _subscriberService.findSubscriber( nIdSubscriber );
    }

    /**
     * Builds the list of the newsletters for a select
     *
     * @param listNewsletters
     *            the newsletters
     * @return the newsletters, by identifier
     */
    private ReferenceList buildNewsletterList( List<NewsLetter> listNewsletters )
    {
        ReferenceList refList = new ReferenceList( );

        for ( NewsLetter newsletter : listNewsletters )
        {
            refList.addItem( newsletter.getId( ), newsletter.getName( ) );
        }

        return refList;
    }

    /**
     * Builds the list of a filter select: the choice of no criterion followed by two values
     *
     * @param strFirstValue
     *            the first value
     * @param strFirstLabelKey
     *            the key of the label of the first value
     * @param strSecondValue
     *            the second value
     * @param strSecondLabelKey
     *            the key of the label of the second value
     * @return the list
     */
    private ReferenceList buildChoiceList( String strFirstValue, String strFirstLabelKey, String strSecondValue, String strSecondLabelKey )
    {
        ReferenceList refList = new ReferenceList( );
        refList.addItem( StringUtils.EMPTY, I18nService.getLocalizedString( PROPERTY_OPTION_ALL, getLocale( ) ) );
        refList.addItem( strFirstValue, I18nService.getLocalizedString( strFirstLabelKey, getLocale( ) ) );
        refList.addItem( strSecondValue, I18nService.getLocalizedString( strSecondLabelKey, getLocale( ) ) );

        return refList;
    }

    /**
     * Parses an identifier
     *
     * @param strId
     *            the identifier
     * @return the identifier, -1 when it is not a number
     */
    private static int parseId( String strId )
    {
        return StringUtils.isNumeric( strId ) ? Integer.parseInt( strId ) : -1;
    }
}
