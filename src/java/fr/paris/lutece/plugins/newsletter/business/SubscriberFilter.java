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
package fr.paris.lutece.plugins.newsletter.business;

import java.util.ArrayList;
import java.util.List;

/**
 * Criteria of a search of subscribers across the newsletters
 */
public class SubscriberFilter
{
    /**
     * No criterion on the status of the subscriber or on the state of its subscriptions
     */
    public static final int ALL = -1;

    /**
     * State of a subscription confirmed by the subscriber
     */
    public static final int SUBSCRIPTION_CONFIRMED = 1;

    /**
     * State of a subscription awaiting the confirmation of the subscriber
     */
    public static final int SUBSCRIPTION_PENDING = 0;

    private String _strEmail;
    private int _nIdNewsletter = ALL;
    private int _nStatus = ALL;
    private int _nSubscriptionState = ALL;
    private List<Integer> _listAuthorizedNewsletterIds = new ArrayList<>( );

    /**
     * Returns the part of the email searched
     *
     * @return the part of the email searched, null or empty for any email
     */
    public String getEmail( )
    {
        return _strEmail;
    }

    /**
     * Sets the part of the email searched
     *
     * @param strEmail
     *            the part of the email searched, null or empty for any email
     */
    public void setEmail( String strEmail )
    {
        _strEmail = strEmail;
    }

    /**
     * Returns the identifier of the newsletter the subscribers must be subscribed to
     *
     * @return the identifier of the newsletter, {@link #ALL} for any authorized newsletter
     */
    public int getIdNewsletter( )
    {
        return _nIdNewsletter;
    }

    /**
     * Sets the identifier of the newsletter the subscribers must be subscribed to
     *
     * @param nIdNewsletter
     *            the identifier of the newsletter, {@link #ALL} for any authorized newsletter
     */
    public void setIdNewsletter( int nIdNewsletter )
    {
        _nIdNewsletter = nIdNewsletter;
    }

    /**
     * Returns the status of the subscribers searched
     *
     * @return {@link Subscriber#STATUS_ACTIVE}, {@link Subscriber#STATUS_SUSPENDED} or {@link #ALL}
     */
    public int getStatus( )
    {
        return _nStatus;
    }

    /**
     * Sets the status of the subscribers searched
     *
     * @param nStatus
     *            {@link Subscriber#STATUS_ACTIVE}, {@link Subscriber#STATUS_SUSPENDED} or {@link #ALL}
     */
    public void setStatus( int nStatus )
    {
        _nStatus = nStatus;
    }

    /**
     * Returns the state of the subscriptions searched
     *
     * @return {@link #SUBSCRIPTION_CONFIRMED}, {@link #SUBSCRIPTION_PENDING} or {@link #ALL}
     */
    public int getSubscriptionState( )
    {
        return _nSubscriptionState;
    }

    /**
     * Sets the state of the subscriptions searched
     *
     * @param nSubscriptionState
     *            {@link #SUBSCRIPTION_CONFIRMED}, {@link #SUBSCRIPTION_PENDING} or {@link #ALL}
     */
    public void setSubscriptionState( int nSubscriptionState )
    {
        _nSubscriptionState = nSubscriptionState;
    }

    /**
     * Returns the identifiers of the newsletters the administrator may manage the subscribers of
     *
     * @return the identifiers of the authorized newsletters
     */
    public List<Integer> getAuthorizedNewsletterIds( )
    {
        return _listAuthorizedNewsletterIds;
    }

    /**
     * Sets the identifiers of the newsletters the administrator may manage the subscribers of
     *
     * @param listAuthorizedNewsletterIds
     *            the identifiers of the authorized newsletters
     */
    public void setAuthorizedNewsletterIds( List<Integer> listAuthorizedNewsletterIds )
    {
        _listAuthorizedNewsletterIds = listAuthorizedNewsletterIds;
    }
}
