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

import java.sql.Timestamp;

import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.newsletter.business.AwaitingActivationHome;
import fr.paris.lutece.plugins.newsletter.business.NewsLetter;
import fr.paris.lutece.plugins.newsletter.business.NewsLetterHome;
import fr.paris.lutece.plugins.newsletter.business.Subscriber;
import fr.paris.lutece.plugins.newsletter.business.SubscriberHome;
import fr.paris.lutece.portal.service.message.SiteMessageException;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.test.LuteceTestCase;
import fr.paris.lutece.test.mocks.MockHttpServletRequest;
import jakarta.inject.Inject;

/**
 * The confirmation link of a subscriber does not confirm another subscriber
 */
public class NewsLetterRegistrationServiceTest extends LuteceTestCase
{
    private static final int KEY = 123456;

    @Inject
    private NewsLetterRegistrationService _service;

    @Test
    public void testConfirmationKeyOfAnotherSubscriber( )
    {
        Plugin plugin = PluginService.getPlugin( NewsletterPlugin.PLUGIN_NAME );
        Timestamp now = new Timestamp( System.currentTimeMillis( ) );

        NewsLetter newsletter = new NewsLetter( );
        newsletter.setName( "confirmation test" );
        newsletter.setDateLastSending( now );
        newsletter.setWorkgroup( "all" );
        newsletter.setUnsubscribe( "TRUE" );
        newsletter.setNbSections( 1 );
        NewsLetterHome.create( newsletter, plugin );

        Subscriber subscriberA = createSubscriber( "confirm-a@example.org", plugin );
        Subscriber subscriberB = createSubscriber( "confirm-b@example.org", plugin );
        NewsLetterHome.addSubscriber( newsletter.getId( ), subscriberA.getId( ), false, now, plugin );
        NewsLetterHome.addSubscriber( newsletter.getId( ), subscriberB.getId( ), false, now, plugin );
        AwaitingActivationHome.create( subscriberA.getId( ), KEY, plugin );

        try
        {
            // The link of A with the e-mail of B
            MockHttpServletRequest request = new MockHttpServletRequest( );
            request.setParameter( "email", subscriberB.getEmail( ) );
            request.setParameter( "user_id", String.valueOf( subscriberA.getId( ) ) );
            request.setParameter( "key", String.valueOf( KEY ) );
            request.setParameter( "newsletter_id", String.valueOf( newsletter.getId( ) ) );

            assertThrows( SiteMessageException.class, ( ) -> _service.doConfirmSubscribe( request ) );
            assertEquals( 0, NewsLetterHome.findNbrActiveSubscribers( newsletter.getId( ), plugin ) );
            assertTrue( AwaitingActivationHome.checkKey( subscriberA.getId( ), KEY, plugin ) );
        }
        finally
        {
            AwaitingActivationHome.remove( subscriberA.getId( ), KEY, plugin );
            NewsLetterHome.removeSubscriber( newsletter.getId( ), subscriberA.getId( ), plugin );
            NewsLetterHome.removeSubscriber( newsletter.getId( ), subscriberB.getId( ), plugin );
            SubscriberHome.remove( subscriberA.getId( ), plugin );
            SubscriberHome.remove( subscriberB.getId( ), plugin );
            NewsLetterHome.remove( newsletter.getId( ), plugin );
        }
    }

    private Subscriber createSubscriber( String strEmail, Plugin plugin )
    {
        Subscriber subscriber = new Subscriber( );
        subscriber.setEmail( strEmail );
        subscriber.setDateSubscription( new Timestamp( System.currentTimeMillis( ) ) );

        return SubscriberHome.create( subscriber, plugin );
    }
}
