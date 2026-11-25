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
package fr.paris.lutece.plugins.newsletter.business;

import fr.paris.lutece.portal.service.plugin.Plugin;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The interface representing the business methods of the subscriber
 */
public interface ISubscriberDAO
{
    /**
     * Insert a new record in the table.
     *
     * @param subscriber
     *            the object to be inserted
     * @param plugin
     *            the Plugin
     */
    void insert( Subscriber subscriber, Plugin plugin );

    /**
     * Delete a record from the table
     *
     * @param nId
     *            the subscriber's identifier
     * @param plugin
     *            the Plugin
     */
    void delete( int nId, Plugin plugin );

    /**
     * loads data from a subscriber's identifier
     *
     * @param nId
     *            the subscriber's identifier
     * @param plugin
     *            the Plugin
     * @return the subscriber, null when no subscriber has this identifier
     */
    Subscriber load( int nId, Plugin plugin );

    /**
     * Loads the list of subscribers
     *
     * @param plugin
     *            the Plugin
     * @return a collection of objects Subscriber
     */
    Collection<Subscriber> selectAll( Plugin plugin );

    /**
     * Finds a subscriber from his email
     *
     * @param strEmail
     *            the subscriber's email
     * @param plugin
     *            the Plugin
     * @return a subscriber object if it exists, null if not
     */
    Subscriber selectByEmail( String strEmail, Plugin plugin );

    /**
     * loads the list of subscribers for a newsletter
     *
     * @param nNewsLetterId
     *            the newsletter identifier
     * @param plugin
     *            the Plugin
     * @return a collection of subscribers
     */
    Collection<Subscriber> selectSubscribers( int nNewsLetterId, Plugin plugin );

    /**
     * loads the list of subscribers for a newsletter
     *
     * @param nNewsLetterId
     *            the newsletter identifier
     * @param strSearchString
     *            gets all the subscribers if null or empty and gets the subscribers whith an email containing this string otherwise
     * @param nBegin
     *            the rank of the first subscriber to return
     * @param nEnd
     *            the maximum number of suscribers to return
     * @param plugin
     *            the Plugin
     * @return a collection of subscribers
     */
    Collection<Subscriber> selectSubscribers( int nNewsLetterId, String strSearchString, int nBegin, int nEnd, Plugin plugin );

    /**
     * loads the list of subscribers for a newsletter
     *
     * @param nNewsLetterId
     *            the newsletter identifier
     * @param strSearchString
     *            gets all the subscribers if null or empty and gets the subscribers whith an email containing this string otherwise
     * @param nBegin
     *            the rank of the first subscriber to return
     * @param nEnd
     *            the maximum number of suscribers to return
     * @param plugin
     *            the Plugin
     * @return a collection of subscribers
     */
    Collection<Subscriber> selectSubscribersForSending( int nNewsLetterId, String strSearchString, int nBegin, int nEnd, Plugin plugin );

    /**
     * Returns, for a subscriber, the number of his subscriptions
     *
     * @param nSubscriberId
     *            the subscriber's identifier
     * @param plugin
     *            the Plugin
     * @return the number of subscriptions
     */
    int selectNewsLetters( int nSubscriberId, Plugin plugin );

    /**
     * loads the list of subscribers
     *
     * @param plugin
     *            the Plugin
     * @return a collection of subscribers
     */
    Collection<Subscriber> selectSubscribersList( Plugin plugin );

    /**
     * Changes the status of a subscriber, when it is not already the given one
     *
     * @param nIdSubscriber
     *            the subscriber's identifier
     * @param nStatus
     *            the new status
     * @param dateStatusUpdate
     *            the date of the change
     * @param plugin
     *            the Plugin
     * @return true if the status changed, false if the subscriber does not exist or already had this status
     */
    boolean updateStatus( int nIdSubscriber, int nStatus, Timestamp dateStatusUpdate, Plugin plugin );

    /**
     * Finds the identifiers of the subscribers matching a filter, sorted by email
     *
     * @param filter
     *            the filter
     * @param plugin
     *            the Plugin
     * @return the identifiers of the subscribers, empty when the filter allows no newsletter
     */
    List<Integer> selectIdsByFilter( SubscriberFilter filter, Plugin plugin );

    /**
     * Counts the subscribers of some newsletters: all of them, the active ones, the suspended ones and the ones having a subscription awaiting
     * their confirmation
     *
     * @param listIdNewsletters
     *            the identifiers of the newsletters
     * @param plugin
     *            the Plugin
     * @return the figures, all zero when the list of the newsletters is empty
     */
    SubscriberStatistics selectStatistics( List<Integer> listIdNewsletters, Plugin plugin );

    /**
     * Loads subscribers from their identifiers
     *
     * @param listIds
     *            the identifiers
     * @param plugin
     *            the Plugin
     * @return the subscribers, in the order of the identifiers
     */
    List<Subscriber> selectByIds( List<Integer> listIds, Plugin plugin );

    /**
     * Counts, for each subscriber, its subscriptions to some newsletters
     *
     * @param listIdSubscribers
     *            the identifiers of the subscribers
     * @param listIdNewsletters
     *            the identifiers of the newsletters taken into account
     * @param plugin
     *            the Plugin
     * @return for each subscriber having a subscription, the number of its subscriptions and the number of the confirmed ones
     */
    Map<Integer, Pair<Integer, Integer>> selectSubscriptionCounts( List<Integer> listIdSubscribers, List<Integer> listIdNewsletters, Plugin plugin );

    /**
     * Loads the subscriptions of a subscriber, sorted by newsletter name
     *
     * @param nIdSubscriber
     *            the subscriber's identifier
     * @param plugin
     *            the Plugin
     * @return the subscriptions
     */
    List<Subscription> selectSubscriptions( int nIdSubscriber, Plugin plugin );

    /**
     * Finds the newsletters a subscriber did not confirm his subscription to
     *
     * @param nIdSubscriber
     *            the subscriber's identifier
     * @param plugin
     *            the Plugin
     * @return the identifiers of the newsletters
     */
    List<Integer> selectPendingNewsletterIds( int nIdSubscriber, Plugin plugin );
}
