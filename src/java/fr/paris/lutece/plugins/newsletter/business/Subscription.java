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

import java.sql.Timestamp;

/**
 * The subscription of a subscriber to a newsletter
 */
public class Subscription
{
    private int _nIdNewsletter;
    private String _strNewsletterName;
    private boolean _bConfirmed;
    private Timestamp _dateSubscription;

    /**
     * Returns the identifier of the newsletter
     *
     * @return the identifier of the newsletter
     */
    public int getIdNewsletter( )
    {
        return _nIdNewsletter;
    }

    /**
     * Sets the identifier of the newsletter
     *
     * @param nIdNewsletter
     *            the identifier of the newsletter
     */
    public void setIdNewsletter( int nIdNewsletter )
    {
        _nIdNewsletter = nIdNewsletter;
    }

    /**
     * Returns the name of the newsletter
     *
     * @return the name of the newsletter
     */
    public String getNewsletterName( )
    {
        return _strNewsletterName;
    }

    /**
     * Sets the name of the newsletter
     *
     * @param strNewsletterName
     *            the name of the newsletter
     */
    public void setNewsletterName( String strNewsletterName )
    {
        _strNewsletterName = strNewsletterName;
    }

    /**
     * Tells whether the subscriber confirmed the subscription
     *
     * @return true if the subscription is confirmed, false if it awaits the confirmation
     */
    public boolean isConfirmed( )
    {
        return _bConfirmed;
    }

    /**
     * Sets whether the subscriber confirmed the subscription
     *
     * @param bConfirmed
     *            true if the subscription is confirmed, false if it awaits the confirmation
     */
    public void setConfirmed( boolean bConfirmed )
    {
        _bConfirmed = bConfirmed;
    }

    /**
     * Returns the date of the subscription
     *
     * @return the date of the subscription
     */
    public Timestamp getDateSubscription( )
    {
        return _dateSubscription;
    }

    /**
     * Sets the date of the subscription
     *
     * @param dateSubscription
     *            the date of the subscription
     */
    public void setDateSubscription( Timestamp dateSubscription )
    {
        _dateSubscription = dateSubscription;
    }
}
