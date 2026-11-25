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
 * The sendings of one newsletter, as read from the sending archive: how many there are, how many are recent, and when the last one was made
 */
public class SendingStatistics
{
    private final int _nSendings;
    private final int _nRecentSendings;
    private final Timestamp _dateLastSending;

    /**
     * Creates the statistics of a newsletter
     *
     * @param nSendings
     *            the number of sendings
     * @param nRecentSendings
     *            the number of recent sendings
     * @param dateLastSending
     *            the date of the last sending, null when the newsletter has never been sent
     */
    public SendingStatistics( int nSendings, int nRecentSendings, Timestamp dateLastSending )
    {
        _nSendings = nSendings;
        _nRecentSendings = nRecentSendings;
        _dateLastSending = ( dateLastSending != null ) ? new Timestamp( dateLastSending.getTime( ) ) : null;
    }

    /**
     * Returns the number of sendings
     *
     * @return the number of sendings
     */
    public int getSendings( )
    {
        return _nSendings;
    }

    /**
     * Returns the number of recent sendings
     *
     * @return the number of recent sendings
     */
    public int getRecentSendings( )
    {
        return _nRecentSendings;
    }

    /**
     * Returns the date of the last sending
     *
     * @return the date of the last sending, null when the newsletter has never been sent
     */
    public Timestamp getDateLastSending( )
    {
        return ( _dateLastSending != null ) ? new Timestamp( _dateLastSending.getTime( ) ) : null;
    }
}
