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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import fr.paris.lutece.plugins.newsletter.business.Subscriber;
import fr.paris.lutece.portal.service.rbac.ResourceType;
import fr.paris.lutece.portal.service.rbac.ResourceTypeManager;
import fr.paris.lutece.test.LuteceTestCase;

/**
 * The RBAC resource type of the management of the subscribers of all the newsletters
 */
public class SubscriberResourceIdServiceTest extends LuteceTestCase
{
    /**
     * The resource type is registered with one permission per action, granted on all the subscribers
     */
    @Test
    public void testRegister( )
    {
        SubscriberResourceIdService service = new SubscriberResourceIdService( );
        service.register( );

        ResourceType resourceType = ResourceTypeManager.getResourceType( Subscriber.RESOURCE_TYPE );
        assertNotNull( resourceType, "the resource type is registered" );
        assertEquals( 6, resourceType.getPermissionList( ).size( ), "one permission per action" );

        for ( String strPermission : new String [ ] {
                SubscriberResourceIdService.PERMISSION_VIEW, SubscriberResourceIdService.PERMISSION_CREATE,
                SubscriberResourceIdService.PERMISSION_MODIFY_STATUS, SubscriberResourceIdService.PERMISSION_VALIDATE,
                SubscriberResourceIdService.PERMISSION_SUBSCRIBE, SubscriberResourceIdService.PERMISSION_UNSUBSCRIBE
        } )
        {
            assertNotNull( resourceType.getPermission( strPermission ), "the permission " + strPermission + " is registered" );
        }

        assertTrue( service.getResourceIdList( Locale.FRENCH ).isEmpty( ), "the permissions are granted on all the subscribers only" );
        assertNull( service.getTitle( "1", Locale.FRENCH ), "no subscriber is a resource of its own" );
    }
}
