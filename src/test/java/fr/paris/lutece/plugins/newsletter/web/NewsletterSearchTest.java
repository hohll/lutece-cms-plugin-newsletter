/*
 * Copyright (c) 2002-2025, City of Paris
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.newsletter.business.NewsLetter;
import fr.paris.lutece.test.LuteceTestCase;

/**
 * The search of the list of the newsletters
 */
public class NewsletterSearchTest extends LuteceTestCase
{
    private static NewsLetter newsletter( String strName )
    {
        NewsLetter newsletter = new NewsLetter( );
        newsletter.setName( strName );
        return newsletter;
    }

    private static final Timestamp MARCH = Timestamp.valueOf( LocalDateTime.of( 2026, 3, 2, 10, 0 ) );
    private static final Timestamp APRIL = Timestamp.valueOf( LocalDateTime.of( 2026, 4, 6, 18, 30 ) );

    @Test
    public void testNoSearchKeepsEverything( )
    {
        assertTrue( NewsletterJspBean.matchesSearch( newsletter( "Brouillon" ), null, "", null ) );
        assertTrue( NewsletterJspBean.matchesSearch( newsletter( "Lettre de mars" ), MARCH, null, null ) );
    }

    @Test
    public void testSearchByName( )
    {
        assertTrue( NewsletterJspBean.matchesSearch( newsletter( "Lettre de mars" ), MARCH, "lettre", null ) );
        assertFalse( NewsletterJspBean.matchesSearch( newsletter( "Brouillon" ), null, "lettre", null ) );
    }

    @Test
    public void testSearchByDate( )
    {
        LocalDate april = LocalDate.of( 2026, 4, 6 );
        assertTrue( NewsletterJspBean.matchesSearch( newsletter( "Lettre d'avril" ), APRIL, null, april ) );
        assertFalse( NewsletterJspBean.matchesSearch( newsletter( "Lettre de mars" ), MARCH, null, april ) );
        assertFalse( NewsletterJspBean.matchesSearch( newsletter( "Brouillon" ), null, null, april ) );
        assertTrue( NewsletterJspBean.matchesSearch( newsletter( "Lettre d'avril" ), APRIL, null, LocalDate.of( 2026, 3, 2 ) ) );
        assertFalse( NewsletterJspBean.matchesSearch( newsletter( "Lettre d'avril" ), APRIL, "mars", LocalDate.of( 2026, 3, 2 ) ) );
    }
}
