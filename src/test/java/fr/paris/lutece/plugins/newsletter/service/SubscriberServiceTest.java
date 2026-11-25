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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
import fr.paris.lutece.test.LuteceTestCase;
import jakarta.inject.Inject;

/**
 * Search, suspension and subscriptions of the subscribers across the newsletters
 */
public class SubscriberServiceTest extends LuteceTestCase
{
    private static final String EMAIL_SUFFIX = "@subscriber-service-test.example.org";
    private static final int VALIDATION_KEY = 4242;

    private Plugin _plugin;
    @Inject
    private SubscriberService _service;
    private NewsLetter _newsletterFirst;
    private NewsLetter _newsletterSecond;
    private Subscriber _subscriberPending;
    private Subscriber _subscriberConfirmed;
    private String _strPrefix;

    /**
     * Creates two newsletters: the first subscriber is confirmed on the first one and pending on the second one, the second subscriber is confirmed
     * on the first one
     *
     * @throws Exception
     *             if the test context cannot be set up
     */
    @BeforeEach
    @Override
    protected void setUp( ) throws Exception
    {
        super.setUp( );
        _plugin = PluginService.getPlugin( NewsletterPlugin.PLUGIN_NAME );
        _strPrefix = "sst" + System.nanoTime( );

        _newsletterFirst = createNewsletter( _strPrefix + " first" );
        _newsletterSecond = createNewsletter( _strPrefix + " second" );
        _subscriberPending = createSubscriber( _strPrefix + "-pending" + EMAIL_SUFFIX );
        _subscriberConfirmed = createSubscriber( _strPrefix + "-confirmed" + EMAIL_SUFFIX );

        Timestamp now = new Timestamp( System.currentTimeMillis( ) );
        NewsLetterHome.addSubscriber( _newsletterFirst.getId( ), _subscriberPending.getId( ), true, now, _plugin );
        NewsLetterHome.addSubscriber( _newsletterSecond.getId( ), _subscriberPending.getId( ), false, now, _plugin );
        NewsLetterHome.addSubscriber( _newsletterFirst.getId( ), _subscriberConfirmed.getId( ), true, now, _plugin );
    }

    /**
     * Removes the subscriptions, the subscribers and the newsletters of the test
     *
     * @throws Exception
     *             if the test context cannot be torn down
     */
    @AfterEach
    @Override
    protected void tearDown( ) throws Exception
    {
        for ( Subscriber subscriber : Arrays.asList( _subscriberPending, _subscriberConfirmed ) )
        {
            for ( NewsLetter newsletter : Arrays.asList( _newsletterFirst, _newsletterSecond ) )
            {
                NewsLetterHome.removeSubscriber( newsletter.getId( ), subscriber.getId( ), _plugin );
            }

            AwaitingActivationHome.removeByUser( subscriber.getId( ), _plugin );
            SubscriberHome.remove( subscriber.getId( ), _plugin );
        }

        NewsLetterHome.remove( _newsletterFirst.getId( ), _plugin );
        NewsLetterHome.remove( _newsletterSecond.getId( ), _plugin );
        super.tearDown( );
    }

    /**
     * The filter combines the newsletter, the state of the subscription, the email and the status, and never goes beyond the authorized
     * newsletters
     */
    @Test
    public void testFindSubscriberIds( )
    {
        SubscriberFilter filter = newFilter( _newsletterFirst.getId( ), _newsletterSecond.getId( ) );
        assertEquals( Arrays.asList( _subscriberConfirmed.getId( ), _subscriberPending.getId( ) ), _service.findSubscriberIds( filter ),
                "every subscriber of the test, sorted by email" );

        filter.setIdNewsletter( _newsletterSecond.getId( ) );
        filter.setSubscriptionState( SubscriberFilter.SUBSCRIPTION_PENDING );
        assertEquals( Collections.singletonList( _subscriberPending.getId( ) ), _service.findSubscriberIds( filter ), "pending on the second newsletter" );

        filter = newFilter( _newsletterFirst.getId( ), _newsletterSecond.getId( ) );
        filter.setEmail( "-confirmed" );
        assertEquals( Collections.singletonList( _subscriberConfirmed.getId( ) ), _service.findSubscriberIds( filter ), "part of the email" );

        filter = newFilter( _newsletterSecond.getId( ) );
        assertEquals( Collections.singletonList( _subscriberPending.getId( ) ), _service.findSubscriberIds( filter ),
                "only the subscribers of the authorized newsletters" );

        assertTrue( _service.findSubscriberIds( newFilter( ) ).isEmpty( ), "no authorized newsletter, no subscriber" );
    }

    /**
     * A suspended subscriber keeps his subscriptions, is found by the status filter and receives no newsletter
     */
    @Test
    public void testSuspendAndActivate( )
    {
        assertTrue( _service.suspend( _subscriberConfirmed.getId( ) ), "an active subscriber is suspended" );
        assertFalse( _service.suspend( _subscriberConfirmed.getId( ) ), "a suspended subscriber is not suspended twice" );
        assertTrue( _service.findSubscriber( _subscriberConfirmed.getId( ) ).isSuspended( ), "the status is stored" );

        SubscriberFilter filter = newFilter( _newsletterFirst.getId( ) );
        filter.setStatus( Subscriber.STATUS_SUSPENDED );
        assertEquals( Collections.singletonList( _subscriberConfirmed.getId( ) ), _service.findSubscriberIds( filter ), "status filter" );
        assertFalse( containsSubscriber( SubscriberHome.findSubscribersForSending( _newsletterFirst.getId( ), 0, 1000, _plugin ), _subscriberConfirmed ),
                "a suspended subscriber is not sent the newsletter" );
        assertEquals( 1, _service.getSubscriptions( _subscriberConfirmed.getId( ), Collections.singletonList( _newsletterFirst.getId( ) ) ).size( ),
                "a suspended subscriber keeps his subscriptions" );

        assertEquals( 1, NewsLetterHome.findNbrActiveSubscribers( _newsletterFirst.getId( ), _plugin ), "the count of the recipients leaves him out" );

        assertTrue( _service.activate( _subscriberConfirmed.getId( ) ), "a suspended subscriber is activated" );
        assertTrue( containsSubscriber( SubscriberHome.findSubscribersForSending( _newsletterFirst.getId( ), 0, 1000, _plugin ), _subscriberConfirmed ),
                "an active subscriber is sent the newsletter again" );
    }

    /**
     * Validating the last pending subscription confirms it and removes the validation keys of the subscriber
     */
    @Test
    public void testValidate( )
    {
        AwaitingActivationHome.create( _subscriberPending.getId( ), VALIDATION_KEY, _plugin );

        assertEquals( 0, _service.validate( _subscriberPending.getId( ), Collections.singletonList( _newsletterFirst.getId( ) ) ),
                "a confirmed subscription is not validated again" );
        assertTrue( AwaitingActivationHome.checkKey( _subscriberPending.getId( ), VALIDATION_KEY, _plugin ), "the key stays while a subscription is pending" );

        assertEquals( 1, _service.validate( _subscriberPending.getId( ), Arrays.asList( _newsletterFirst.getId( ), _newsletterSecond.getId( ) ) ),
                "the pending subscription is validated" );
        assertTrue( SubscriberHome.findPendingNewsletterIds( _subscriberPending.getId( ), _plugin ).isEmpty( ), "nothing is pending anymore" );
        assertFalse( AwaitingActivationHome.checkKey( _subscriberPending.getId( ), VALIDATION_KEY, _plugin ), "the key is removed" );
    }

    /**
     * Subscribing twice is refused, unsubscribing from the last newsletter removes the subscriber
     */
    @Test
    public void testSubscribeAndUnsubscribe( )
    {
        assertFalse( _service.subscribe( _subscriberConfirmed.getId( ), _newsletterFirst.getId( ), true ), "already subscribed" );
        assertTrue( _service.subscribe( _subscriberConfirmed.getId( ), _newsletterSecond.getId( ), true ), "subscribed to the second newsletter" );

        List<Subscription> listSubscriptions = _service.getSubscriptions( _subscriberConfirmed.getId( ),
                Arrays.asList( _newsletterFirst.getId( ), _newsletterSecond.getId( ) ) );
        assertEquals( 2, listSubscriptions.size( ), "two subscriptions" );
        assertTrue( listSubscriptions.stream( ).allMatch( Subscription::isConfirmed ), "both confirmed" );

        assertFalse( _service.unsubscribe( _subscriberConfirmed.getId( ), _newsletterFirst.getId( ) ), "a subscription is left" );
        assertTrue( _service.unsubscribe( _subscriberConfirmed.getId( ), _newsletterSecond.getId( ) ), "the last subscription is removed" );
        assertNull( _service.findSubscriber( _subscriberConfirmed.getId( ) ), "the subscriber without subscription is removed" );
    }

    /**
     * The status of a subscriber may be changed only by an administrator managing all the newsletters he is subscribed to
     */
    @Test
    public void testIsSubscribedOnlyTo( )
    {
        assertTrue( _service.isSubscribedOnlyTo( _subscriberPending.getId( ), Arrays.asList( _newsletterFirst.getId( ), _newsletterSecond.getId( ) ) ),
                "every newsletter of the subscriber is managed" );
        assertFalse( _service.isSubscribedOnlyTo( _subscriberPending.getId( ), Collections.singletonList( _newsletterFirst.getId( ) ) ),
                "the subscriber is also subscribed to a newsletter not managed" );
        assertTrue( _service.isSubscribedOnlyTo( _subscriberConfirmed.getId( ), Collections.singletonList( _newsletterFirst.getId( ) ) ),
                "the only newsletter of the subscriber is managed" );
    }

    /**
     * The confirmation email is not sent again for the pending subscriptions to newsletters the administrator does not manage, and the keys already
     * sent stay valid
     */
    @Test
    public void testResendConfirmationOutsideAuthorizedNewsletters( )
    {
        AwaitingActivationHome.create( _subscriberPending.getId( ), VALIDATION_KEY, _plugin );

        assertFalse( _service.resendConfirmation( _subscriberPending, Collections.singletonList( _newsletterFirst.getId( ) ), "http://localhost/", null ),
                "the only pending subscription is to a newsletter not managed" );
        assertTrue( AwaitingActivationHome.checkKey( _subscriberPending.getId( ), VALIDATION_KEY, _plugin ), "the key already sent stays valid" );
    }

    /**
     * The counts of the list take only the authorized newsletters into account
     */
    @Test
    public void testSubscriptionCounts( )
    {
        List<Subscriber> listSubscribers = Arrays.asList( _subscriberPending, _subscriberConfirmed );

        Map<Integer, Pair<Integer, Integer>> mapCounts = _service.getSubscriptionCounts( listSubscribers,
                Arrays.asList( _newsletterFirst.getId( ), _newsletterSecond.getId( ) ) );
        assertEquals( 2, mapCounts.get( _subscriberPending.getId( ) ).getFirst( ), "two subscriptions" );
        assertEquals( 1, mapCounts.get( _subscriberPending.getId( ) ).getSecond( ), "one of them confirmed" );

        mapCounts = _service.getSubscriptionCounts( listSubscribers, Collections.singletonList( _newsletterSecond.getId( ) ) );
        assertEquals( 1, mapCounts.get( _subscriberPending.getId( ) ).getFirst( ), "only the authorized newsletter" );
        assertFalse( mapCounts.containsKey( _subscriberConfirmed.getId( ) ), "no subscription to the authorized newsletter" );
    }

    /**
     * The figures count the subscribers of the newsletters given only, by status and by pending subscription
     */
    @Test
    public void testStatistics( )
    {
        List<Integer> listBoth = Arrays.asList( _newsletterFirst.getId( ), _newsletterSecond.getId( ) );

        SubscriberStatistics statistics = _service.getStatistics( listBoth );
        assertEquals( 2, statistics.getSubscribers( ), "two subscribers" );
        assertEquals( 2, statistics.getActive( ), "both active" );
        assertEquals( 0, statistics.getSuspended( ), "none suspended" );
        assertEquals( 1, statistics.getPending( ), "one with a pending subscription" );

        assertEquals( 0, _service.getStatistics( Collections.singletonList( _newsletterFirst.getId( ) ) ).getPending( ),
                "the pending subscription is to a newsletter not given" );

        _service.suspend( _subscriberConfirmed.getId( ) );
        statistics = _service.getStatistics( listBoth );
        assertEquals( 1, statistics.getActive( ), "one active" );
        assertEquals( 1, statistics.getSuspended( ), "one suspended" );

        assertEquals( 0, _service.getStatistics( Collections.emptyList( ) ).getSubscribers( ), "no newsletter, no subscriber" );
    }

    /**
     * A known email gives back its subscriber, an unknown email gives a new subscriber
     */
    @Test
    public void testFindOrCreateSubscriber( )
    {
        assertEquals( _subscriberConfirmed.getId( ), _service.findOrCreateSubscriber( _subscriberConfirmed.getEmail( ) ).getId( ), "the existing subscriber" );

        String strEmail = _strPrefix + "-created" + EMAIL_SUFFIX;
        Subscriber subscriber = _service.findOrCreateSubscriber( strEmail );

        try
        {
            assertTrue( subscriber.getId( ) > 0, "the subscriber has been created" );
            assertEquals( strEmail, SubscriberHome.findByPrimaryKey( subscriber.getId( ), _plugin ).getEmail( ), "with the email" );
        }
        finally
        {
            SubscriberHome.remove( subscriber.getId( ), _plugin );
        }
    }

    /**
     * Builds a filter restricted to some newsletters
     *
     * @param nIdNewsletters
     *            the identifiers of the authorized newsletters
     * @return the filter
     */
    private SubscriberFilter newFilter( Integer... nIdNewsletters )
    {
        SubscriberFilter filter = new SubscriberFilter( );
        filter.setAuthorizedNewsletterIds( Arrays.asList( nIdNewsletters ) );
        filter.setEmail( _strPrefix );

        return filter;
    }

    /**
     * Tells whether a collection holds a subscriber
     *
     * @param listSubscribers
     *            the collection
     * @param subscriber
     *            the subscriber
     * @return true if a subscriber of the collection has the same identifier
     */
    private static boolean containsSubscriber( java.util.Collection<Subscriber> listSubscribers, Subscriber subscriber )
    {
        return listSubscribers.stream( ).anyMatch( item -> item.getId( ) == subscriber.getId( ) );
    }

    /**
     * Creates a newsletter
     *
     * @param strName
     *            its name
     * @return the newsletter
     */
    private NewsLetter createNewsletter( String strName )
    {
        NewsLetter newsletter = new NewsLetter( );
        newsletter.setName( strName );
        newsletter.setDescription( strName );
        newsletter.setDateLastSending( new Timestamp( System.currentTimeMillis( ) ) );
        newsletter.setHtml( "<p>letter</p>" );
        newsletter.setNewsLetterTemplateId( 1 );
        newsletter.setWorkgroup( "all" );
        newsletter.setUnsubscribe( "TRUE" );
        newsletter.setNewsletterSenderMail( "test@example.org" );
        newsletter.setNewsletterSenderName( "test" );
        newsletter.setTestRecipients( "" );
        newsletter.setTestSubject( "" );
        newsletter.setNbSections( 1 );

        return NewsLetterHome.create( newsletter, _plugin );
    }

    /**
     * Creates a subscriber
     *
     * @param strEmail
     *            his email
     * @return the subscriber
     */
    private Subscriber createSubscriber( String strEmail )
    {
        Subscriber subscriber = new Subscriber( );
        subscriber.setEmail( strEmail );

        return SubscriberHome.create( subscriber, _plugin );
    }
}
