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
package fr.paris.lutece.plugins.newsletter.web.dashboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.http.HttpServletRequest;

import fr.paris.lutece.api.user.User;
import fr.paris.lutece.plugins.newsletter.business.Subscriber;
import fr.paris.lutece.plugins.newsletter.business.SubscriberFilter;
import fr.paris.lutece.plugins.newsletter.service.SubscriberService;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.dashboard.DashboardComponent;
import fr.paris.lutece.portal.service.template.AppTemplateService;

/**
 * Dashboard component notifying the administrators managing the subscribers of the number of active (not suspended) subscribers having a
 * subscription awaiting their confirmation. Only the newsletters of the administrator's workgroups on which he holds the permission of managing the subscribers are taken into
 * account. The core instantiates this class by reflection: the services are looked up through CDI.
 */
public class PendingSubscriptionsDashboardComponent extends DashboardComponent
{
    // Marks
    private static final String MARK_PENDING_COUNT = "pending_count";

    // Templates
    private static final String TEMPLATE_DASHBOARD = "/admin/plugins/newsletter/dashboard/pending_subscriptions_dashboard.html";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDashboardData( AdminUser user, HttpServletRequest request )
    {
        SubscriberService subscriberService = CDI.current( ).select( SubscriberService.class ).get( );
        List<Integer> listIdNewsletters = subscriberService.getNewsletterIds( subscriberService.getAuthorizedNewsletters( (User) user ) );

        Map<String, Object> model = new HashMap<>( );
        model.put( MARK_PENDING_COUNT, listIdNewsletters.isEmpty( ) ? 0 : subscriberService.findSubscriberIds( buildPendingFilter( listIdNewsletters ) ).size( ) );

        return AppTemplateService.getTemplate( TEMPLATE_DASHBOARD, user.getLocale( ), model ).getHtml( );
    }

    /**
     * Builds the filter of the active subscribers having a subscription awaiting their confirmation
     *
     * @param listIdNewsletters
     *            the identifiers of the newsletters the administrator may manage
     * @return the filter
     */
    private static SubscriberFilter buildPendingFilter( List<Integer> listIdNewsletters )
    {
        SubscriberFilter filter = new SubscriberFilter( );
        filter.setAuthorizedNewsletterIds( listIdNewsletters );
        filter.setStatus( Subscriber.STATUS_ACTIVE );
        filter.setSubscriptionState( SubscriberFilter.SUBSCRIPTION_PENDING );

        return filter;
    }
}
