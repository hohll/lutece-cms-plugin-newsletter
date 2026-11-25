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

import java.sql.Timestamp;

/**
 * This class represents business objects Subscriber
 */
public class Subscriber
{
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    /**
     * Status of a subscriber who receives the newsletters he subscribed to
     */
    public static final int STATUS_ACTIVE = 1;

    /**
     * Status of a suspended subscriber: he keeps his subscriptions but receives no newsletter
     */
    public static final int STATUS_SUSPENDED = 0;

    /**
     * The RBAC resource type of the management of the subscribers of all the newsletters
     */
    public static final String RESOURCE_TYPE = "NEWSLETTER_SUBSCRIBER";

    private boolean _bConfirmed;
    private int _nId;
    private String _strEmail;
    private Timestamp _dDateSubscription;
    private int _nStatus = STATUS_ACTIVE;
    private Timestamp _dateStatusUpdate;

    /**
     * Returns the subscriber's identifier
     *
     * @return the subscriber's identifier
     */
    public int getId( )
    {
        return _nId;
    }

    /**
     * Sets the subscriber's identifier
     *
     * @param nId
     *            the subscriber's identifier
     */
    public void setId( int nId )
    {
        _nId = nId;
    }

    /**
     * Returns the subscriber's email
     *
     * @return the subscriber's email
     */
    public String getEmail( )
    {
        return _strEmail;
    }

    /**
     * Sets the subscriber's email (in lower case )
     *
     * @param strEmail
     *            the subscriber's email
     */
    public void setEmail( String strEmail )
    {
        _strEmail = strEmail.toLowerCase( );
    }

    /**
     * Returns the date of the subscription
     *
     * @return the subscription's date
     */
    public Timestamp getDateSubscription( )
    {
        return _dDateSubscription;
    }

    /**
     * Sets the subscription's date
     *
     * @param dDateSubscription
     *            the subscription's date
     */
    public void setDateSubscription( Timestamp dDateSubscription )
    {
        _dDateSubscription = dDateSubscription;
    }

    /**
     * Return <b>true</b> if user confirmed his subscription.
     * 
     * @return <b>true</b> if user confirmed his subscription, <b>false</b> otherwise
     */
    public boolean getConfirmed( )
    {
        return _bConfirmed;
    }

    /**
     * Set to <b>true</b> if user confirmed his subscription
     * 
     * @param bConfirmed
     *            the new confirmed value.
     */
    public void setConfirmed( boolean bConfirmed )
    {
        _bConfirmed = bConfirmed;
    }

    /**
     * Returns the status of the subscriber
     *
     * @return {@link #STATUS_ACTIVE} or {@link #STATUS_SUSPENDED}
     */
    public int getStatus( )
    {
        return _nStatus;
    }

    /**
     * Sets the status of the subscriber
     *
     * @param nStatus
     *            {@link #STATUS_ACTIVE} or {@link #STATUS_SUSPENDED}
     */
    public void setStatus( int nStatus )
    {
        _nStatus = nStatus;
    }

    /**
     * Tells whether the subscriber is suspended
     *
     * @return <b>true</b> if the subscriber is suspended, <b>false</b> otherwise
     */
    public boolean isSuspended( )
    {
        return _nStatus == STATUS_SUSPENDED;
    }

    /**
     * Returns the date of the last change of the status
     *
     * @return the date of the last change of the status, null if the status never changed
     */
    public Timestamp getDateStatusUpdate( )
    {
        return _dateStatusUpdate;
    }

    /**
     * Sets the date of the last change of the status
     *
     * @param dateStatusUpdate
     *            the date of the last change of the status
     */
    public void setDateStatusUpdate( Timestamp dateStatusUpdate )
    {
        _dateStatusUpdate = dateStatusUpdate;
    }
}
