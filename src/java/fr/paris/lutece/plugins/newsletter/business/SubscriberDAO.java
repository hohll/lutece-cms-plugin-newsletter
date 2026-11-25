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
import fr.paris.lutece.util.sql.DAOUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * This class provides Data Access methods for subscriber objects
 */
@ApplicationScoped
@Named( "newsletter.subscriberDAO" )
public class SubscriberDAO implements ISubscriberDAO
{
    private static final String SQL_QUERY_INSERT = "INSERT INTO newsletter_subscriber_details ( id_subscriber , email ) VALUES ( ?, ? )";
    private static final String SQL_QUERY_DELETE = "DELETE FROM newsletter_subscriber_details WHERE id_subscriber = ? ";
    private static final String SQL_QUERY_SELECT = "SELECT email, status, date_status_update FROM newsletter_subscriber_details WHERE id_subscriber = ? ";
    private static final String SQL_QUERY_SELECT_ALL = "SELECT id_subscriber, email, status, date_status_update FROM newsletter_subscriber_details ";
    private static final String SQL_QUERY_SELECT_SUBSCRIBERS_LIST = "SELECT id_subscriber , email FROM newsletter_subscriber_details ";
    private static final String SQL_QUERY_SELECT_BY_EMAIL = "SELECT id_subscriber , email, status, date_status_update FROM newsletter_subscriber_details WHERE email = ? ";
    private static final String SQL_QUERY_SELECT_SUBSCRIBERS_BY_NEWSLETTER = "SELECT a.id_subscriber , a.email, b.confirmed, b.date_subscription FROM newsletter_subscriber_details a, newsletter_subscriber b WHERE a.id_subscriber = b.id_subscriber AND b.id_newsletter = ? AND b.confirmed = 1 AND a.status = 1 ";
    private static final String SQL_QUERY_SELECT_SUBSCRIBERS_FOR_SENDING = " SELECT a.id_subscriber , a.email, b.confirmed, b.date_subscription FROM newsletter_subscriber_details a, newsletter_subscriber b WHERE a.id_subscriber = b.id_subscriber AND b.id_newsletter = ? AND a.email LIKE ? AND confirmed = 1 AND a.status = 1 ORDER BY a.email LIMIT ? OFFSET ? ";
    private static final String SQL_QUERY_SELECT_SUBSCRIBERS = " SELECT a.id_subscriber , a.email, b.confirmed, b.date_subscription, a.status FROM newsletter_subscriber_details a, newsletter_subscriber b WHERE a.id_subscriber = b.id_subscriber AND b.id_newsletter = ? AND a.email LIKE ? ORDER BY a.email LIMIT ? OFFSET ? ";
    private static final String SQL_QUERY_COUNT_NEWSLETTERS_BY_SUBSCRIBER = "SELECT count(*) FROM newsletter_subscriber where id_subscriber = ? ";
    private static final String SQL_QUERY_CHECK_PRIMARY_KEY = "SELECT id_subscriber FROM newsletter_subscriber_details WHERE id_subscriber = ?";
    private static final String SQL_QUERY_NEW_PRIMARY_KEY = "SELECT max(id_subscriber) FROM newsletter_subscriber_details ";
    private static final String SQL_QUERY_UPDATE_STATUS = "UPDATE newsletter_subscriber_details SET status = ?, date_status_update = ? WHERE id_subscriber = ? AND status <> ? ";
    private static final String SQL_QUERY_SELECT_IDS_BY_FILTER = "SELECT a.id_subscriber FROM newsletter_subscriber_details a WHERE EXISTS ( SELECT 1 FROM newsletter_subscriber b WHERE b.id_subscriber = a.id_subscriber AND b.id_newsletter IN ( {0} ) ";
    private static final String SQL_FILTER_NEWSLETTER = " AND b.id_newsletter = ? ";
    private static final String SQL_FILTER_CONFIRMED = " AND b.confirmed = ? ";
    private static final String SQL_FILTER_END_SUBSCRIPTION = " ) ";
    private static final String SQL_FILTER_EMAIL = " AND a.email LIKE ? ";
    private static final String SQL_FILTER_STATUS = " AND a.status = ? ";
    private static final String SQL_ORDER_BY_EMAIL = " ORDER BY a.email ";
    private static final String SQL_QUERY_SELECT_STATISTICS = "SELECT COUNT(*), COALESCE( SUM( CASE WHEN a.status = ? THEN 1 ELSE 0 END ), 0 ), COALESCE( SUM( CASE WHEN a.status = ? THEN 1 ELSE 0 END ), 0 ), "
            + " COALESCE( SUM( CASE WHEN EXISTS ( SELECT 1 FROM newsletter_subscriber c WHERE c.id_subscriber = a.id_subscriber AND c.confirmed = ? AND c.id_newsletter IN ( {0} ) ) THEN 1 ELSE 0 END ), 0 ) "
            + " FROM newsletter_subscriber_details a WHERE EXISTS ( SELECT 1 FROM newsletter_subscriber b WHERE b.id_subscriber = a.id_subscriber AND b.id_newsletter IN ( {0} ) ) ";
    private static final String SQL_QUERY_SELECT_BY_IDS = "SELECT a.id_subscriber, a.email, a.status, a.date_status_update FROM newsletter_subscriber_details a WHERE a.id_subscriber IN ( {0} ) ";
    private static final String SQL_QUERY_COUNT_SUBSCRIPTIONS_BY_SUBSCRIBERS = "SELECT id_subscriber, COUNT(*), SUM( CASE WHEN confirmed = 1 THEN 1 ELSE 0 END ) FROM newsletter_subscriber WHERE id_subscriber IN ( {0} ) AND id_newsletter IN ( {1} ) GROUP BY id_subscriber ";
    private static final String SQL_QUERY_SELECT_SUBSCRIPTIONS = "SELECT b.id_newsletter, n.name, b.confirmed, b.date_subscription FROM newsletter_subscriber b, newsletter_description n WHERE b.id_newsletter = n.id_newsletter AND b.id_subscriber = ? ORDER BY n.name ";
    private static final String SQL_QUERY_SELECT_PENDING_NEWSLETTER_IDS = "SELECT id_newsletter FROM newsletter_subscriber WHERE id_subscriber = ? AND confirmed = 0 ";
    private static final String SQL_PARAMETER = "?";
    private static final String SQL_PARAMETER_SEPARATOR = ",";

    /**
     * Insert a new record in the table.
     *
     * @param subscriber
     *            the object to be inserted
     * @param plugin
     *            the Plugin
     */
    public void insert( Subscriber subscriber, Plugin plugin )
    {
        int nNewPrimaryKey = newPrimaryKey( plugin );
        subscriber.setId( nNewPrimaryKey );

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_INSERT, plugin ) )
        {
            daoUtil.setInt( 1, subscriber.getId( ) );
            daoUtil.setString( 2, subscriber.getEmail( ) );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * Delete a record from the table
     *
     * @param nId
     *            the subscriber's identifier
     * @param plugin
     *            the Plugin
     */
    public void delete( int nId, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_DELETE, plugin ) )
        {
            daoUtil.setInt( 1, nId );
            daoUtil.executeUpdate( );
        }
    }

    /**
     * loads data from a subscriber's identifier
     *
     * @param nId
     *            the subscriber's identifier
     * @param plugin
     *            the Plugin
     * @return an object Subscriber
     */
    public Subscriber load( int nId, Plugin plugin )
    {
        Subscriber subscriber = null;

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT, plugin ) )
        {
            daoUtil.setInt( 1, nId );
            daoUtil.executeQuery( );

            if ( daoUtil.next( ) )
            {
                subscriber = new Subscriber( );
                subscriber.setId( nId );
                subscriber.setEmail( daoUtil.getString( 1 ) );
                subscriber.setStatus( daoUtil.getInt( 2 ) );
                subscriber.setDateStatusUpdate( daoUtil.getTimestamp( 3 ) );
            }
        }

        return subscriber;
    }

    /**
     * Check the unicity of a primary key
     *
     * @param nKey
     *            the primary key to be checked
     * @param plugin
     *            the Plugin
     * @return true if the key exists, false if not
     */
    boolean checkPrimaryKey( int nKey, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_CHECK_PRIMARY_KEY, plugin ) )
        {
            daoUtil.setInt( 1, nKey );
            daoUtil.executeQuery( );

            return daoUtil.next( );
        }
    }

    /**
     * Generates a new primary key
     *
     * @param plugin
     *            the Plugin
     * @return the new primary key
     */
    int newPrimaryKey( Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_NEW_PRIMARY_KEY, plugin ) )
        {
            daoUtil.executeQuery( );

            int nKey = 1;
            if ( daoUtil.next( ) )
            {
                nKey = daoUtil.getInt( 1 ) + 1;
            }
            return nKey;
        }
    }

    /**
     * Loads the list of subscribers
     *
     * @param plugin
     *            the Plugin
     * @return a collection of objects Subscriber
     */
    public Collection<Subscriber> selectAll( Plugin plugin )
    {
        ArrayList<Subscriber> list = new ArrayList<>( );

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_ALL, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                Subscriber subscriber = new Subscriber( );
                subscriber.setId( daoUtil.getInt( 1 ) );
                subscriber.setEmail( daoUtil.getString( 2 ) );
                subscriber.setStatus( daoUtil.getInt( 3 ) );
                subscriber.setDateStatusUpdate( daoUtil.getTimestamp( 4 ) );
                list.add( subscriber );
            }
        }

        return list;
    }

    /**
     * Finds a subscriber from his email
     *
     * @param strEmail
     *            the subscriber's email
     * @param plugin
     *            the Plugin
     * @return a subscriber object if it exists, null if not
     */
    public Subscriber selectByEmail( String strEmail, Plugin plugin )
    {
        Subscriber subscriber = null;

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_EMAIL, plugin ) )
        {
            daoUtil.setString( 1, strEmail.toLowerCase( ) );
            daoUtil.executeQuery( );

            if ( daoUtil.next( ) )
            {
                subscriber = new Subscriber( );
                subscriber.setId( daoUtil.getInt( 1 ) );
                subscriber.setEmail( daoUtil.getString( 2 ) );
                subscriber.setStatus( daoUtil.getInt( 3 ) );
                subscriber.setDateStatusUpdate( daoUtil.getTimestamp( 4 ) );
            }
        }

        return subscriber;
    }

    /**
     * loads the list of subscribers for a newsletter
     *
     * @param nNewsLetterId
     *            the newsletter identifier
     * @param plugin
     *            the Plugin
     * @return a collection of subscribers
     */
    public Collection<Subscriber> selectSubscribers( int nNewsLetterId, Plugin plugin )
    {
        ArrayList<Subscriber> list = new ArrayList<>( );

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_SUBSCRIBERS_BY_NEWSLETTER, plugin ) )
        {
            daoUtil.setInt( 1, nNewsLetterId );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                Subscriber subscriber = new Subscriber( );
                subscriber.setId( daoUtil.getInt( 1 ) );
                subscriber.setEmail( daoUtil.getString( 2 ) );
                subscriber.setConfirmed( daoUtil.getBoolean( 3 ) );
                subscriber.setDateSubscription( daoUtil.getTimestamp( 4 ) );
                list.add( subscriber );
            }
        }

        return list;
    }

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
    public Collection<Subscriber> selectSubscribers( int nNewsLetterId, String strSearchString, int nBegin, int nEnd, Plugin plugin )
    {
        ArrayList<Subscriber> list = new ArrayList<>( );

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_SUBSCRIBERS, plugin ) )
        {
            daoUtil.setInt( 1, nNewsLetterId );
            daoUtil.setString( 2, "%" + strSearchString + "%" );
            daoUtil.setInt( 3, nEnd );
            daoUtil.setInt( 4, nBegin );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                Subscriber subscriber = new Subscriber( );
                subscriber.setId( daoUtil.getInt( 1 ) );
                subscriber.setEmail( daoUtil.getString( 2 ) );
                subscriber.setConfirmed( daoUtil.getBoolean( 3 ) );
                subscriber.setDateSubscription( daoUtil.getTimestamp( 4 ) );
                subscriber.setStatus( daoUtil.getInt( 5 ) );
                list.add( subscriber );
            }
        }

        return list;
    }

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
    public Collection<Subscriber> selectSubscribersForSending( int nNewsLetterId, String strSearchString, int nBegin, int nEnd, Plugin plugin )
    {
        ArrayList<Subscriber> list = new ArrayList<>( );

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_SUBSCRIBERS_FOR_SENDING, plugin ) )
        {
            daoUtil.setInt( 1, nNewsLetterId );
            daoUtil.setString( 2, "%" + strSearchString + "%" );
            daoUtil.setInt( 3, nEnd );
            daoUtil.setInt( 4, nBegin );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                Subscriber subscriber = new Subscriber( );
                subscriber.setId( daoUtil.getInt( 1 ) );
                subscriber.setEmail( daoUtil.getString( 2 ) );
                subscriber.setConfirmed( daoUtil.getBoolean( 3 ) );
                subscriber.setDateSubscription( daoUtil.getTimestamp( 4 ) );
                list.add( subscriber );
            }
        }

        return list;
    }

    /**
     * Returns, for a subscriber, the number of his subscriptions
     *
     * @param nSubscriberId
     *            the subscriber's identifier
     * @param plugin
     *            the Plugin
     * @return the number of subscriptions
     */
    public int selectNewsLetters( int nSubscriberId, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_COUNT_NEWSLETTERS_BY_SUBSCRIBER, plugin ) )
        {
            daoUtil.setInt( 1, nSubscriberId );
            daoUtil.executeQuery( );

            if ( daoUtil.next( ) )
            {
                return daoUtil.getInt( 1 );
            }
            return 0;
        }
    }

    /**
     * loads the list of subscribers
     *
     * @param plugin
     *            the Plugin
     * @return a collection of subscribers
     */
    public Collection<Subscriber> selectSubscribersList( Plugin plugin )
    {
        ArrayList<Subscriber> list = new ArrayList<>( );

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_SUBSCRIBERS_LIST, plugin ) )
        {
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                Subscriber subscriber = new Subscriber( );
                subscriber.setId( daoUtil.getInt( 1 ) );
                subscriber.setEmail( daoUtil.getString( 2 ) );
                list.add( subscriber );
            }
        }

        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateStatus( int nIdSubscriber, int nStatus, Timestamp dateStatusUpdate, Plugin plugin )
    {
        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_UPDATE_STATUS, plugin ) )
        {
            int nIndex = 1;
            daoUtil.setInt( nIndex++, nStatus );
            daoUtil.setTimestamp( nIndex++, dateStatusUpdate );
            daoUtil.setInt( nIndex++, nIdSubscriber );
            daoUtil.setInt( nIndex, nStatus );
            daoUtil.executeUpdate( );

            return daoUtil.getReturnedRowCount( ) > 0;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SubscriberStatistics selectStatistics( List<Integer> listIdNewsletters, Plugin plugin )
    {
        if ( listIdNewsletters.isEmpty( ) )
        {
            return new SubscriberStatistics( 0, 0, 0, 0 );
        }

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_STATISTICS.replace( "{0}", buildParameters( listIdNewsletters.size( ) ) ), plugin ) )
        {
            int nIndex = 1;
            daoUtil.setInt( nIndex++, Subscriber.STATUS_ACTIVE );
            daoUtil.setInt( nIndex++, Subscriber.STATUS_SUSPENDED );
            daoUtil.setInt( nIndex++, SubscriberFilter.SUBSCRIPTION_PENDING );

            for ( Integer nIdNewsletter : listIdNewsletters )
            {
                daoUtil.setInt( nIndex++, nIdNewsletter );
            }

            for ( Integer nIdNewsletter : listIdNewsletters )
            {
                daoUtil.setInt( nIndex++, nIdNewsletter );
            }

            daoUtil.executeQuery( );

            if ( daoUtil.next( ) )
            {
                return new SubscriberStatistics( daoUtil.getInt( 1 ), daoUtil.getInt( 2 ), daoUtil.getInt( 3 ), daoUtil.getInt( 4 ) );
            }

            return new SubscriberStatistics( 0, 0, 0, 0 );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> selectIdsByFilter( SubscriberFilter filter, Plugin plugin )
    {
        List<Integer> listIds = new ArrayList<>( );

        if ( filter.getAuthorizedNewsletterIds( ).isEmpty( ) )
        {
            return listIds;
        }

        StringBuilder sbSql = new StringBuilder( SQL_QUERY_SELECT_IDS_BY_FILTER.replace( "{0}", buildParameters( filter.getAuthorizedNewsletterIds( ).size( ) ) ) );

        if ( filter.getIdNewsletter( ) != SubscriberFilter.ALL )
        {
            sbSql.append( SQL_FILTER_NEWSLETTER );
        }

        if ( filter.getSubscriptionState( ) != SubscriberFilter.ALL )
        {
            sbSql.append( SQL_FILTER_CONFIRMED );
        }

        sbSql.append( SQL_FILTER_END_SUBSCRIPTION );

        if ( StringUtils.isNotBlank( filter.getEmail( ) ) )
        {
            sbSql.append( SQL_FILTER_EMAIL );
        }

        if ( filter.getStatus( ) != SubscriberFilter.ALL )
        {
            sbSql.append( SQL_FILTER_STATUS );
        }

        sbSql.append( SQL_ORDER_BY_EMAIL );

        try ( DAOUtil daoUtil = new DAOUtil( sbSql.toString( ), plugin ) )
        {
            int nIndex = 1;

            for ( Integer nIdNewsletter : filter.getAuthorizedNewsletterIds( ) )
            {
                daoUtil.setInt( nIndex++, nIdNewsletter );
            }

            if ( filter.getIdNewsletter( ) != SubscriberFilter.ALL )
            {
                daoUtil.setInt( nIndex++, filter.getIdNewsletter( ) );
            }

            if ( filter.getSubscriptionState( ) != SubscriberFilter.ALL )
            {
                daoUtil.setInt( nIndex++, filter.getSubscriptionState( ) );
            }

            if ( StringUtils.isNotBlank( filter.getEmail( ) ) )
            {
                daoUtil.setString( nIndex++, "%" + filter.getEmail( ).trim( ) + "%" );
            }

            if ( filter.getStatus( ) != SubscriberFilter.ALL )
            {
                daoUtil.setInt( nIndex, filter.getStatus( ) );
            }

            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                listIds.add( daoUtil.getInt( 1 ) );
            }
        }

        return listIds;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Subscriber> selectByIds( List<Integer> listIds, Plugin plugin )
    {
        Map<Integer, Subscriber> mapSubscribers = new HashMap<>( );

        if ( listIds.isEmpty( ) )
        {
            return new ArrayList<>( );
        }

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_BY_IDS.replace( "{0}", buildParameters( listIds.size( ) ) ), plugin ) )
        {
            int nIndex = 1;

            for ( Integer nId : listIds )
            {
                daoUtil.setInt( nIndex++, nId );
            }

            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                Subscriber subscriber = new Subscriber( );
                subscriber.setId( daoUtil.getInt( 1 ) );
                subscriber.setEmail( daoUtil.getString( 2 ) );
                subscriber.setStatus( daoUtil.getInt( 3 ) );
                subscriber.setDateStatusUpdate( daoUtil.getTimestamp( 4 ) );
                mapSubscribers.put( subscriber.getId( ), subscriber );
            }
        }

        List<Subscriber> listSubscribers = new ArrayList<>( );

        for ( Integer nId : listIds )
        {
            Subscriber subscriber = mapSubscribers.get( nId );

            if ( subscriber != null )
            {
                listSubscribers.add( subscriber );
            }
        }

        return listSubscribers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Integer, Pair<Integer, Integer>> selectSubscriptionCounts( List<Integer> listIdSubscribers, List<Integer> listIdNewsletters, Plugin plugin )
    {
        Map<Integer, Pair<Integer, Integer>> mapCounts = new HashMap<>( );

        if ( listIdSubscribers.isEmpty( ) || listIdNewsletters.isEmpty( ) )
        {
            return mapCounts;
        }

        String strSql = SQL_QUERY_COUNT_SUBSCRIPTIONS_BY_SUBSCRIBERS.replace( "{0}", buildParameters( listIdSubscribers.size( ) ) ).replace( "{1}",
                buildParameters( listIdNewsletters.size( ) ) );

        try ( DAOUtil daoUtil = new DAOUtil( strSql, plugin ) )
        {
            int nIndex = 1;

            for ( Integer nId : listIdSubscribers )
            {
                daoUtil.setInt( nIndex++, nId );
            }

            for ( Integer nId : listIdNewsletters )
            {
                daoUtil.setInt( nIndex++, nId );
            }

            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                mapCounts.put( daoUtil.getInt( 1 ), new Pair<>( daoUtil.getInt( 2 ), daoUtil.getInt( 3 ) ) );
            }
        }

        return mapCounts;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Subscription> selectSubscriptions( int nIdSubscriber, Plugin plugin )
    {
        List<Subscription> listSubscriptions = new ArrayList<>( );

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_SUBSCRIPTIONS, plugin ) )
        {
            daoUtil.setInt( 1, nIdSubscriber );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                Subscription subscription = new Subscription( );
                subscription.setIdNewsletter( daoUtil.getInt( 1 ) );
                subscription.setNewsletterName( daoUtil.getString( 2 ) );
                subscription.setConfirmed( daoUtil.getInt( 3 ) == SubscriberFilter.SUBSCRIPTION_CONFIRMED );
                subscription.setDateSubscription( daoUtil.getTimestamp( 4 ) );
                listSubscriptions.add( subscription );
            }
        }

        return listSubscriptions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> selectPendingNewsletterIds( int nIdSubscriber, Plugin plugin )
    {
        List<Integer> listIds = new ArrayList<>( );

        try ( DAOUtil daoUtil = new DAOUtil( SQL_QUERY_SELECT_PENDING_NEWSLETTER_IDS, plugin ) )
        {
            daoUtil.setInt( 1, nIdSubscriber );
            daoUtil.executeQuery( );

            while ( daoUtil.next( ) )
            {
                listIds.add( daoUtil.getInt( 1 ) );
            }
        }

        return listIds;
    }

    /**
     * Builds the list of the placeholders of a SQL IN clause
     *
     * @param nCount
     *            the number of placeholders, at least 1
     * @return the placeholders separated by commas
     */
    private static String buildParameters( int nCount )
    {
        return String.join( SQL_PARAMETER_SEPARATOR, Collections.nCopies( nCount, SQL_PARAMETER ) );
    }
}
