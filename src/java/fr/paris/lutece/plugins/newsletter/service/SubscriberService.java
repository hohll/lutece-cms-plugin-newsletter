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
package fr.paris.lutece.plugins.newsletter.service;

import fr.paris.lutece.api.user.User;
import fr.paris.lutece.plugins.newsletter.business.AwaitingActivationHome;
import fr.paris.lutece.plugins.newsletter.business.NewsLetter;
import fr.paris.lutece.plugins.newsletter.business.NewsLetterHome;
import fr.paris.lutece.plugins.newsletter.business.Pair;
import fr.paris.lutece.plugins.newsletter.business.Subscriber;
import fr.paris.lutece.plugins.newsletter.business.SubscriberFilter;
import fr.paris.lutece.plugins.newsletter.business.SubscriberHome;
import fr.paris.lutece.plugins.newsletter.business.SubscriberStatistics;
import fr.paris.lutece.plugins.newsletter.business.Subscription;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.rbac.RBACService;
import fr.paris.lutece.portal.service.workgroup.AdminWorkgroupService;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Management of the subscribers across the newsletters: search, suspension and subscriptions. The subscriptions an administrator may manage are
 * those of the newsletters of his workgroups on which he holds the permission of managing the subscribers.
 */
@ApplicationScoped
public class SubscriberService
{
    @Inject
    private NewsLetterRegistrationService _registrationService;

    /**
     * Returns the newsletters a user may manage the subscribers of
     *
     * @param user
     *            the administrator
     * @return the newsletters of his workgroups on which he holds the permission of managing the subscribers
     */
    public List<NewsLetter> getAuthorizedNewsletters( User user )
    {
        Collection<NewsLetter> listNewsletters = AdminWorkgroupService.getAuthorizedCollection( NewsLetterHome.findAll( getPlugin( ) ), user );

        return listNewsletters.stream( ).filter( newsletter -> RBACService.isAuthorized( newsletter, NewsletterResourceIdService.PERMISSION_MANAGE_SUBSCRIBERS, user ) )
                .collect( Collectors.toList( ) );
    }

    /**
     * Returns the identifiers of a list of newsletters
     *
     * @param listNewsletters
     *            the newsletters
     * @return their identifiers
     */
    public List<Integer> getNewsletterIds( Collection<NewsLetter> listNewsletters )
    {
        return listNewsletters.stream( ).map( NewsLetter::getId ).collect( Collectors.toList( ) );
    }

    /**
     * Finds the identifiers of the subscribers matching a filter, sorted by email
     *
     * @param filter
     *            the filter, holding the newsletters the administrator may manage
     * @return the identifiers of the subscribers
     */
    public List<Integer> findSubscriberIds( SubscriberFilter filter )
    {
        return SubscriberHome.findIdsByFilter( filter, getPlugin( ) );
    }

    /**
     * Returns the figures of the subscribers of some newsletters
     *
     * @param listIdNewsletters
     *            the identifiers of the newsletters the administrator may manage
     * @return the number of subscribers, of active and suspended ones, and of ones having a subscription awaiting their confirmation
     */
    public SubscriberStatistics getStatistics( List<Integer> listIdNewsletters )
    {
        return SubscriberHome.findStatistics( listIdNewsletters, getPlugin( ) );
    }

    /**
     * Counts, for each subscriber, its subscriptions to some newsletters
     *
     * @param listSubscribers
     *            the subscribers
     * @param listIdNewsletters
     *            the identifiers of the newsletters taken into account
     * @return for each subscriber having a subscription, the number of its subscriptions and the number of the confirmed ones
     */
    public Map<Integer, Pair<Integer, Integer>> getSubscriptionCounts( List<Subscriber> listSubscribers, List<Integer> listIdNewsletters )
    {
        List<Integer> listIdSubscribers = listSubscribers.stream( ).map( Subscriber::getId ).collect( Collectors.toList( ) );

        return SubscriberHome.findSubscriptionCounts( listIdSubscribers, listIdNewsletters, getPlugin( ) );
    }

    /**
     * Returns a subscriber
     *
     * @param nIdSubscriber
     *            the subscriber's identifier
     * @return the subscriber, null when it does not exist
     */
    public Subscriber findSubscriber( int nIdSubscriber )
    {
        Subscriber subscriber = SubscriberHome.findByPrimaryKey( nIdSubscriber, getPlugin( ) );

        return ( subscriber == null || subscriber.getEmail( ) == null ) ? null : subscriber;
    }

    /**
     * Returns the subscriber of an email, created when no subscriber has this email yet
     *
     * @param strEmail
     *            the email, already checked as valid
     * @return the subscriber
     */
    public Subscriber findOrCreateSubscriber( String strEmail )
    {
        Subscriber subscriber = SubscriberHome.findByEmail( strEmail, getPlugin( ) );

        if ( subscriber == null )
        {
            subscriber = new Subscriber( );
            subscriber.setEmail( strEmail );
            subscriber = SubscriberHome.create( subscriber, getPlugin( ) );
        }

        return subscriber;
    }

    /**
     * Returns the subscriptions of a subscriber to some newsletters
     *
     * @param nIdSubscriber
     *            the subscriber's identifier
     * @param listIdNewsletters
     *            the identifiers of the newsletters taken into account
     * @return the subscriptions to these newsletters, sorted by newsletter name
     */
    public List<Subscription> getSubscriptions( int nIdSubscriber, List<Integer> listIdNewsletters )
    {
        return SubscriberHome.findSubscriptions( nIdSubscriber, getPlugin( ) ).stream( )
                .filter( subscription -> listIdNewsletters.contains( subscription.getIdNewsletter( ) ) ).collect( Collectors.toList( ) );
    }

    /**
     * Tells whether a subscriber has a subscription to one of some newsletters
     *
     * @param nIdSubscriber
     *            the subscriber's identifier
     * @param listIdNewsletters
     *            the identifiers of the newsletters
     * @return true if the subscriber has a subscription to one of the newsletters
     */
    public boolean isSubscribedToOneOf( int nIdSubscriber, List<Integer> listIdNewsletters )
    {
        return !getSubscriptions( nIdSubscriber, listIdNewsletters ).isEmpty( );
    }

    /**
     * Tells whether all the subscriptions of a subscriber are to some newsletters. The status of a subscriber applies to all his subscriptions: an
     * administrator changes it only when he manages all the newsletters the subscriber is subscribed to.
     *
     * @param nIdSubscriber
     *            the subscriber's identifier
     * @param listIdNewsletters
     *            the identifiers of the newsletters
     * @return true if every subscription of the subscriber is to one of the newsletters
     */
    public boolean isSubscribedOnlyTo( int nIdSubscriber, List<Integer> listIdNewsletters )
    {
        return SubscriberHome.findSubscriptions( nIdSubscriber, getPlugin( ) ).stream( )
                .allMatch( subscription -> listIdNewsletters.contains( subscription.getIdNewsletter( ) ) );
    }

    /**
     * Suspends a subscriber: he keeps his subscriptions but receives no newsletter
     *
     * @param nIdSubscriber
     *            the subscriber's identifier
     * @return true if the subscriber was active
     */
    public boolean suspend( int nIdSubscriber )
    {
        return SubscriberHome.updateStatus( nIdSubscriber, Subscriber.STATUS_SUSPENDED, getPlugin( ) );
    }

    /**
     * Activates a subscriber again: he receives the newsletters he subscribed to
     *
     * @param nIdSubscriber
     *            the subscriber's identifier
     * @return true if the subscriber was suspended
     */
    public boolean activate( int nIdSubscriber )
    {
        return SubscriberHome.updateStatus( nIdSubscriber, Subscriber.STATUS_ACTIVE, getPlugin( ) );
    }

    /**
     * Subscribes a subscriber to a newsletter
     *
     * @param nIdSubscriber
     *            the subscriber's identifier
     * @param nIdNewsletter
     *            the newsletter's identifier
     * @param bConfirmed
     *            true to register a confirmed subscription, false to register a subscription awaiting the confirmation of the subscriber
     * @return false if the subscriber was already subscribed to the newsletter
     */
    public boolean subscribe( int nIdSubscriber, int nIdNewsletter, boolean bConfirmed )
    {
        if ( NewsLetterHome.findRegistration( nIdNewsletter, nIdSubscriber, getPlugin( ) ) )
        {
            return false;
        }

        NewsLetterHome.addSubscriber( nIdNewsletter, nIdSubscriber, bConfirmed, new Timestamp( System.currentTimeMillis( ) ), getPlugin( ) );

        return true;
    }

    /**
     * Unsubscribes a subscriber from a newsletter. A subscriber without subscription left is removed, with his validation keys.
     *
     * @param nIdSubscriber
     *            the subscriber's identifier
     * @param nIdNewsletter
     *            the newsletter's identifier
     * @return true if the subscriber has been removed because he has no subscription left
     */
    public boolean unsubscribe( int nIdSubscriber, int nIdNewsletter )
    {
        NewsLetterHome.removeSubscriber( nIdNewsletter, nIdSubscriber, getPlugin( ) );

        if ( SubscriberHome.findNewsLetters( nIdSubscriber, getPlugin( ) ) == 0 )
        {
            AwaitingActivationHome.removeByUser( nIdSubscriber, getPlugin( ) );
            SubscriberHome.remove( nIdSubscriber, getPlugin( ) );

            return true;
        }

        if ( SubscriberHome.findPendingNewsletterIds( nIdSubscriber, getPlugin( ) ).isEmpty( ) )
        {
            AwaitingActivationHome.removeByUser( nIdSubscriber, getPlugin( ) );
        }

        return false;
    }

    /**
     * Confirms subscriptions in place of the subscriber. The validation keys of the subscriber are removed when nothing awaits his confirmation
     * anymore.
     *
     * @param nIdSubscriber
     *            the subscriber's identifier
     * @param listIdNewsletters
     *            the identifiers of the newsletters whose pending subscription is confirmed
     * @return the number of subscriptions confirmed
     */
    public int validate( int nIdSubscriber, List<Integer> listIdNewsletters )
    {
        List<Integer> listPending = SubscriberHome.findPendingNewsletterIds( nIdSubscriber, getPlugin( ) );
        int nCount = 0;

        for ( Integer nIdNewsletter : listIdNewsletters )
        {
            if ( listPending.remove( nIdNewsletter ) )
            {
                NewsLetterHome.validateSubscriber( nIdNewsletter, nIdSubscriber, getPlugin( ) );
                nCount++;
            }
        }

        if ( listPending.isEmpty( ) )
        {
            AwaitingActivationHome.removeByUser( nIdSubscriber, getPlugin( ) );
        }

        return nCount;
    }

    /**
     * Sends again to a subscriber the email asking him to confirm his pending subscriptions to some newsletters. A new validation key is generated;
     * the keys of the emails already sent stay valid.
     *
     * @param subscriber
     *            the subscriber
     * @param listIdNewsletters
     *            the identifiers of the newsletters taken into account
     * @param strBaseUrl
     *            the base url of the site, which the confirmation link points to
     * @param locale
     *            the locale of the email
     * @return false if no subscription of the subscriber to these newsletters awaits his confirmation
     */
    public boolean resendConfirmation( Subscriber subscriber, List<Integer> listIdNewsletters, String strBaseUrl, Locale locale )
    {
        List<String> listIds = new ArrayList<>( );

        for ( Integer nIdNewsletter : SubscriberHome.findPendingNewsletterIds( subscriber.getId( ), getPlugin( ) ) )
        {
            if ( listIdNewsletters.contains( nIdNewsletter ) )
            {
                listIds.add( Integer.toString( nIdNewsletter ) );
            }
        }

        if ( listIds.isEmpty( ) )
        {
            return false;
        }

        _registrationService.sendConfirmationMail( subscriber, listIds.toArray( new String [ 0] ), strBaseUrl, locale, locale );

        return true;
    }

    /**
     * Returns the newsletter plugin
     *
     * @return the plugin
     */
    private Plugin getPlugin( )
    {
        return PluginService.getPlugin( NewsletterPlugin.PLUGIN_NAME );
    }
}
