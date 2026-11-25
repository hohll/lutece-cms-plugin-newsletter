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

/**
 * The figures of the subscribers of some newsletters: how many they are, how many are active or suspended, and how many have a subscription
 * awaiting their confirmation
 */
public class SubscriberStatistics
{
    private final int _nSubscribers;
    private final int _nActive;
    private final int _nSuspended;
    private final int _nPending;

    /**
     * Creates the figures of the subscribers
     *
     * @param nSubscribers
     *            the number of subscribers
     * @param nActive
     *            the number of active subscribers
     * @param nSuspended
     *            the number of suspended subscribers
     * @param nPending
     *            the number of subscribers having a subscription awaiting their confirmation
     */
    public SubscriberStatistics( int nSubscribers, int nActive, int nSuspended, int nPending )
    {
        _nSubscribers = nSubscribers;
        _nActive = nActive;
        _nSuspended = nSuspended;
        _nPending = nPending;
    }

    /**
     * Returns the number of subscribers
     *
     * @return the number of subscribers
     */
    public int getSubscribers( )
    {
        return _nSubscribers;
    }

    /**
     * Returns the number of active subscribers
     *
     * @return the number of active subscribers
     */
    public int getActive( )
    {
        return _nActive;
    }

    /**
     * Returns the number of suspended subscribers
     *
     * @return the number of suspended subscribers
     */
    public int getSuspended( )
    {
        return _nSuspended;
    }

    /**
     * Returns the number of subscribers having a subscription awaiting their confirmation
     *
     * @return the number of subscribers having a pending subscription
     */
    public int getPending( )
    {
        return _nPending;
    }
}
